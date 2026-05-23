package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class SupabaseSignUpRequest(
    val email: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class SupabaseLoginRequest(
    val email: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class SupabaseUser(
    val id: String,
    val email: String?
)

@JsonClass(generateAdapter = true)
data class SupabaseAuthResponse(
    val access_token: String?,
    val token_type: String?,
    val expires_in: Long?,
    val refresh_token: String?,
    val user: SupabaseUser?
)

// BI-DIRECTIONAL REMOTE SYNC MODELS
@JsonClass(generateAdapter = true)
data class RemoteTransaction(
    val id: Long? = null,
    val description: String,
    val amount: Double,
    val type: String,
    val category: String,
    val dateString: String,
    val timestamp: Long,
    val extraText: String,
    val week: String,
    val user_id: String? = null
)

@JsonClass(generateAdapter = true)
data class RemoteCategory(
    val id: Long? = null,
    val name: String,
    val type: String,
    val user_id: String? = null
)

@JsonClass(generateAdapter = true)
data class RemoteOrder(
    val id: Long? = null,
    val clientName: String,
    val pantyType: String,
    val pantySize: String,
    val quantity: Int,
    val pantyValue: Double,
    val totalValue: Double,
    val week: String,
    val user_id: String? = null
)

@JsonClass(generateAdapter = true)
data class RemotePieceCalculation(
    val id: Long? = null,
    val pano: String,
    val kg: Double?,
    val valorKg: Double?,
    val quantidade: Int?,
    val user_id: String? = null
)

interface SupabaseAuthApi {
    @POST("auth/v1/signup")
    suspend fun signUp(
        @Header("apikey") apiKey: String,
        @Body request: SupabaseSignUpRequest
    ): Response<SupabaseAuthResponse>

    @POST("auth/v1/token?grant_type=password")
    suspend fun login(
        @Header("apikey") apiKey: String,
        @Body request: SupabaseLoginRequest
    ): Response<SupabaseAuthResponse>
}

interface SupabaseDataApi {
    // PUSH operations (upsert)
    @POST("rest/v1/transactions")
    suspend fun upsertTransactions(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates",
        @Body list: List<RemoteTransaction>
    ): Response<Unit>

    @POST("rest/v1/categories")
    suspend fun upsertCategories(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates",
        @Body list: List<RemoteCategory>
    ): Response<Unit>

    @POST("rest/v1/orders")
    suspend fun upsertOrders(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates",
        @Body list: List<RemoteOrder>
    ): Response<Unit>

    @POST("rest/v1/piece_calculations")
    suspend fun upsertCalculations(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates",
        @Body list: List<RemotePieceCalculation>
    ): Response<Unit>

    // PULL operations (fetch)
    @GET("rest/v1/transactions")
    suspend fun getTransactions(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String
    ): Response<List<RemoteTransaction>>

    @GET("rest/v1/categories")
    suspend fun getCategories(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String
    ): Response<List<RemoteCategory>>

    @GET("rest/v1/orders")
    suspend fun getOrders(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String
    ): Response<List<RemoteOrder>>

    @GET("rest/v1/piece_calculations")
    suspend fun getCalculations(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String
    ): Response<List<RemotePieceCalculation>>
}

object SupabaseClient {
    private const val TAG = "SupabaseClient"

    val supabaseUrl: String by lazy {
        try {
            BuildConfig.SUPABASE_URL.trim()
        } catch (e: Throwable) {
            ""
        }
    }

    val supabaseAnonKey: String by lazy {
        try {
            BuildConfig.SUPABASE_ANON_KEY.trim()
        } catch (e: Throwable) {
            ""
        }
    }

    val isConfigured: Boolean
        get() = supabaseUrl.isNotEmpty() &&
                supabaseUrl != "YOUR_SUPABASE_URL" &&
                !supabaseUrl.contains("PLACEHOLDER") &&
                supabaseAnonKey.isNotEmpty() &&
                supabaseAnonKey != "YOUR_SUPABASE_ANON_KEY" &&
                !supabaseAnonKey.contains("PLACEHOLDER")

    private val retrofitInstance: Retrofit? by lazy {
        if (!isConfigured) {
            Log.w(TAG, "Supabase keys are unconfigured. Running in local fallback.")
            null
        } else {
            try {
                val logging = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
                val okHttpClient = OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build()

                val formattedUrl = if (supabaseUrl.endsWith("/")) supabaseUrl else "$supabaseUrl/"
                Retrofit.Builder()
                    .baseUrl(formattedUrl)
                    .client(okHttpClient)
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build()
            } catch (e: Exception) {
                Log.e(TAG, "Error building Retrofit client for Supabase: ${e.message}", e)
                null
            }
        }
    }

    val api: SupabaseAuthApi?
        get() = retrofitInstance?.create(SupabaseAuthApi::class.java)

    val dataApi: SupabaseDataApi?
        get() = retrofitInstance?.create(SupabaseDataApi::class.java)
}
