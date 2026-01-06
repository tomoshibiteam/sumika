package com.sumika.core.model

/**
 * 長期的な性格パラメータ（永続化）
 * ユーザーとのインタラクションで徐々に変化する
 * 
 * 全ての値は 0.0〜1.0 の範囲
 */
data class Personality(
    /** 活発さ傾向: 0=おとなしい, 1=活発 */
    val energy: Float = 0.5f,
    
    /** 落ち着き傾向: 0=落ち着きがない, 1=穏やか */
    val calmness: Float = 0.5f,
    
    /** 遊び好き傾向: 0=無関心, 1=遊び好き */
    val playfulness: Float = 0.5f,
    
    /** 人懐っこさ傾向: 0=人見知り, 1=社交的 */
    val sociability: Float = 0.5f,
    
    /** 規則正しさ傾向: 0=気まぐれ, 1=規則正しい */
    val routine: Float = 0.5f
) {
    init {
        require(energy in 0f..1f) { "energy must be 0.0-1.0" }
        require(calmness in 0f..1f) { "calmness must be 0.0-1.0" }
        require(playfulness in 0f..1f) { "playfulness must be 0.0-1.0" }
        require(sociability in 0f..1f) { "sociability must be 0.0-1.0" }
        require(routine in 0f..1f) { "routine must be 0.0-1.0" }
    }
}
