package com.yourcompany.quotevault.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.yourcompany.quotevault.domain.model.Quote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class QuoteShareManager(private val context: Context) {

    sealed class ShareOption {
        object ShareAsText : ShareOption()
        data class ShareAsImage(val style: CardStyle) : ShareOption()
        data class SaveAsImage(val style: CardStyle) : ShareOption()
    }

    suspend fun executeShare(option: ShareOption, quote: Quote): ShareResult {
        return when (option) {
            is ShareOption.ShareAsText -> shareAsText(quote)
            is ShareOption.ShareAsImage -> shareAsImage(quote, option.style)
            is ShareOption.SaveAsImage -> saveAsImage(quote, option.style)
        }
    }

    private fun shareAsText(quote: Quote): ShareResult {
        return try {
            val shareText = "\"${quote.text}\"\n\n— ${quote.author}\n\nShared via QuoteVault"

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(intent, "Share Quote"))
            ShareResult.Success
        } catch (e: Exception) {
            ShareResult.Error(e.message ?: "Failed to share quote")
        }
    }

    private suspend fun shareAsImage(quote: Quote, style: CardStyle): ShareResult {
        return withContext(Dispatchers.IO) {
            try {
                val bitmap = createQuoteBitmap(quote, style)
                val file = saveBitmapToCache(bitmap, "quote_${quote.id}")
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(Intent.createChooser(intent, "Share Quote"))
                ShareResult.Success
            } catch (e: Exception) {
                ShareResult.Error(e.message ?: "Failed to create image")
            }
        }
    }

    private suspend fun saveAsImage(quote: Quote, style: CardStyle): ShareResult {
        return withContext(Dispatchers.IO) {
            try {
                val bitmap = createQuoteBitmap(quote, style)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    saveBitmapToMediaStore(bitmap, quote, style)
                } else {
                    saveBitmapToFile(bitmap, quote, style)
                }
                ShareResult.Success
            } catch (e: Exception) {
                ShareResult.Error(e.message ?: "Failed to save image")
            }
        }
    }

    private fun createQuoteBitmap(quote: Quote, style: CardStyle): Bitmap {
        val width = 800
        val height = 1200
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw background
        val backgroundColor = when (style) {
            CardStyle.MODERN -> 0xFFF5F5F5.toInt()
            CardStyle.CLASSIC -> 0xFFFFF8E1.toInt()
            CardStyle.MINIMAL -> 0xFFFFFFFF.toInt()
            CardStyle.DARK -> 0xFF212121.toInt()
            CardStyle.PASTEL -> 0xFFE1F5FE.toInt()
        }
        canvas.drawColor(backgroundColor)

        // Draw border for Modern and Classic styles
        if (style == CardStyle.MODERN || style == CardStyle.CLASSIC) {
            val borderPaint = android.graphics.Paint().apply {
                color = when (style) {
                    CardStyle.MODERN -> 0xFF6366F1.toInt()
                    CardStyle.CLASSIC -> 0xFF8B4513.toInt()
                    else -> 0xFF000000.toInt()
                }
                this.style = android.graphics.Paint.Style.STROKE
                strokeWidth = 12f
                isAntiAlias = true
            }
            canvas.drawRect(24f, 24f, (width - 24).toFloat(), (height - 24).toFloat(), borderPaint)
        }

        // Draw quote text
        val paint = android.graphics.Paint().apply {
            color = when (style) {
                CardStyle.DARK -> 0xFFFFFFFF.toInt()
                CardStyle.CLASSIC -> 0xFF5D4037.toInt()
                CardStyle.MINIMAL -> 0xFF000000.toInt()
                CardStyle.MODERN -> 0xFF212121.toInt()
                CardStyle.PASTEL -> 0xFF01579B.toInt()
            }
            textSize = 48f
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(
                when (style) {
                    CardStyle.CLASSIC -> android.graphics.Typeface.SERIF
                    else -> android.graphics.Typeface.SANS_SERIF
                },
                android.graphics.Typeface.BOLD
            )
        }

        // Draw quote with word wrapping
        val maxLineWidth = (width * 0.8).toInt()
        val words = quote.text.split(" ")
        var currentLine = ""
        var yPos = height / 3f
        val lineHeight = 70f

        words.forEach { word ->
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val textWidth = paint.measureText(testLine)

            if (textWidth < maxLineWidth) {
                currentLine = testLine
            } else {
                canvas.drawText("\"$currentLine\"", width / 2f, yPos, paint)
                currentLine = word
                yPos += lineHeight
            }
        }

        if (currentLine.isNotEmpty()) {
            canvas.drawText("\"$currentLine\"", width / 2f, yPos, paint)
        }

        // Draw author
        paint.textSize = 36f
        paint.alpha = 180
        paint.typeface = android.graphics.Typeface.create(
            when (style) {
                CardStyle.CLASSIC -> android.graphics.Typeface.SERIF
                else -> android.graphics.Typeface.SANS_SERIF
            },
            android.graphics.Typeface.ITALIC
        )
        yPos += lineHeight + 50f
        canvas.drawText("— ${quote.author}", width / 2f, yPos, paint)

        // Draw app branding
        paint.textSize = 24f
        paint.alpha = 128
        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.NORMAL)
        yPos += lineHeight
        canvas.drawText("Shared via QuoteVault", width / 2f, height - 60f, paint)

        return bitmap
    }

    

    private fun saveBitmapToCache(bitmap: Bitmap, filename: String): File {
        val cacheDir = context.cacheDir
        val file = File(cacheDir, "$filename.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }

    private fun saveBitmapToMediaStore(bitmap: Bitmap, quote: Quote, style: CardStyle): Uri? {
        val contentResolver = context.contentResolver
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val imageDetails = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "quote_${quote.id}_${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QuoteVault")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val uri = contentResolver.insert(collection, imageDetails) ?: return null

        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }

            imageDetails.clear()
            imageDetails.put(MediaStore.Images.Media.IS_PENDING, 0)
            contentResolver.update(uri, imageDetails, null, null)

            return uri
        } catch (e: Exception) {
            contentResolver.delete(uri, null, null)
            return null
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap, quote: Quote, style: CardStyle): Uri? {
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val quoteVaultDir = File(picturesDir, "QuoteVault")

        if (!quoteVaultDir.exists()) {
            quoteVaultDir.mkdirs()
        }

        val file = File(quoteVaultDir, "quote_${quote.id}_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        // Notify the media scanner
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = Uri.fromFile(file)
        context.sendBroadcast(mediaScanIntent)

        return Uri.fromFile(file)
    }
}