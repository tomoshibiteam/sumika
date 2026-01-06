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
import com.sumika.core.model.PetType

/**
 * ペットのスプライト描画
 */
class PetRenderer(private val context: Context) {
    
    companion object {
        private const val TAG = "PetRenderer"
        private const val PET_SIZE_RATIO = 0.15f  // 画面幅に対するペットサイズ
    }
    
    private var spriteBitmap: Bitmap? = null
    private var currentPetType: PetType? = null
    private var currentVariation: Int = -1
    
    private val paint = Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
    }
    
    private val srcRect = Rect()
    private val dstRect = RectF()
    private val matrix = Matrix()
    
    // プレースホルダー用
    private val placeholderPaint = Paint().apply {
        isAntiAlias = true
    }
    
    /**
     * スプライトを読み込み
     */
    fun loadSprite(petType: PetType, variation: Int) {
        if (petType == currentPetType && variation == currentVariation) return
        
        // 既存のビットマップを解放
        spriteBitmap?.recycle()
        spriteBitmap = null
        
        currentPetType = petType
        currentVariation = variation
        
        // TODO: 実際のスプライトシート読み込み
        // val resourceName = "sprite_${petType.name.lowercase()}_$variation"
        // spriteBitmap = BitmapFactory.decodeResource(context.resources, resourceId)
        
        Log.d(TAG, "Sprite loaded: $petType variation=$variation")
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
        screenHeight: Int
    ) {
        val petSize = screenWidth * PET_SIZE_RATIO
        
        // スプライトがない場合はプレースホルダーを描画
        if (spriteBitmap == null) {
            drawPlaceholder(canvas, behavior, animController, screenX, screenY, petSize)
            return
        }
        
        // スプライト描画
        // TODO: スプライトシートから該当フレームを切り出して描画
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
        petSize: Float
    ) {
        // 状態に応じた色
        val baseColor = when (currentPetType) {
            PetType.CAT -> when (currentVariation) {
                0 -> 0xFF2D2D2D.toInt()  // 黒猫
                1 -> 0xFFE8A87C.toInt()  // 三毛
                else -> 0xFFF5F5F5.toInt()  // 白猫
            }
            PetType.DOG -> when (currentVariation) {
                0 -> 0xFFC4956A.toInt()  // 茶
                1 -> 0xFF3D3D3D.toInt()  // 黒
                else -> 0xFFF0F0F0.toInt()  // 白
            }
            PetType.BIRD -> when (currentVariation) {
                0 -> 0xFFFFD93D.toInt()  // 黄
                1 -> 0xFF6EC6FF.toInt()  // 青
                else -> 0xFFFAFAFA.toInt()  // 白
            }
            else -> 0xFFCCCCCC.toInt()
        }
        
        // 本体（円）
        placeholderPaint.color = baseColor
        val bodyRadius = petSize * 0.4f
        canvas.drawCircle(screenX, screenY, bodyRadius, placeholderPaint)
        
        // 頭（小さい円）
        val headRadius = petSize * 0.25f
        val headY = screenY - bodyRadius * 0.6f
        canvas.drawCircle(screenX, headY, headRadius, placeholderPaint)
        
        // 目
        placeholderPaint.color = 0xFF222222.toInt()
        val eyeRadius = petSize * 0.04f
        val eyeY = headY - headRadius * 0.1f
        val eyeOffset = headRadius * 0.35f
        
        // 寝ている時は目を閉じる
        if (animController.state == AnimationState.SLEEP) {
            placeholderPaint.strokeWidth = eyeRadius * 0.8f
            placeholderPaint.style = Paint.Style.STROKE
            canvas.drawLine(
                screenX - eyeOffset - eyeRadius,
                eyeY,
                screenX - eyeOffset + eyeRadius,
                eyeY,
                placeholderPaint
            )
            canvas.drawLine(
                screenX + eyeOffset - eyeRadius,
                eyeY,
                screenX + eyeOffset + eyeRadius,
                eyeY,
                placeholderPaint
            )
            placeholderPaint.style = Paint.Style.FILL
        } else {
            // 向きに応じて目の位置を調整
            val lookOffset = if (behavior.facingRight) eyeRadius * 0.5f else -eyeRadius * 0.5f
            canvas.drawCircle(screenX - eyeOffset + lookOffset, eyeY, eyeRadius, placeholderPaint)
            canvas.drawCircle(screenX + eyeOffset + lookOffset, eyeY, eyeRadius, placeholderPaint)
        }
        
        // 口/表情
        when (animController.state) {
            AnimationState.HAPPY -> {
                // 笑顔
                placeholderPaint.style = Paint.Style.STROKE
                placeholderPaint.strokeWidth = eyeRadius * 0.6f
                val smileY = headY + headRadius * 0.3f
                canvas.drawArc(
                    screenX - headRadius * 0.3f,
                    smileY - headRadius * 0.2f,
                    screenX + headRadius * 0.3f,
                    smileY + headRadius * 0.2f,
                    0f, 180f, false, placeholderPaint
                )
                placeholderPaint.style = Paint.Style.FILL
            }
            AnimationState.EAT -> {
                // 口を開ける
                canvas.drawCircle(
                    screenX,
                    headY + headRadius * 0.35f,
                    petSize * 0.05f,
                    placeholderPaint
                )
            }
            else -> {
                // 普通の口
                val mouthY = headY + headRadius * 0.35f
                canvas.drawCircle(screenX, mouthY, eyeRadius * 0.6f, placeholderPaint)
            }
        }
        
        // 耳（猫/犬のみ）
        if (currentPetType == PetType.CAT || currentPetType == PetType.DOG) {
            placeholderPaint.color = baseColor
            val earSize = petSize * 0.12f
            val earY = headY - headRadius * 0.7f
            
            if (currentPetType == PetType.CAT) {
                // 三角の耳
                val path = android.graphics.Path()
                path.moveTo(screenX - headRadius * 0.5f, earY + earSize)
                path.lineTo(screenX - headRadius * 0.3f, earY - earSize)
                path.lineTo(screenX - headRadius * 0.1f, earY + earSize)
                path.close()
                canvas.drawPath(path, placeholderPaint)
                
                path.reset()
                path.moveTo(screenX + headRadius * 0.1f, earY + earSize)
                path.lineTo(screenX + headRadius * 0.3f, earY - earSize)
                path.lineTo(screenX + headRadius * 0.5f, earY + earSize)
                path.close()
                canvas.drawPath(path, placeholderPaint)
            } else {
                // 丸い耳
                canvas.drawCircle(screenX - headRadius * 0.6f, earY, earSize, placeholderPaint)
                canvas.drawCircle(screenX + headRadius * 0.6f, earY, earSize, placeholderPaint)
            }
        }
        
        // 鳥の場合はくちばし
        if (currentPetType == PetType.BIRD) {
            placeholderPaint.color = 0xFFFF9800.toInt()
            val beakSize = petSize * 0.08f
            val beakX = if (behavior.facingRight) screenX + headRadius * 0.7f else screenX - headRadius * 0.7f
            val beakY = headY + headRadius * 0.1f
            
            val path = android.graphics.Path()
            path.moveTo(beakX, beakY)
            if (behavior.facingRight) {
                path.lineTo(beakX + beakSize, beakY + beakSize * 0.3f)
                path.lineTo(beakX, beakY + beakSize * 0.6f)
            } else {
                path.lineTo(beakX - beakSize, beakY + beakSize * 0.3f)
                path.lineTo(beakX, beakY + beakSize * 0.6f)
            }
            path.close()
            canvas.drawPath(path, placeholderPaint)
        }
    }
    
    /**
     * リソース解放
     */
    fun release() {
        spriteBitmap?.recycle()
        spriteBitmap = null
    }
}
