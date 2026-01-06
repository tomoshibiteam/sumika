package com.sumika.core.animation

import android.os.SystemClock

/**
 * アニメーションコントローラー
 * 現在のアニメーション状態とフレーム進行を管理
 */
class AnimationController {
    
    private var currentState = AnimationState.IDLE
    private var currentAnimation = DefaultAnimations.IDLE
    private var currentFrame = 0
    private var frameStartTime = 0L
    private var stateStartTime = 0L
    
    /** 現在のアニメーション状態 */
    val state: AnimationState get() = currentState
    
    /** 現在のフレームインデックス */
    val frame: Int get() = currentFrame
    
    /** 状態が開始してからの経過時間（ms） */
    val stateElapsedMs: Long get() = SystemClock.elapsedRealtime() - stateStartTime
    
    /**
     * アニメーション状態を変更
     */
    fun setState(newState: AnimationState) {
        if (currentState == newState) return
        
        currentState = newState
        currentAnimation = DefaultAnimations.ALL[newState] ?: DefaultAnimations.IDLE
        currentFrame = 0
        frameStartTime = SystemClock.elapsedRealtime()
        stateStartTime = frameStartTime
    }
    
    /**
     * フレームを更新（毎フレーム呼び出し）
     * @return 状態が自動遷移した場合true
     */
    fun update(): Boolean {
        val now = SystemClock.elapsedRealtime()
        val elapsed = now - frameStartTime
        
        if (elapsed >= currentAnimation.frameDurationMs) {
            // 次のフレームへ
            currentFrame++
            frameStartTime = now
            
            // アニメーション終了チェック
            if (currentFrame >= currentAnimation.frameCount) {
                if (currentAnimation.loop) {
                    currentFrame = 0
                } else {
                    // ループしないアニメーションは次の状態へ遷移
                    val nextState = currentAnimation.nextState ?: AnimationState.IDLE
                    setState(nextState)
                    return true
                }
            }
        }
        
        return false
    }
    
    /**
     * 特定のアニメーションを1回再生してから元に戻る
     */
    fun playOnce(state: AnimationState) {
        setState(state)
    }
    
    /**
     * 現在のフレーム情報を取得
     */
    fun getFrameInfo(spriteSheet: SpriteSheetDef): FrameInfo {
        return spriteSheet.getFrameInfo(currentState, currentFrame)
    }
}
