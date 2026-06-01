package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONArray
import org.json.JSONObject

object MqttSyncManager {

    private const val BROKER = "tcp://broker.hivemq.com:1883"
    private const val TAG = "MqttSyncManager"

    suspend fun syncWithWeb(
        pinCode: String,
        context: Context,
        transactions: List<TransactionEntity>,
        categories: List<CategoryEntity>,
        orders: List<OrderEntity>,
        calculations: List<PieceCalculationEntity>,
        brandConfig: JSONObject?
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val clientId = "gestor-android-\${System.currentTimeMillis()}"
            val persistence = MemoryPersistence()
            val client = MqttClient(BROKER, clientId, persistence)

            val connOpts = MqttConnectOptions()
            connOpts.isCleanSession = true
            connOpts.connectionTimeout = 10
            
            client.connect(connOpts)
            
            val payload = JSONObject()
            
            // Serialize
            val txArray = JSONArray()
            transactions.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("description", it.description)
                obj.put("amount", it.amount)
                obj.put("type", it.type)
                obj.put("category", it.category)
                obj.put("dateString", it.dateString)
                obj.put("timestamp", it.timestamp)
                obj.put("week", it.week)
                txArray.put(obj)
            }
            payload.put("transactions", txArray)
            
            val catArray = JSONArray()
            categories.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("name", it.name)
                obj.put("type", it.type)
                catArray.put(obj)
            }
            payload.put("categories", catArray)
            
            val ordArray = JSONArray()
            orders.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("clientName", it.clientName)
                obj.put("pantyType", it.pantyType)
                obj.put("pantySize", it.pantySize)
                obj.put("quantity", it.quantity)
                obj.put("pantyValue", it.pantyValue)
                obj.put("totalValue", it.totalValue)
                obj.put("week", it.week)
                obj.put("businessArea", it.businessArea)
                obj.put("status", it.status)
                ordArray.put(obj)
            }
            payload.put("orders", ordArray)
            
            val calcArray = JSONArray()
            calculations.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("pano", it.pano)
                obj.put("kg", it.kg)
                obj.put("valorKg", it.valorKg)
                obj.put("quantidade", it.quantidade)
                calcArray.put(obj)
            }
            payload.put("calculations", calcArray)
            
            if (brandConfig != null) {
                payload.put("brandConfig", brandConfig)
            }

            // Publish message
            val message = MqttMessage(payload.toString().toByteArray())
            message.qos = 1
            client.publish("gestor_producao/sync/$pinCode", message)
            
            client.disconnect()
            Log.i(TAG, "Sync payload sent to topic: gestor_producao/sync/$pinCode")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing via MQTT: \${e.message}")
            return@withContext false
        }
    }
}
