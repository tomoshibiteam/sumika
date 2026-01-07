package com.sumika.wallpaper.renderer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import com.sumika.core.animation.AnimationController
import com.sumika.core.animation.AnimationState
import com.sumika.core.animation.PetBehavior
import com.sumika.core.model.GrowthStage
import com.sumika.core.model.PetType
import com.sumika.wallpaper.R

/**
 * ペットのスプライト描画
 * 
 * スプライトシートレイアウト (2x2グリッド):
 * +---------------+---------------+
 * | 立ち (IDLE)   | 歩き ×2フレーム |
 * +---------------+---------------+
 * | 寝る (SLEEP)  | 喜び ×2フレーム |
 * +---------------+---------------+
 */
class PetRenderer(private val context: Context) {
    
    companion object {
        private const val TAG = "PetRenderer"
        
        // スプライトシートのグリッド設定
        private const val GRID_COLS = 2
        private const val GRID_ROWS = 2
        
        /**
         * 成長段階に応じたペットサイズ比率
         */
        fun getPetSizeRatio(stage: GrowthStage): Float = when (stage) {
            GrowthStage.BABY -> 0.18f   // 小さい
            GrowthStage.TEEN -> 0.22f   // 中間
            GrowthStage.ADULT -> 0.26f  // 大きい
        }
    }
    
    private var spriteBitmap: Bitmap? = null
    private var currentPetType: PetType? = null
    private var currentVariation: Int = -1
    
    // スプライトシートのプロパティ
    private var gridCols = 2
    private var gridRows = 2
    private var cellWidth = 0
    private var cellHeight = 0
    
    // 各状態のフレーム数と位置
    // Pair(行, フレーム数)
    private var animationMap = mutableMapOf<AnimationState, Pair<Int, Int>>()
    
    // アニメーションフレーム用タイマー
    private var animFrameIndex = 0
    private var lastFrameTime = 0L
    private var frameIntervalMs = 300L
    
    private val paint = Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
    }
    
    private val srcRect = Rect()
    private val dstRect = RectF()
    private val matrix = Matrix()
    
    /**
     * スプライトを読み込み
     */
    fun loadSprite(petType: PetType, variation: Int) {
        if (petType == currentPetType && variation == currentVariation && spriteBitmap != null) return
        
        // 既存のビットマップを解放
        spriteBitmap?.recycle()
        spriteBitmap = null
        
        currentPetType = petType
        currentVariation = variation
        
        // ペットタイプに応じた設定
        val resourceId: Int
        when (petType) {
            PetType.DOG -> {
                resourceId = R.drawable.pet_dog_sprite
                gridCols = 2
                gridRows = 2
                frameIntervalMs = 300L
                animationMap = mutableMapOf(
                    AnimationState.IDLE to (0 to 1),
                    AnimationState.SIT to (0 to 1),
                    AnimationState.WALK to (1 to 2),
                    AnimationState.RUN to (1 to 2),
                    AnimationState.SLEEP to (2 to 1), // 2行目(実際は下段)
                    AnimationState.HAPPY to (3 to 2), // 3行目(実際は下段の右)
                    AnimationState.PLAY to (3 to 2),
                    AnimationState.FOCUS to (0 to 1),
                    AnimationState.LEVEL_UP to (3 to 2)
                )
                // Note: Dog is 2x2 but uses sub-frames, we'll handle this in getSourceRect
            }
            PetType.CAT -> {
                resourceId = R.drawable.pet_cat_sprite
                gridCols = 4
                gridRows = 2
                frameIntervalMs = 250L
                animationMap = mutableMapOf(
                    AnimationState.IDLE to (0 to 1),    // 左上: 立ち
                    AnimationState.WALK to (1 to 3),    // 右上: 歩き (3フレーム)
                    AnimationState.RUN to (1 to 3),
                    AnimationState.SLEEP to (4 to 2),   // 左下: 寝る (垂直に2フレームと想定して4行目として扱う)
                    AnimationState.HAPPY to (5 to 3),   // 右下: 喜び (3フレーム)
                    AnimationState.PLAY to (5 to 3),
                    AnimationState.SIT to (0 to 1),
                    AnimationState.FOCUS to (0 to 1),
                    AnimationState.LEVEL_UP to (5 to 3)
                )
            }
            PetType.BIRD -> {
                resourceId = R.drawable.pet_dog_sprite
                gridCols = 2
                gridRows = 2
                animationMap = mutableMapOf(
                    AnimationState.IDLE to (0 to 1),
                    AnimationState.WALK to (1 to 2)
                )
            }
            PetType.RABBIT -> {
                resourceId = R.drawable.pet_rabbit_walk_sprite
                gridCols = 4  // 4列
                gridRows = 2  // 2行
                frameIntervalMs = 120L  // 速めのアニメーション
                animationMap = mutableMapOf(
                    AnimationState.IDLE to (0 to 1),     // 最初のフレーム
                    AnimationState.WALK to (0 to 8),     // 8フレーム全て使用
                    AnimationState.RUN to (0 to 8),      // 走りも歩きと同じ
                    AnimationState.SIT to (0 to 1),
                    AnimationState.SLEEP to (0 to 1),
                    AnimationState.HAPPY to (0 to 8),
                    AnimationState.PLAY to (0 to 8),
                    AnimationState.FOCUS to (0 to 1),
                    AnimationState.LEVEL_UP to (0 to 8)
                )
            }
        }
        
        try {
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inMutable = true
            }
            val loadedBitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)
            
            // 白い背景を透明に変換
            spriteBitmap = loadedBitmap?.let { makeWhiteTransparent(it) }
            
            spriteBitmap?.let { bitmap ->
                cellWidth = bitmap.width / gridCols
                cellHeight = bitmap.height / gridRows
                Log.d(TAG, "Sprite loaded ($petType): ${bitmap.width}x${bitmap.height}, cell: ${cellWidth}x${cellHeight}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load sprite: $e")
            spriteBitmap = null
        }
    }
    
    /**
     * ペットを描画
     */
    fun draw(
        canvas: Canvas,
        behavior: PetBehavior,
        animController: AnimationController,
        screenX: Float,
        screenY: Float,
        screenWidth: Int,
        screenHeight: Int,
        growthStage: GrowthStage = GrowthStage.BABY,
        isFocusing: Boolean = false
    ) {
        val petSize = screenWidth * getPetSizeRatio(growthStage)
        val bitmap = spriteBitmap
        
        if (bitmap == null || cellWidth == 0 || cellHeight == 0) {
            // スプライトがない場合はプレースホルダーを描画
            drawPlaceholder(canvas, behavior, animController, screenX, screenY, petSize, isFocusing)
            return
        }
        
        // アニメーション設定を取得
        val (rowIndex, frameCount) = animationMap[animController.state] ?: (0 to 1)
        
        // アニメーションフレーム更新
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastFrameTime > frameIntervalMs) {
            animFrameIndex = (animFrameIndex + 1) % frameCount
            lastFrameTime = currentTime
        } else if (animFrameIndex >= frameCount) {
            animFrameIndex = 0
        }
        
        // ソース矩形を計算 (ペット種別ごとの特殊レイアウト対応)
        calculateSourceRect(rowIndex, animFrameIndex)
        
        // 描画先の矩形
        val halfSize = petSize / 2
        dstRect.set(
            screenX - halfSize,
            screenY - halfSize,
            screenX + halfSize,
            screenY + halfSize
        )
        
        // 向きに応じてミラーリング
        canvas.save()
        if (!behavior.facingRight) {
            canvas.scale(-1f, 1f, screenX, screenY)
        }
        
        // スプライトを描画
        canvas.drawBitmap(bitmap, srcRect, dstRect, paint)
        canvas.restore()
        
        // 集中モードのオーラ
        if (isFocusing || animController.state == AnimationState.FOCUS) {
            drawFocusAura(canvas, screenX, screenY, petSize)
        }
    }
    
    /**
     * ペットの種別やアニメーション行に応じたソース矩形の計算
     */
    private fun calculateSourceRect(rowIndex: Int, frameIndex: Int) {
        when (currentPetType) {
            PetType.DOG -> {
                // 犬は2x2グリッド。一部のセル(右側)がさらに半分に分かれている
                when (rowIndex) {
                    0 -> srcRect.set(0, 0, cellWidth, cellHeight) // 左上: 立ち
                    1 -> { // 右上: 歩き (2フレーム)
                        val subWidth = cellWidth / 2
                        srcRect.set(cellWidth + frameIndex * subWidth, 0, cellWidth + (frameIndex + 1) * subWidth, cellHeight)
                    }
                    2 -> srcRect.set(0, cellHeight, cellWidth, 2 * cellHeight) // 左下: 寝る
                    3 -> { // 右下: 喜び (2フレーム)
                        val subWidth = cellWidth / 2
                        srcRect.set(cellWidth + frameIndex * subWidth, cellHeight, cellWidth + (frameIndex + 1) * subWidth, 2 * cellHeight)
                    }
                }
            }
            PetType.CAT -> {
                // 猫は4x2グリッド。
                // 1行目 (gridY=0): 0=単一, 1=3フレーム(1〜3セル目)
                // 2行目 (gridY=1): 4=2フレーム(垂直2段の左1列目と想定?), 5=3フレーム(1〜3セル目)
                // 実際の提供画像を見ると 475x259 (犬と同じ)
                // 左上(0): 立ち, 右上(1-3): 歩き
                // 左下(4-?)
                
                when (rowIndex) {
                    0 -> srcRect.set(0, 0, cellWidth, cellHeight) // 単一セル
                    1 -> srcRect.set((1 + frameIndex) * cellWidth, 0, (2 + frameIndex) * cellWidth, cellHeight) // 1行目の2,3,4枚目
                    4 -> { // 2行目の1,2枚目? 
                        // 画像レイアウトに合わせて調整。提供画像では左下は縦に2つ並んでいるように見える
                        // cellHeightが半分なら rows=2 cols=4
                        srcRect.set(0, cellHeight, cellWidth, 2 * cellHeight) // とりあえず1枚目
                    }
                    5 -> srcRect.set((1 + frameIndex) * cellWidth, cellHeight, (2 + frameIndex) * cellWidth, 2 * cellHeight) // 2行目の2,3,4枚目
                }
            }
            PetType.RABBIT -> {
                // ウサギは4x2グリッド、8フレームの歩行アニメーション
                // rowIndex 0 = 全8フレーム (4列x2行)
                val col = frameIndex % 4
                val row = frameIndex / 4
                srcRect.set(
                    col * cellWidth,
                    row * cellHeight,
                    (col + 1) * cellWidth,
                    (row + 1) * cellHeight
                )
            }
            else -> {
                srcRect.set(0, 0, cellWidth, cellHeight)
            }
        }
    }
    
    /**
     * 集中モードのオーラを描画
     */
    private fun drawFocusAura(canvas: Canvas, centerX: Float, centerY: Float, petSize: Float) {
        val time = System.currentTimeMillis()
        for (i in 0 until 3) {
            val phase = ((time / 600.0) + i * 0.33) % 1.0
            val radius = petSize * 0.5f + (phase.toFloat() * petSize * 0.4f)
            val alpha = ((1.0 - phase) * 60).toInt()
            
            paint.color = 0xFF87CEEB.toInt()
            paint.alpha = alpha
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            
            canvas.drawCircle(centerX, centerY, radius, paint)
        }
        
        paint.style = Paint.Style.FILL
        paint.alpha = 255
    }
    
    /**
     * プレースホルダー描画（スプライト未実装時）
     */
    private fun drawPlaceholder(
        canvas: Canvas,
        behavior: PetBehavior,
        animController: AnimationController,
        screenX: Float,
        screenY: Float,
        petSize: Float,
        isFocusing: Boolean = false
    ) {
        // 簡単な円で代用
        paint.color = when (currentPetType) {
            PetType.CAT -> 0xFFE8A87C.toInt()
            PetType.DOG -> 0xFFC4956A.toInt()
            PetType.BIRD -> 0xFFFFD93D.toInt()
            PetType.RABBIT -> 0xFFFFB6C1.toInt() // Light pink for rabbit
            else -> 0xFFCCCCCC.toInt()
        }
        paint.style = Paint.Style.FILL
        
        val radius = petSize * 0.4f
        canvas.drawCircle(screenX, screenY, radius, paint)
        
        // 目
        paint.color = 0xFF222222.toInt()
        val eyeSize = petSize * 0.05f
        val eyeY = screenY - radius * 0.2f
        canvas.drawCircle(screenX - radius * 0.3f, eyeY, eyeSize, paint)
        canvas.drawCircle(screenX + radius * 0.3f, eyeY, eyeSize, paint)
    }
    
    /**
     * リソース解放
     */
    fun release() {
        spriteBitmap?.recycle()
        spriteBitmap = null
    }
    
    /**
     * 白い背景を透明に変換
     * RGB値が閾値以上の白っぽいピクセルを透明にする
     */
    private fun makeWhiteTransparent(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        
        val result = if (source.isMutable) {
            source
        } else {
            source.copy(Bitmap.Config.ARGB_8888, true).also {
                source.recycle()
            }
        }
        
        val pixels = IntArray(width * height)
        result.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val threshold = 250 // 白に近い色の閾値
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val alpha = (pixel shr 24) and 0xFF
            val red = (pixel shr 16) and 0xFF
            val green = (pixel shr 8) and 0xFF
            val blue = pixel and 0xFF
            
            // 白っぽいピクセルを透明に
            if (red >= threshold && green >= threshold && blue >= threshold && alpha > 0) {
                pixels[i] = 0x00000000 // 完全透明
            }
        }
        
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        Log.d(TAG, "White background made transparent")
        return result
    }
}
