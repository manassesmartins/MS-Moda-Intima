package com.example.data.api

import android.util.Log
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.Response
import retrofit2.http.*
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GoogleUser(
    val id: String,
    val email: String,
    val name: String,
    val picture: String? = null
)

// SHEETS SYNC MODELS
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

@JsonClass(generateAdapter = true)
data class SheetsAppendValueRequest(
    val range: String,
    val majorDimension: String = "ROWS",
    val values: List<List<Any>>
)

@JsonClass(generateAdapter = true)
data class SheetsAppendValueResponse(
    val spreadsheetId: String,
    val tableRange: String?,
    val updates: SheetsUpdates?
)

@JsonClass(generateAdapter = true)
data class SheetsUpdates(
    val spreadsheetId: String,
    val updatedRange: String,
    val updatedRows: Int,
    val updatedColumns: Int,
    val updatedCells: Int
)

@JsonClass(generateAdapter = true)
data class SheetsValueRange(
    val range: String,
    val majorDimension: String = "ROWS",
    val values: List<List<String>>?
)

interface GoogleSheetsApi {
    @GET("v4/spreadsheets/{spreadsheetId}/values/{range}")
    suspend fun getSpreadsheetValues(
        @Path("spreadsheetId") spreadsheetId: String,
        @Path("range") range: String,
        @Header("Authorization") authHeader: String
    ): Response<SheetsValueRange>

    @POST("v4/spreadsheets/{spreadsheetId}/values/{range}:append")
    suspend fun appendSpreadsheetValues(
        @Path("spreadsheetId") spreadsheetId: String,
        @Path("range") range: String,
        @Query("valueInputOption") valueInputOption: String = "USER_ENTERED",
        @Header("Authorization") authHeader: String,
        @Body body: SheetsAppendValueRequest
    ): Response<SheetsAppendValueResponse>

    @PUT("v4/spreadsheets/{spreadsheetId}/values/{range}")
    suspend fun updateSpreadsheetValues(
        @Path("spreadsheetId") spreadsheetId: String,
        @Path("range") range: String,
        @Query("valueInputOption") valueInputOption: String = "USER_ENTERED",
        @Header("Authorization") authHeader: String,
        @Body body: SheetsValueRange
    ): Response<Unit>
}

object GoogleSheetsClient {
    private const val TAG = "GoogleSheetsClient"
    
    // Default master Spreadsheet ID for MS Moda Íntima
    var spreadsheetId: String = "1MS_ProducaoModaIntima_Backup_DB"
    
    var googleAccessToken: String = "ya29.mock-google-login-sheets-key-access-msmodaintima-token"

    val isConfigured: Boolean
        get() = spreadsheetId.isNotEmpty()

    private val retrofitInstance: Retrofit by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://sheets.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    val sheetsApi: GoogleSheetsApi by lazy {
        retrofitInstance.create(GoogleSheetsApi::class.java)
    }
}
