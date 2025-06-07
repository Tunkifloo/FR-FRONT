package com.example.fr_front.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

fun createMultipartFromUri(context: Context, uri: Uri, partName: String): MultipartBody.Part {
    val inputStream = context.contentResolver.openInputStream(uri)
    val fileName = getFileName(context, uri)

    // Crear archivo temporal
    val tempFile = File(context.cacheDir, fileName)
    val outputStream = FileOutputStream(tempFile)

    inputStream?.use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }

    // tipo MIME correcto basado en la extensión del archivo
    val mimeType = when (fileName.substringAfterLast('.', "").lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "webp" -> "image/webp"
        "bmp" -> "image/bmp"
        "gif" -> "image/gif"
        else -> "image/*" // Fallback genérico
    }

    val requestBody = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(partName, fileName, requestBody)
}

fun getFileName(context: Context, uri: Uri): String {
    var result = "image.jpg" // Cambiar default a jpg

    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    result = it.getString(displayNameIndex)
                }
            }
        }
    }
    if (result.isEmpty()) {
        result = uri.path?.let { path ->
            val cut = path.lastIndexOf('/')
            if (cut != -1) path.substring(cut + 1) else "image.jpg"
        } ?: "image.jpg"
    }

    if (!result.contains('.')) {
        result += ".jpg"
    }

    return result
}

fun formatDate(dateString: String): String {
    return try {
        // Aquí puedes implementar formateo de fecha si es necesario
        dateString.substring(0, 10) // Solo la fecha sin hora
    } catch (e: Exception) {
        dateString
    }
}

fun formatPercentage(value: Double): String {
    return String.format("%.1f%%", value * 100)
}

fun formatMetric(value: Double, decimals: Int = 3): String {
    return String.format("%.${decimals}f", value)
}