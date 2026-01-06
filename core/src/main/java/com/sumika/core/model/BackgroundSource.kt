package com.sumika.core.model

import android.net.Uri

/**
 * 背景ソース（排他モデル）
 * Uri / Preset / SolidColor のいずれか1つのみ有効
 */
sealed class BackgroundSource {
    /** ユーザーが選択した画像 */
    data class UserImage(val uri: Uri) : BackgroundSource()
    
    /** アプリ内プリセット画像 */
    data class Preset(val name: String) : BackgroundSource()
    
    /** 単色 */
    data class SolidColor(val color: Int) : BackgroundSource()
    
    companion object {
        val DEFAULT = SolidColor(0xFF1A1A2E.toInt())  // ダークブルー
    }
}

/**
 * ぼかしレベル
 */
enum class BlurLevel {
    NONE,
    LIGHT,
    MEDIUM,
    HEAVY
}

/**
 * 壁紙設定
 */
data class WallpaperSettings(
    val backgroundSource: BackgroundSource = BackgroundSource.DEFAULT,
    val blurLevel: BlurLevel = BlurLevel.NONE,
    val brightness: Float = 1.0f,  // 0.0-1.0
    val nestPositionX: Float = 0.85f,  // 0.0-1.0 (画面比率)
    val nestPositionY: Float = 0.85f   // 0.0-1.0 (画面比率)
) {
    init {
        require(brightness in 0f..1f) { "brightness must be 0.0-1.0" }
        require(nestPositionX in 0f..1f) { "nestPositionX must be 0.0-1.0" }
        require(nestPositionY in 0f..1f) { "nestPositionY must be 0.0-1.0" }
    }
}
