package com.sumika.wallpaper.engine

import android.util.Log

/**
 * ホーム画面のページオフセット管理
 * ランチャーのページ移動による壁紙オフセットを処理
 * 
 * 座標系:
 * - ワールド座標: 0.0-1.0 の正規化座標（ペット/寝床の論理位置）
 * - スクリーン座標: ピクセル座標（実際の描画位置）
 */
class OffsetManager {
    
    companion object {
        private const val TAG = "OffsetManager"
        
        /**
         * パララックス強度
         * 0.0 = パララックス無効（固定表示）
         * 1.0 = フルパララックス（壁紙と同じ動き）
         * 0.1-0.3 = 軽微なパララックス（推奨）
         */
        private const val PARALLAX_STRENGTH = 0.15f
    }
    
    /** X軸オフセット (0.0-1.0): 0=左端ページ, 1=右端ページ */
    var xOffset = 0.5f
        private set
    
    /** Y軸オフセット (通常は0.5固定) */
    var yOffset = 0.5f
        private set
    
    /** Xオフセットのステップ（ページ数の逆数） */
    var xOffsetStep = 0f
        private set
    
    /** Yオフセットのステップ */
    var yOffsetStep = 0f
        private set
    
    /** 総ページ数（推定） */
    val estimatedPageCount: Int
        get() = if (xOffsetStep > 0f) (1f / xOffsetStep).toInt() + 1 else 1
    
    /**
     * オフセット変更時に呼び出し
     */
    fun onOffsetsChanged(
        xOffset: Float,
        yOffset: Float,
        xOffsetStep: Float,
        yOffsetStep: Float
    ) {
        val changed = this.xOffset != xOffset || this.yOffset != yOffset
        
        this.xOffset = xOffset
        this.yOffset = yOffset
        this.xOffsetStep = xOffsetStep
        this.yOffsetStep = yOffsetStep
        
        if (changed) {
            Log.d(TAG, "Offset changed: x=$xOffset (step=$xOffsetStep), pages≈$estimatedPageCount")
        }
    }
    
    /**
     * ワールド座標（0.0-1.0）をスクリーンX座標に変換
     * 
     * パララックス効果:
     * - xOffset=0.5（中央ページ）の時、ワールド座標そのまま
     * - xOffset=0（左端）の時、右にシフト
     * - xOffset=1（右端）の時、左にシフト
     * 
     * @param worldX ワールドX座標 (0.0-1.0)
     * @param screenWidth スクリーン幅 (px)
     * @return スクリーンX座標 (px)
     */
    fun toScreenX(worldX: Float, screenWidth: Int): Float {
        // パララックスオフセット計算
        // xOffset=0.5を中心として、左右にずれる量を計算
        val parallaxOffset = (xOffset - 0.5f) * PARALLAX_STRENGTH * screenWidth
        return worldX * screenWidth - parallaxOffset
    }
    
    /**
     * ワールド座標（0.0-1.0）をスクリーンY座標に変換
     * Y軸はパララックスを適用しない（縦スクロールするランチャーは稀）
     * 
     * @param worldY ワールドY座標 (0.0-1.0)
     * @param screenHeight スクリーン高さ (px)
     * @return スクリーンY座標 (px)
     */
    fun toScreenY(worldY: Float, screenHeight: Int): Float {
        return worldY * screenHeight
    }
    
    /**
     * スクリーン座標をワールド座標に変換（タッチ座標用）
     */
    fun toWorldX(screenX: Float, screenWidth: Int): Float {
        val parallaxOffset = (xOffset - 0.5f) * PARALLAX_STRENGTH * screenWidth
        return (screenX + parallaxOffset) / screenWidth
    }
    
    fun toWorldY(screenY: Float, screenHeight: Int): Float {
        return screenY / screenHeight
    }
}
