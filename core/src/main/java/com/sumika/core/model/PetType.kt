package com.sumika.core.model

/**
 * ペットの種類
 */
enum class PetType {
    CAT,  // 猫
    DOG,  // 犬
    BIRD,  // 鳥
    RABBIT  // ウサギ
}

/**
 * 各種類のバリエーション
 */
object PetVariation {
    val CAT_VARIATIONS = listOf("black", "calico", "white")      // 黒猫/三毛/白猫
    val DOG_VARIATIONS = listOf("brown", "black", "white")       // 茶(柴風)/黒/白
    val BIRD_VARIATIONS = listOf("yellow", "blue", "white")      // 黄/青/白
    val RABBIT_VARIATIONS = listOf("white", "brown", "gray")     // 白/茶/灰
    
    fun getVariations(type: PetType): List<String> = when (type) {
        PetType.CAT -> CAT_VARIATIONS
        PetType.DOG -> DOG_VARIATIONS
        PetType.BIRD -> BIRD_VARIATIONS
        PetType.RABBIT -> RABBIT_VARIATIONS
    }
}
