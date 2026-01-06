package com.sumika.wallpaper.engine

/**
 * ホーム画面のページオフセット管理
 * ランチャーのページ移動による壁紙オフセットを処理
 */
class OffsetManager {
    
    /** X軸オフセット (0.0-1.0) */
    var xOffset = 0.5f
        private set
    
    /** Y軸オフセット (0.0-1.0) */
    var yOffset = 0.5f
        private set
    
    /** Xオフセットのステップ */
    var xOffsetStep = 0f
        private set
    
    /** Yオフセットのステップ */
    var yOffsetStep = 0f
        private set
    
    /**
     * オフセット変更時に呼び出し
     */
    fun onOffsetsChanged(
        xOffset: Float,
        yOffset: Float,
        xOffsetStep: Float,
        yOffsetStep: Float
    ) {
        this.xOffset = xOffset
        this.yOffset = yOffset
        this.xOffsetStep = xOffsetStep
        this.yOffsetStep = yOffsetStep
    }
    
    /**
     * ワールド座標（0.0-1.0）をスクリーン座標に変換
     * 
     * @param worldX ワールドX座標 (0.0-1.0)
     * @param screenWidth スクリーン幅 (px)
     * @return スクリーンX座標 (px)
     */
    fun toScreenX(worldX: Float, screenWidth: Int): Float {
        // パララックス効果を適用
        // xOffset=0.5の時はそのまま、0や1の時は位置がずれる
        val parallaxStrength = 0.1f  // パララックスの強さ（0=なし、1=フル）
        val offsetAdjustment = (xOffset - 0.5f) * parallaxStrength * screenWidth
        return worldX * screenWidth - offsetAdjustment
    }
    
    /**
     * ワールド座標（0.0-1.0）をスクリーン座標に変換
     * 
     * @param worldY ワールドY座標 (0.0-1.0)
     * @param screenHeight スクリーン高さ (px)
     * @return スクリーンY座標 (px)
     */
    fun toScreenY(worldY: Float, screenHeight: Int): Float {
        // Y軸はパララックスを適用しない（任意で追加可能）
        return worldY * screenHeight
    }
}
