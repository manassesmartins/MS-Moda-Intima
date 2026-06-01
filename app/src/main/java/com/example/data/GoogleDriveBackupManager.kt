package com.example.data

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GoogleDriveBackupManager {
    private const val TAG = "GoogleDriveBackup"
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private const val BACKUP_FILE_NAME = "ms_modaintima_database_backup.db"

    // Search for the backup file in Google Drive, returns fileId if found
    suspend fun findBackupFile(accessToken: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = "https://www.googleapis.com/drive/v3/files?q=name='$BACKUP_FILE_NAME' and trashed=false&fields=files(id,name)"
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $accessToken")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Search failed: Code ${response.code} - ${response.body?.string()}")
                    return@withContext null
                }
                val bodyStr = response.body?.string() ?: ""
                val json = JSONObject(bodyStr)
                val files = json.optJSONArray("files")
                if (files != null && files.length() > 0) {
                    val fileObj = files.getJSONObject(0)
                    return@withContext fileObj.getString("id")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error finding backup file", e)
        }
        return@withContext null
    }

    // Download file from Google Drive and write the bytes to the local database file path
    suspend fun downloadBackup(accessToken: String, fileId: String, context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "https://www.googleapis.com/drive/v3/files/$fileId?alt=media"
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $accessToken")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Download failed: Code ${response.code} - ${response.body?.string()}")
                    return@withContext false
                }
                val bytes = response.body?.bytes() ?: return@withContext false
                return@withContext restoreDatabase(context, bytes)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading backup", e)
        }
        return@withContext false
    }

    // Upload backup file (create or update)
    suspend fun uploadBackup(accessToken: String, context: Context): Boolean = withContext(Dispatchers.IO) {
        val backupFile = backupDatabase(context) ?: return@withContext false
        try {
            val existingFileId = findBackupFile(accessToken)
            if (existingFileId != null) {
                // UPDATE existing backup
                val url = "https://www.googleapis.com/upload/drive/v3/files/$existingFileId?uploadType=media"
                val mediaType = "application/octet-stream".toMediaType()
                val requestBody = backupFile.asRequestBody(mediaType)
                val request = Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer $accessToken")
                    .patch(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        Log.i(TAG, "Backup updated successfully! FileId: $existingFileId")
                        backupFile.delete()
                        return@withContext true
                    } else {
                        Log.e(TAG, "Update failed: Code ${response.code} - ${response.body?.string()}")
                    }
                }
            } else {
                // CREATE new backup
                // 1. Create Metadata
                val metaUrl = "https://www.googleapis.com/drive/v3/files"
                val metaJson = JSONObject().apply {
                    put("name", BACKUP_FILE_NAME)
                    put("mimeType", "application/octet-stream")
                }
                val metaBody = metaJson.toString().toRequestBody("application/json".toMediaType())
                val metaRequest = Request.Builder()
                    .url(metaUrl)
                    .header("Authorization", "Bearer $accessToken")
                    .post(metaBody)
                    .build()

                var newFileId: String? = null
                client.newCall(metaRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        val respBody = response.body?.string() ?: ""
                        newFileId = JSONObject(respBody).getString("id")
                    } else {
                        Log.e(TAG, "Metadata creation failed: Code ${response.code} - ${response.body?.string()}")
                    }
                }

                if (newFileId != null) {
                    // 2. Upload content
                    val uploadUrl = "https://www.googleapis.com/upload/drive/v3/files/$newFileId?uploadType=media"
                    val requestBody = backupFile.asRequestBody("application/octet-stream".toMediaType())
                    val uploadRequest = Request.Builder()
                        .url(uploadUrl)
                        .header("Authorization", "Bearer $accessToken")
                        .patch(requestBody)
                        .build()

                    client.newCall(uploadRequest).execute().use { response ->
                        if (response.isSuccessful) {
                            Log.i(TAG, "Backup created successfully! New FileId: $newFileId")
                            backupFile.delete()
                            return@withContext true
                        } else {
                            Log.e(TAG, "Content upload failed: Code ${response.code} - ${response.body?.string()}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading backup", e)
        } finally {
            if (backupFile.exists()) {
                backupFile.delete()
            }
        }
        return@withContext false
    }

    private fun backupDatabase(context: Context): File? {
        val dbFile = context.getDatabasePath("ms_modaintima_database")
        if (!dbFile.exists()) {
            Log.e(TAG, "Database file does not exist to backup")
            return null
        }
        
        // Force SQLite checkpointing before backup
        try {
            val db = AppDatabase.getDatabase(context)
            db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()
        } catch (e: Exception) {
            Log.e(TAG, "Error during Full Checkpoint", e)
        }

        val backupFile = File(context.cacheDir, "ms_modaintima_database.db")
        try {
            dbFile.inputStream().use { input ->
                backupFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            return backupFile
        } catch (e: Exception) {
            Log.e(TAG, "Error copying DB file to cache", e)
            return null
        }
    }

    private fun restoreDatabase(context: Context, backupBytes: ByteArray): Boolean {
        val dbName = "ms_modaintima_database"
        val dbFile = context.getDatabasePath(dbName)
        val dbWal = File(dbFile.path + "-wal")
        val dbShm = File(dbFile.path + "-shm")

        try {
            // Close active database
            try {
                AppDatabase.getDatabase(context).close()
            } catch (e: Exception) {
                Log.e(TAG, "Closing active DB failed", e)
            }

            if (!dbFile.parentFile.exists()) {
                dbFile.parentFile.mkdirs()
            }
            dbFile.outputStream().use { output ->
                output.write(backupBytes)
            }

            if (dbWal.exists()) dbWal.delete()
            if (dbShm.exists()) dbShm.delete()

            Log.i(TAG, "Database fully restored and WAL/SHM deleted.")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error writing restored DB file", e)
            return false
        }
    }
    
    // Import from local storage (or visual import/export with URI)
    fun importLocalDatabase(context: Context, inputStream: InputStream): Boolean {
        try {
            val bytes = inputStream.readBytes()
            return restoreDatabase(context, bytes)
        } catch (e: Exception) {
            Log.e(TAG, "Error importing local database", e)
            return false
        }
    }

    // Export local database to URI
    fun exportLocalDatabase(context: Context, outputStream: OutputStream): Boolean {
        try {
            val dbFile = context.getDatabasePath("ms_modaintima_database")
            if (!dbFile.exists()) return false

            // Checkpoint WAL
            try {
                AppDatabase.getDatabase(context).openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()
            } catch (e: Exception) {}

            dbFile.inputStream().use { input ->
                input.copyTo(outputStream)
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting local database", e)
            return false
        }
    }

    // Helper to request a fresh Google Drive/Profile access token for API calls
    suspend fun getGoogleAccessToken(context: Context): String? = withContext(Dispatchers.IO) {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return@withContext null
        return@withContext try {
            GoogleAuthUtil.getToken(
                context,
                account.account ?: return@withContext null,
                "oauth2:https://www.googleapis.com/auth/drive.file https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email openid"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting Google OAuth token", e)
            null
        }
    }
}
