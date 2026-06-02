package com.example.ui.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

object LogoDecoder {
    fun decodeBase64ToBitmap(base64Str: String?): Bitmap? {
        if (base64Str.isNullOrBlank()) return null
        return try {
            val pureBase64 = if (base64Str.startsWith("data:")) {
                base64Str.substringAfter(",")
            } else {
                base64Str
            }
            val decodedString: ByteArray = Base64.decode(pureBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

@Composable
fun rememberBitmapFromBase64(base64Str: String?): ImageBitmap? {
    return remember(base64Str) {
        val bitmap = LogoDecoder.decodeBase64ToBitmap(base64Str)
        bitmap?.asImageBitmap()
    }
}
