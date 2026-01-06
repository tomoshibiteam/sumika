package com.sumika.core.animation

/**
 * アニメーション状態
 */
enum class AnimationState {
    /** 待機 */
    IDLE,
    /** 歩行 */
    WALK,
    /** 走る */
    RUN,
    /** 座る */
    SIT,
    /** 寝る */
    SLEEP,
    /** 食べる */
    EAT,
    /** 喜ぶ（撫でられた） */
    HAPPY,
    /** 遊ぶ */
    PLAY
}

/**
 * アニメーション定義
 * スプライトシート内のフレーム情報を保持
 */
data class AnimationDef(
    val state: AnimationState,
    val frameCount: Int,
    val frameDurationMs: Long,
    val loop: Boolean = true,
    val nextState: AnimationState? = null  // ループしない場合の次の状態
)

/**
 * フレーム情報
 */
data class FrameInfo(
    val x: Int,      // スプライトシート内のX座標
    val y: Int,      // スプライトシート内のY座標
    val width: Int,
    val height: Int
)

/**
 * スプライトシート定義
 * 各ペット種・バリエーション・成長段階ごとに1つ
 */
data class SpriteSheetDef(
    val resourceName: String,
    val frameWidth: Int,
    val frameHeight: Int,
    val columns: Int,
    val animations: Map<AnimationState, AnimationDef>
) {
    /**
     * 特定のアニメーション・フレームの位置を取得
     */
    fun getFrameInfo(state: AnimationState, frameIndex: Int): FrameInfo {
        val animation = animations[state] ?: animations[AnimationState.IDLE]!!
        val safeIndex = frameIndex % animation.frameCount
        
        // 行ごとにアニメーションが配置されている想定
        val row = animations.keys.indexOf(state)
        val col = safeIndex
        
        return FrameInfo(
            x = col * frameWidth,
            y = row * frameHeight,
            width = frameWidth,
            height = frameHeight
        )
    }
}

/**
 * デフォルトのアニメーション定義
 * MVPでは全ペットで共通
 */
object DefaultAnimations {
    val IDLE = AnimationDef(AnimationState.IDLE, frameCount = 4, frameDurationMs = 500, loop = true)
    val WALK = AnimationDef(AnimationState.WALK, frameCount = 6, frameDurationMs = 100, loop = true)
    val SIT = AnimationDef(AnimationState.SIT, frameCount = 2, frameDurationMs = 800, loop = true)
    val SLEEP = AnimationDef(AnimationState.SLEEP, frameCount = 2, frameDurationMs = 1000, loop = true)
    val EAT = AnimationDef(AnimationState.EAT, frameCount = 4, frameDurationMs = 200, loop = false, nextState = AnimationState.IDLE)
    val HAPPY = AnimationDef(AnimationState.HAPPY, frameCount = 4, frameDurationMs = 150, loop = false, nextState = AnimationState.IDLE)
    val PLAY = AnimationDef(AnimationState.PLAY, frameCount = 6, frameDurationMs = 120, loop = false, nextState = AnimationState.IDLE)
    
    val ALL = mapOf(
        AnimationState.IDLE to IDLE,
        AnimationState.WALK to WALK,
        AnimationState.SIT to SIT,
        AnimationState.SLEEP to SLEEP,
        AnimationState.EAT to EAT,
        AnimationState.HAPPY to HAPPY,
        AnimationState.PLAY to PLAY
    )
}
