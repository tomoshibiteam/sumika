package com.sumika.core.background

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import android.util.Size
import com.sumika.core.model.BackgroundSource
import com.sumika.core.model.BlurLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

/**
 * 背景画像の前処理とキャッシュ管理
 * 
 * 処理フロー:
 * 1. 画像選択時にリサイズ（画面サイズ以下に）
 * 2. ぼかし/明るさ調整
 * 3. キャッシュに保存
 * 4. 描画時はキャッシュBitmapを使用
 */
class BackgroundProcessor(private val context: Context) {
    
    companion object {
        private const val TAG = "BackgroundProcessor"
        private const val CACHE_DIR = "bg_cache"
        private const val MAX_DIMENSION = 1920  // 最大サイズ
    }
    
    private val cacheDir: File by lazy {
        File(context.cacheDir, CACHE_DIR).apply {
            if (!exists()) mkdirs()
        }
    }
    
    /**
     * 背景を処理してキャッシュ済みBitmapを取得
     */
    suspend fun process(
        source: BackgroundSource,
        blurLevel: BlurLevel,
        brightness: Float,
        targetSize: Size
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val cacheKey = generateCacheKey(source, blurLevel, brightness, targetSize)
            
            // キャッシュチェック
            getCached(cacheKey)?.let {
                Log.d(TAG, "Cache hit: $cacheKey")
                return@withContext it
            }
            
            Log.d(TAG, "Cache miss, processing: $cacheKey")
            
            // 元画像を読み込み & リサイズ
            val original = loadAndResize(source, targetSize) ?: return@withContext null
            
            // ぼかし適用
            val blurred = if (blurLevel != BlurLevel.NONE) {
                applyBlur(original, blurLevel)
            } else {
                original
            }
            
            // 明るさ調整
            val adjusted = if (brightness != 1.0f) {
                adjustBrightness(blurred, brightness)
            } else {
                blurred
            }
            
            // キャッシュに保存
            saveToCache(adjusted, cacheKey)
            
            adjusted
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process background: ${e.message}", e)
            null
        }
    }
    
    /**
     * 画像を読み込んでリサイズ
     */
    private fun loadAndResize(source: BackgroundSource, targetSize: Size): Bitmap? {
        return when (source) {
            is BackgroundSource.UserImage -> {
                loadAndResizeUri(source.uri, targetSize)
            }
            is BackgroundSource.Preset -> {
                // TODO: プリセット画像の読み込み
                null
            }
            is BackgroundSource.SolidColor -> {
                // 単色は処理不要、描画時にdrawColorで対応
                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).apply {
                    setPixel(0, 0, source.color)
                }
            }
        }
    }
    
    private fun loadAndResizeUri(uri: Uri, targetSize: Size): Bitmap? {
        return try {
            // まずサイズだけ取得
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
            
            // inSampleSizeを計算
            val (width, height) = options.outWidth to options.outHeight
            var inSampleSize = 1
            while (width / inSampleSize > MAX_DIMENSION || height / inSampleSize > MAX_DIMENSION) {
                inSampleSize *= 2
            }
            
            // 実際に読み込み
            val loadOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, loadOptions)
            }?.let { bitmap ->
                // ターゲットサイズにスケール
                val scale = minOf(
                    targetSize.width.toFloat() / bitmap.width,
                    targetSize.height.toFloat() / bitmap.height
                ).coerceAtMost(1f)
                
                if (scale < 1f) {
                    Bitmap.createScaledBitmap(
                        bitmap,
                        (bitmap.width * scale).toInt(),
                        (bitmap.height * scale).toInt(),
                        true
                    ).also {
                        if (it != bitmap) bitmap.recycle()
                    }
                } else {
                    bitmap
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load image: ${e.message}", e)
            null
        }
    }
    
    /**
     * ぼかしを適用
     */
    @Suppress("DEPRECATION")
    private fun applyBlur(source: Bitmap, level: BlurLevel): Bitmap {
        val radius = when (level) {
            BlurLevel.NONE -> return source
            BlurLevel.LIGHT -> 8f
            BlurLevel.MEDIUM -> 15f
            BlurLevel.HEAVY -> 25f
        }
        
        return try {
            // RenderScript (deprecated but still works)
            val rs = RenderScript.create(context)
            val input = Allocation.createFromBitmap(rs, source)
            val output = Allocation.createTyped(rs, input.type)
            
            val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
            script.setRadius(radius.coerceAtMost(25f))
            script.setInput(input)
            script.forEach(output)
            
            val result = Bitmap.createBitmap(source.width, source.height, source.config ?: Bitmap.Config.ARGB_8888)
            output.copyTo(result)
            
            input.destroy()
            output.destroy()
            script.destroy()
            rs.destroy()
            
            result
        } catch (e: Exception) {
            Log.w(TAG, "RenderScript blur failed, using fallback: ${e.message}")
            // フォールバック: 縮小→拡大でぼかし効果
            applyBoxBlur(source, (radius / 5).toInt().coerceAtLeast(1))
        }
    }
    
    /**
     * ボックスブラー（フォールバック）
     */
    private fun applyBoxBlur(source: Bitmap, radius: Int): Bitmap {
        val scale = 1f / (radius + 1)
        val small = Bitmap.createScaledBitmap(
            source,
            (source.width * scale).toInt().coerceAtLeast(1),
            (source.height * scale).toInt().coerceAtLeast(1),
            true
        )
        return Bitmap.createScaledBitmap(small, source.width, source.height, true).also {
            small.recycle()
        }
    }
    
    /**
     * 明るさを調整
     */
    private fun adjustBrightness(source: Bitmap, brightness: Float): Bitmap {
        val result = Bitmap.createBitmap(source.width, source.height, source.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        
        val colorMatrix = ColorMatrix().apply {
            setScale(brightness, brightness, brightness, 1f)
        }
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }
        
        canvas.drawBitmap(source, 0f, 0f, paint)
        return result
    }
    
    /**
     * キャッシュから読み込み
     */
    private fun getCached(cacheKey: String): Bitmap? {
        val file = File(cacheDir, "$cacheKey.webp")
        if (!file.exists()) return null
        
        return try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            file.delete()
            null
        }
    }
    
    /**
     * キャッシュに保存
     */
    private fun saveToCache(bitmap: Bitmap, cacheKey: String) {
        val file = File(cacheDir, "$cacheKey.webp")
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 85, out)
            }
            Log.d(TAG, "Cached: $cacheKey (${file.length() / 1024}KB)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cache: ${e.message}")
        }
    }
    
    /**
     * キャッシュキーを生成
     */
    private fun generateCacheKey(
        source: BackgroundSource,
        blurLevel: BlurLevel,
        brightness: Float,
        targetSize: Size
    ): String {
        val sourceKey = when (source) {
            is BackgroundSource.UserImage -> source.uri.toString()
            is BackgroundSource.Preset -> "preset:${source.name}"
            is BackgroundSource.SolidColor -> "color:${source.color}"
        }
        val raw = "$sourceKey|$blurLevel|$brightness|${targetSize.width}x${targetSize.height}"
        return md5(raw)
    }
    
    private fun md5(input: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * 古いキャッシュを削除
     */
    fun clearOldCache(maxAgeDays: Int = 7) {
        val cutoff = System.currentTimeMillis() - (maxAgeDays * 24 * 60 * 60 * 1000L)
        cacheDir.listFiles()?.forEach { file ->
            if (file.lastModified() < cutoff) {
                file.delete()
                Log.d(TAG, "Deleted old cache: ${file.name}")
            }
        }
    }
    
    /**
     * 全キャッシュを削除
     */
    fun clearAllCache() {
        cacheDir.listFiles()?.forEach { it.delete() }
        Log.d(TAG, "All cache cleared")
    }
}
