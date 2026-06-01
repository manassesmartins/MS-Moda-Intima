package com.example.ui

import android.content.Context
import android.util.Log
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

sealed class UpdateStatus {
    object Idle : UpdateStatus()
    object Checking : UpdateStatus()
    data class UpdateAvailable(
        val version: String,
        val changelog: String,
        val downloadUrl: String,
        val type: UpdateType, // RELEASE or COMMIT or RAW
        val itemName: String, // e.g. commit message or tag name
        val date: String
    ) : UpdateStatus()
    object UpToDate : UpdateStatus()
    data class Downloading(val progress: Float) : UpdateStatus()
    data class Downloaded(val apkFile: File) : UpdateStatus()
    data class Error(val message: String) : UpdateStatus()
}

enum class UpdateType {
    RELEASE,
    COMMIT,
    RAW
}

class GitHubUpdater(private val context: Context) {

    private val sharedPrefs = context.getSharedPreferences("ms_producao_github_updater_prefs", Context.MODE_PRIVATE)
    private val client = OkHttpClient()

    private val _status = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val status: StateFlow<UpdateStatus> = _status.asStateFlow()

    private var latestCheckedSha: String = ""

    // Configurable parameters with smart defaults
    var owner: String
        get() = sharedPrefs.getString("github_owner", "ManassesMartins")?.takeIf { it.isNotBlank() } ?: "ManassesMartins"
        set(value) {
            sharedPrefs.edit().putString("github_owner", value.trim()).apply()
        }

    var repo: String
        get() = sharedPrefs.getString("github_repo", "workspace-ms-producao-valeriacalc")?.takeIf { it.isNotBlank() } ?: "workspace-ms-producao-valeriacalc"
        set(value) {
            sharedPrefs.edit().putString("github_repo", value.trim()).apply()
        }

    var branch: String
        get() = sharedPrefs.getString("github_branch", "main")?.takeIf { it.isNotBlank() } ?: "main"
        set(value) {
            sharedPrefs.edit().putString("github_branch", value.trim()).apply()
        }

    var apkPath: String
        get() = sharedPrefs.getString("github_apk_path", "app-debug.apk")?.takeIf { it.isNotBlank() } ?: "app-debug.apk"
        set(value) {
            sharedPrefs.edit().putString("github_apk_path", value.trim()).apply()
        }

    var lastNotifiedVersion: String
        get() = sharedPrefs.getString("last_notified_version", "") ?: ""
        set(value) {
            sharedPrefs.edit().putString("last_notified_version", value).apply()
        }

    var versionJsonPath: String
        get() = sharedPrefs.getString("github_version_json_path", "version.json")?.takeIf { it.isNotBlank() } ?: "version.json"
        set(value) {
            sharedPrefs.edit().putString("github_version_json_path", value.trim()).apply()
        }

    fun getLocalVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    fun clearStatus() {
        _status.value = UpdateStatus.Idle
    }

    /**
     * Checks both GitHub releases and git commits to find if an update exists.
     */
    suspend fun checkForUpdates(forceNotify: Boolean = false) = withContext(Dispatchers.IO) {
        _status.value = UpdateStatus.Checking
        try {
            val currentVersion = getLocalVersion()

            // Step 0: Check custom version.json URL if it contains version info
            var foundCustomJsonUpdate = false
            val customJsonUrl = "https://raw.githubusercontent.com/$owner/$repo/$branch/$versionJsonPath"
            val customJsonRequest = Request.Builder()
                .url(customJsonUrl)
                .addHeader("User-Agent", "Mozilla/5.0")
                .build()

            try {
                client.newCall(customJsonRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyString = response.body?.string()
                        if (!bodyString.isNullOrEmpty()) {
                            val json = JSONObject(bodyString)
                            val latestVersion = json.optString("version", "").trim()
                            if (latestVersion.isNotEmpty()) {
                                val changelog = json.optString("changelog", "Nova versão disponível no repositório.")
                                val rawDownloadUrl = json.optString("downloadUrl", "").trim()
                                val downloadUrl = if (rawDownloadUrl.isNotEmpty()) {
                                    rawDownloadUrl
                                } else {
                                    "https://raw.githubusercontent.com/$owner/$repo/$branch/$apkPath"
                                }

                                val hasNewerVersion = isNewerVersion(latestVersion, currentVersion)
                                if (hasNewerVersion) {
                                    if (forceNotify || latestVersion != lastNotifiedVersion) {
                                        sharedPrefs.edit().putString("pending_notified_version", latestVersion).apply()
                                        sharedPrefs.edit().putString("pending_commit_sha", "").apply()
                                        _status.value = UpdateStatus.UpdateAvailable(
                                            version = latestVersion,
                                            changelog = changelog,
                                            downloadUrl = downloadUrl,
                                            type = UpdateType.RAW,
                                            itemName = "Atualização via JSON",
                                            date = ""
                                        )
                                        foundCustomJsonUpdate = true
                                    }
                                } else {
                                    sharedPrefs.edit().remove("pending_notified_version").remove("pending_commit_sha").apply()
                                    _status.value = if (forceNotify) UpdateStatus.UpToDate else UpdateStatus.Idle
                                    foundCustomJsonUpdate = true
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("GitHubUpdater", "Failed to check custom version.json, proceeding to releases", e)
            }

            if (foundCustomJsonUpdate) return@withContext

            // Step 1: Check GitHub releases first (highly structured)
            val releaseUrl = "https://api.github.com/repos/$owner/$repo/releases/latest"
            val releaseRequest = Request.Builder()
                .url(releaseUrl)
                .addHeader("Accept", "application/vnd.github+json")
                .addHeader("User-Agent", "Mozilla/5.0")
                .build()

            var foundReleaseUpdate = false

            try {
                client.newCall(releaseRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyString = response.body?.string()
                        if (!bodyString.isNullOrEmpty()) {
                            val releaseObj = JSONObject(bodyString)
                            val tagName = releaseObj.optString("tag_name", "").removePrefix("v")
                            val releaseName = releaseObj.optString("name", "Nova Versão")
                            val changelog = releaseObj.optString("body", "Sem changelog fornecido.")
                            val publishedAt = releaseObj.optString("published_at", "").take(10)
                            
                            // Look for any .apk asset
                            var apkDownloadUrl = ""
                            val assetsArray = releaseObj.optJSONArray("assets")
                            if (assetsArray != null) {
                                for (i in 0 until assetsArray.length()) {
                                    val assetObj = assetsArray.getJSONObject(i)
                                    val assetName = assetObj.optString("name", "")
                                    if (assetName.endsWith(".apk")) {
                                        apkDownloadUrl = assetObj.optString("browser_download_url", "")
                                        break
                                    }
                                }
                            }

                            // If we don't have an APK asset, we can fallback to the raw repository path
                            if (apkDownloadUrl.isEmpty()) {
                                apkDownloadUrl = "https://raw.githubusercontent.com/$owner/$repo/$branch/$apkPath"
                            }

                            val hasNewerVersion = isNewerVersion(tagName, currentVersion)
                            if (hasNewerVersion || (forceNotify && tagName.isNotEmpty())) {
                                if (forceNotify || tagName != lastNotifiedVersion) {
                                    sharedPrefs.edit().putString("pending_notified_version", tagName).apply()
                                    sharedPrefs.edit().putString("pending_commit_sha", "").apply()
                                    _status.value = UpdateStatus.UpdateAvailable(
                                        version = tagName,
                                        changelog = changelog,
                                        downloadUrl = apkDownloadUrl,
                                        type = UpdateType.RELEASE,
                                        itemName = releaseName,
                                        date = publishedAt
                                    )
                                    foundReleaseUpdate = true
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("GitHubUpdater", "Failed to check releases, will attempt commits/fallback", e)
            }

            if (foundReleaseUpdate) return@withContext

            // Step 2: If no release update was found, check commits of the specific APK file path on GitHub
            val encodedApkPath = java.net.URLEncoder.encode(apkPath, "UTF-8")
            val commitsUrl = "https://api.github.com/repos/$owner/$repo/commits?per_page=1&sha=$branch&path=$encodedApkPath"
            val commitsRequest = Request.Builder()
                .url(commitsUrl)
                .addHeader("Accept", "application/vnd.github+json")
                .addHeader("User-Agent", "Mozilla/5.0")
                .build()

            var commitsRequestSuccess = false
            try {
                client.newCall(commitsRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        commitsRequestSuccess = true
                        val bodyString = response.body?.string()
                        if (!bodyString.isNullOrEmpty()) {
                            val commitsArray = JSONArray(bodyString)
                            if (commitsArray.length() > 0) {
                                val latestCommitObj = commitsArray.getJSONObject(0)
                                val sha = latestCommitObj.optString("sha", "")
                                val commitObj = latestCommitObj.optJSONObject("commit")
                                val message = commitObj?.optString("message", "Nova alteração de código") ?: ""
                                val authorObj = commitObj?.optJSONObject("author")
                                val authorName = authorObj?.optString("name", "") ?: ""
                                val dateString = authorObj?.optString("date", "")?.take(10) ?: ""

                                val savedCommitSha = sharedPrefs.getString("last_checked_commit_sha", "") ?: ""

                                if (sha != savedCommitSha || forceNotify) {
                                    // A new commit is detected on GitHub!
                                    latestCheckedSha = sha
                                    sharedPrefs.edit().putString("pending_commit_sha", sha).apply()
                                    sharedPrefs.edit().putString("pending_notified_version", "").apply()
                                    val downloadUrl = "https://raw.githubusercontent.com/$owner/$repo/$branch/$apkPath"
                                    _status.value = UpdateStatus.UpdateAvailable(
                                        version = "Commit ${sha.take(7)}",
                                        changelog = "Commit por $authorName:\n$message",
                                        downloadUrl = downloadUrl,
                                        type = UpdateType.COMMIT,
                                        itemName = message,
                                        date = dateString
                                    )
                                    return@withContext
                                }
                            }
                        }
                    } else {
                        Log.e("GitHubUpdater", "Commits API returned error code ${response.code}")
                        if (forceNotify) {
                            if (response.code == 404) {
                                // Treating 404 as "Up to Date" ensures that if there's no custom public release/commit pushed yet,
                                // the app gracefully reports it is in the latest stable compiled version rather than throwing errors.
                                _status.value = UpdateStatus.UpToDate
                            } else if (response.code == 403) {
                                _status.value = UpdateStatus.Error("Limite de requisições da API do GitHub excedido (HTTP 403).")
                            } else {
                                _status.value = UpdateStatus.Error("Erro na API do GitHub (HTTP ${response.code}).")
                            }
                        } else {
                            _status.value = UpdateStatus.Idle
                        }
                        return@withContext
                    }
                }
            } catch (e: Exception) {
                Log.e("GitHubUpdater", "Failed to check commits", e)
                if (forceNotify) {
                    _status.value = UpdateStatus.Error("Conexão falhou ao verificar commits: ${e.localizedMessage}")
                } else {
                    _status.value = UpdateStatus.Idle
                }
                return@withContext
            }

            if (commitsRequestSuccess) {
                _status.value = if (forceNotify) UpdateStatus.UpToDate else UpdateStatus.Idle
            }
        } catch (e: Exception) {
            Log.e("GitHubUpdater", "Error checking for updates", e)
            if (forceNotify) {
                _status.value = UpdateStatus.Error("Erro de conexão: ${e.localizedMessage ?: "Verifique sua internet"}")
            } else {
                _status.value = UpdateStatus.Idle
            }
        }
    }

    /**
     * Downloads the APK file from the provided URL, reporting progress.
     */
    suspend fun downloadApk(url: String): File? = withContext(Dispatchers.IO) {
        _status.value = UpdateStatus.Downloading(0.01f)
        try {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    _status.value = UpdateStatus.Error("Falha ao baixar arquivo: HTTP ${response.code}")
                    return@withContext null
                }

                val body = response.body
                if (body == null) {
                    _status.value = UpdateStatus.Error("Arquivo vazio retornado pelo servidor")
                    return@withContext null
                }

                val contentLength = body.contentLength()
                val cacheFile = File(context.cacheDir, "update.apk")
                if (cacheFile.exists()) {
                    cacheFile.delete()
                }

                val buffer = ByteArray(4096)
                var bytesRead: Int
                var totalBytesRead = 0L

                body.byteStream().use { inputStream ->
                    FileOutputStream(cacheFile).use { outputStream ->
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead
                            if (contentLength > 0) {
                                val progress = totalBytesRead.toFloat() / contentLength.toFloat()
                                _status.value = UpdateStatus.Downloading(progress.coerceIn(0.01f, 0.99f))
                            }
                        }
                        outputStream.flush()
                    }
                }

                _status.value = UpdateStatus.Downloaded(cacheFile)
                cacheFile
            }
        } catch (e: Exception) {
            Log.e("GitHubUpdater", "Error downloading APK", e)
            _status.value = UpdateStatus.Error("Falha no download: ${e.localizedMessage ?: "Verifique sua rede"}")
            null
        }
    }

    /**
     * Installs the downloaded APK leveraging Android's FileProvider.
     */
    fun installApk(apkFile: File) {
        try {
            if (!apkFile.exists()) {
                _status.value = UpdateStatus.Error("Arquivo APK não encontrado localmente.")
                return
            }

            val authority = "${context.packageName}.fileprovider"
            val apkUri: Uri = FileProvider.getUriForFile(context, authority, apkFile)

            val pendingVer = sharedPrefs.getString("pending_notified_version", "") ?: ""
            if (pendingVer.isNotEmpty()) {
                sharedPrefs.edit().putString("last_notified_version", pendingVer).apply()
            }

            val pendingSha = sharedPrefs.getString("pending_commit_sha", "") ?: ""
            val shaToSave = if (pendingSha.isNotEmpty()) pendingSha else latestCheckedSha
            if (shaToSave.isNotEmpty()) {
                sharedPrefs.edit().putString("last_checked_commit_sha", shaToSave).apply()
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // For Android O and higher, verify package installs authorization
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    // Open settings directly. It's safe since Android N+ requires exact package URI
                    val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(settingsIntent)
                    // We also show a fallback error instructing user to authorize and click again
                    _status.value = UpdateStatus.Error("Por favor, autorize a instalação de fontes desconhecidas para este aplicativo nas configurações que foram abertas e tente novamente.")
                    return
                }
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("GitHubUpdater", "Error installing APK", e)
            _status.value = UpdateStatus.Error("Erro ao iniciar instalador: ${e.localizedMessage}")
        }
    }

    private fun isNewerVersion(remote: String, local: String): Boolean {
        if (remote.isEmpty() || remote == local) return false
        
        val remoteParts = remote.split(".").map { it.toIntOrNull() ?: 0 }
        val localParts = local.split(".").map { it.toIntOrNull() ?: 0 }

        val length = maxOf(remoteParts.size, localParts.size)
        for (i in 0 until length) {
            val r = remoteParts.getOrElse(i) { 0 }
            val l = localParts.getOrElse(i) { 0 }
            if (r > l) return true
            if (r < l) return false
        }
        return false
    }
}
