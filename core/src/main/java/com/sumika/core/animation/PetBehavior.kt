package com.sumika.core.animation

import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * ペットの行動制御
 * 移動、アクション、自律行動を管理
 */
class PetBehavior(
    private val animationController: AnimationController
) {
    companion object {
        private const val MOVE_SPEED = 0.15f  // 画面幅/秒
        private const val ARRIVAL_THRESHOLD = 0.02f
        private const val IDLE_ACTION_INTERVAL_MIN = 3000L  // 3秒
        private const val IDLE_ACTION_INTERVAL_MAX = 8000L  // 8秒
    }
    
    // 位置（ワールド座標 0.0-1.0）
    var posX = 0.5f
        private set
    var posY = 0.7f
        private set
    
    // 目標位置（移動中のみ有効）
    private var targetX: Float? = null
    private var targetY: Float? = null
    
    // 向き（true = 右向き）
    var facingRight = true
        private set
    
    // 自律行動タイマー
    private var nextIdleActionTime = 0L
    
    /**
     * 更新（毎フレーム呼び出し）
     * @return アニメーションが自動遷移した場合true
     */
    fun update(dt: Float, currentTimeMs: Long): Boolean {
        // 移動処理
        updateMovement(dt)
        
        // アニメーション更新
        val transitioned = animationController.update()
        
        // 自律行動（IDLE時のみ）
        if (animationController.state == AnimationState.IDLE) {
            checkIdleAction(currentTimeMs)
        }
        
        return transitioned
    }
    
    /**
     * 目標位置へ移動開始
     */
    fun moveTo(x: Float, y: Float) {
        targetX = x.coerceIn(0.1f, 0.9f)
        targetY = y.coerceIn(0.3f, 0.9f)
        
        // 向きを更新
        targetX?.let { tx ->
            if (abs(tx - posX) > 0.01f) {
                facingRight = tx > posX
            }
        }
        
        animationController.setState(AnimationState.WALK)
    }
    
    /**
     * 撫でられた
     */
    fun onPet() {
        cancelMovement()
        animationController.playOnce(AnimationState.HAPPY)
    }
    
    /**
     * 餌をもらった
     */
    fun onFeed() {
        cancelMovement()
        animationController.playOnce(AnimationState.EAT)
    }
    
    /**
     * 遊ぶ
     */
    fun onPlay() {
        cancelMovement()
        animationController.playOnce(AnimationState.PLAY)
    }
    
    /**
     * 寝る
     */
    fun sleep() {
        cancelMovement()
        animationController.setState(AnimationState.SLEEP)
    }
    
    /**
     * 起きる
     */
    fun wakeUp() {
        if (animationController.state == AnimationState.SLEEP) {
            animationController.setState(AnimationState.IDLE)
        }
    }
    
    private fun updateMovement(dt: Float) {
        val tx = targetX ?: return
        val ty = targetY ?: return
        
        val dx = tx - posX
        val dy = ty - posY
        val distance = sqrt(dx * dx + dy * dy)
        
        if (distance < ARRIVAL_THRESHOLD) {
            // 到着
            posX = tx
            posY = ty
            cancelMovement()
            animationController.setState(AnimationState.IDLE)
            return
        }
        
        // 移動
        val moveAmount = MOVE_SPEED * dt
        val ratio = (moveAmount / distance).coerceAtMost(1f)
        posX += dx * ratio
        posY += dy * ratio
    }
    
    private fun cancelMovement() {
        targetX = null
        targetY = null
    }
    
    private fun checkIdleAction(currentTimeMs: Long) {
        if (currentTimeMs < nextIdleActionTime) return
        
        // 次のアクション時間を設定
        nextIdleActionTime = currentTimeMs + Random.nextLong(
            IDLE_ACTION_INTERVAL_MIN,
            IDLE_ACTION_INTERVAL_MAX
        )
        
        // ランダムな行動
        when (Random.nextInt(10)) {
            0, 1, 2 -> {
                // 少し移動
                val newX = posX + Random.nextFloat() * 0.2f - 0.1f
                val newY = posY + Random.nextFloat() * 0.1f - 0.05f
                moveTo(newX, newY)
            }
            3 -> {
                // 座る
                animationController.setState(AnimationState.SIT)
            }
            // それ以外はIDLEを継続
        }
    }
    
    /**
     * 位置を直接設定（初期化用）
     */
    fun setPosition(x: Float, y: Float) {
        posX = x.coerceIn(0.1f, 0.9f)
        posY = y.coerceIn(0.3f, 0.9f)
    }
}
