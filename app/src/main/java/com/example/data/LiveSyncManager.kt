package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject
import java.util.UUID

object LiveSyncManager {
    private const val BROKER = "tcp://broker.hivemq.com:1883"
    private const val TAG = "LiveSyncManager"
    private const val PREFS_NAME = "LiveSyncPrefs"
    private const val PREF_KEY_CODE = "live_sync_group_code"

    private var mqttClient: MqttClient? = null
    var activeGroupCode: String? = null
        private set

    val clientUuid: String = UUID.randomUUID().toString()
    
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var repositoryRef: TransactionRepository? = null

    var isApplyingRemoteUpdate = false

    fun getStoredGroupCode(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(PREF_KEY_CODE, null)
    }

    fun saveGroupCode(context: Context, code: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (code == null) {
            prefs.edit().remove(PREF_KEY_CODE).apply()
        } else {
            prefs.edit().putString(PREF_KEY_CODE, code).apply()
        }
        activeGroupCode = code
    }

    fun initializeStoredSync(context: Context, repository: TransactionRepository) {
        val code = getStoredGroupCode(context)
        if (!code.isNullOrBlank()) {
            startSync(context, code, repository)
        }
    }

    @Synchronized
    fun startSync(context: Context, code: String, repository: TransactionRepository) {
        if (mqttClient != null && activeGroupCode == code) {
            Log.d(TAG, "Already synced with group: $code")
            return
        }
        
        stopSync(context)

        activeGroupCode = code
        saveGroupCode(context, code)
        repositoryRef = repository

        syncScope.launch {
            try {
                val clientId = "gestor-live-${clientUuid.take(8)}-${System.currentTimeMillis()}"
                val client = MqttClient(BROKER, clientId, MemoryPersistence())
                mqttClient = client

                val connOpts = MqttConnectOptions().apply {
                    isCleanSession = true
                    connectionTimeout = 15
                    keepAliveInterval = 30
                    isAutomaticReconnect = true
                }

                client.setCallback(object : MqttCallbackExtended {
                    override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                        Log.i(TAG, "MQTT LiveSync connected. Reconnect=$reconnect. Subscribing to group: $code")
                        try {
                            client.subscribe("gestor_producao/live/$code", 1)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error subscribing: ${e.message}")
                        }
                    }

                    override fun connectionLost(cause: Throwable?) {
                        Log.w(TAG, "MQTT LiveSync connection lost: ${cause?.message}")
                    }

                    override fun messageArrived(topic: String?, message: MqttMessage?) {
                        if (message == null) return
                        val payload = String(message.payload)
                        Log.d(TAG, "LiveSync message received: $payload")
                        handleIncomingPayload(payload)
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {}
                })

                client.connect(connOpts)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start live sync: ${e.message}")
            }
        }
    }

    @Synchronized
    fun stopSync(context: Context) {
        saveGroupCode(context, null)
        activeGroupCode = null
        val client = mqttClient
        mqttClient = null
        syncScope.launch {
            try {
                if (client != null && client.isConnected) {
                    client.disconnect()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error disconnecting MQTT: ${e.message}")
            }
        }
    }

    fun publishMutation(table: String, action: String, data: JSONObject) {
        val code = activeGroupCode ?: return
        val client = mqttClient ?: return
        if (!client.isConnected) return
        
        if (isApplyingRemoteUpdate) {
            return
        }

        syncScope.launch {
            try {
                val envelope = JSONObject().apply {
                    put("sender", clientUuid)
                    put("table", table)
                    put("action", action)
                    put("data", data)
                }
                val msg = MqttMessage(envelope.toString().toByteArray()).apply {
                    qos = 1
                }
                client.publish("gestor_producao/live/$code", msg)
                Log.i(TAG, "Published mutation to gestor_producao/live/$code: $table $action")
            } catch (e: Exception) {
                Log.e(TAG, "Error publishing mutation: ${e.message}")
            }
        }
    }

    private fun handleIncomingPayload(payloadStr: String) {
        val repo = repositoryRef ?: return
        try {
            val envelope = JSONObject(payloadStr)
            val sender = envelope.optString("sender")
            if (sender == clientUuid) {
                return
            }

            val table = envelope.getString("table")
            val action = envelope.getString("action")
            val data = envelope.getJSONObject("data")

            syncScope.launch {
                try {
                    isApplyingRemoteUpdate = true
                    
                    when (table) {
                        "transactions" -> {
                            val id = data.getLong("id")
                            if (action == "delete") {
                                repo.deleteById(id)
                            } else {
                                val entity = TransactionEntity(
                                    id = id,
                                    description = data.getString("description"),
                                    amount = data.getDouble("amount"),
                                    type = data.getString("type"),
                                    category = data.getString("category"),
                                    dateString = data.getString("dateString"),
                                    timestamp = data.getLong("timestamp"),
                                    synced = true,
                                    extraText = data.optString("extraText", ""),
                                    week = data.optString("week", "1ª Semana")
                                )
                                repo.insert(entity)
                            }
                        }
                        "categories" -> {
                            val id = data.getLong("id")
                            if (action == "delete") {
                                repo.deleteCategoryById(id)
                            } else {
                                val entity = CategoryEntity(
                                    id = id,
                                    name = data.getString("name"),
                                    type = data.getString("type")
                                )
                                repo.insertCategory(entity)
                            }
                        }
                        "orders" -> {
                            val id = data.getLong("id")
                            if (action == "delete") {
                                repo.deleteOrderById(id)
                            } else {
                                val entity = OrderEntity(
                                    id = id,
                                    clientName = data.getString("clientName"),
                                    pantyType = data.getString("pantyType"),
                                    pantySize = data.getString("pantySize"),
                                    quantity = data.getInt("quantity"),
                                    pantyValue = data.getDouble("pantyValue"),
                                    totalValue = data.getDouble("totalValue"),
                                    week = data.getString("week"),
                                    businessArea = data.getString("businessArea"),
                                    status = data.getString("status"),
                                    timestamp = data.optLong("timestamp", System.currentTimeMillis())
                                )
                                repo.insertOrder(entity)
                            }
                        }
                        "calculations" -> {
                            val id = data.getLong("id")
                            if (action == "delete") {
                                repo.deleteCalculationById(id)
                            } else {
                                val entity = PieceCalculationEntity(
                                    id = id,
                                    pano = data.getString("pano"),
                                    kg = if (data.isNull("kg")) null else data.getDouble("kg"),
                                    valorKg = if (data.isNull("valorKg")) null else data.getDouble("valorKg"),
                                    quantidade = if (data.isNull("quantidade")) null else data.getInt("quantidade")
                                )
                                repo.insertCalculation(entity)
                            }
                        }
                        "clients" -> {
                            val id = data.getLong("id")
                            if (action == "delete") {
                                repo.deleteClientById(id)
                            } else {
                                val entity = ClientEntity(
                                    id = id,
                                    name = data.getString("name"),
                                    phone = data.optString("phone", "")
                                )
                                repo.insertClient(entity)
                            }
                        }
                        "models" -> {
                            val id = data.getLong("id")
                            if (action == "delete") {
                                repo.deleteProductModelById(id)
                            } else {
                                val entity = ProductModelEntity(
                                    id = id,
                                    name = data.getString("name")
                                )
                                repo.insertProductModel(entity)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error applying incoming live update: ${e.message}")
                } finally {
                    isApplyingRemoteUpdate = false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "JSON error parsing payload: ${e.message}")
        }
    }
}
