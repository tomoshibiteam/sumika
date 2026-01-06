package com.sumika.core.model

/**
 * 動作ステートマシン
 * ペットの現在の動作状態を表す
 */
enum class MotionState {
    /** 待機中 */
    IDLE,
    
    /** 歩いている */
    WALKING,
    
    /** 寝ている */
    SLEEPING,
    
    /** 餌を食べている */
    EATING,
    
    /** 遊んでいる */
    PLAYING,
    
    /** 喜んでいる（撫でられた等） */
    HAPPY,
    
    /** 寝床へ向かっている */
    GOING_TO_NEST
}
