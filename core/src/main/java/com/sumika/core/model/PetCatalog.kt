package com.sumika.core.model

/**
 * ペットカタログエントリー
 */
data class PetCatalogEntry(
    val id: String, // "cat_0", "dog_1", etc.
    val type: PetType,
    val variation: Int,
    val defaultName: String,
    val defaultPersonality: Personality,
    val description: String,
    val personalityLabels: List<String>, // ["活発", "穏やか", "遊び好き"]
    val isFree: Boolean, // 初回選択可能な無料ペット
    val isProOnly: Boolean, // Proプラン限定フラグ
    val imageResName: String // 画像リソース名 (drawable)
)

/**
 * 全ペットのカタログ
 */
object PetCatalog {
    
    /**
     * 全18種類のペット
     */
    val ALL_PETS = listOf(
        // === 猫（9種類） ===
        PetCatalogEntry(
            id = "cat_0",
            type = PetType.CAT,
            variation = 0,
            defaultName = "クロ",
            defaultPersonality = Personality(
                energy = 0.75f,
                calmness = 0.4f,
                playfulness = 0.8f,
                sociability = 0.6f,
                routine = 0.4f
            ),
            description = "活発で遊び好きな黒猫。やや気まぐれで自由奔放。夜になると特に元気になります。",
            personalityLabels = listOf("⚡活発", "🎾遊び好き", "🌙気まぐれ"),
            isFree = true,
            isProOnly = false,imageResName = "pet_cat_0"
        ),
        PetCatalogEntry(
            id = "cat_1",
            type = PetType.CAT,
            variation = 1,
            defaultName = "ミケ",
            defaultPersonality = Personality(
                energy = 0.5f,
                calmness = 0.7f,
                playfulness = 0.6f,
                sociability = 0.8f,
                routine = 0.6f
            ),
            description = "落ち着いていて社交的な三毛猫。バランスの取れた性格で、誰とでも仲良くなれます。",
            personalityLabels = listOf("😌穏やか", "👥社交的", "⚖️バランス型"),
            isFree = false,
            isProOnly = true,imageResName = "pet_cat_1"
        ),
        PetCatalogEntry(
            id = "cat_2",
            type = PetType.CAT,
            variation = 2,
            defaultName = "シロ",
            defaultPersonality = Personality(
                energy = 0.3f,
                calmness = 0.85f,
                playfulness = 0.4f,
                sociability = 0.3f,
                routine = 0.75f
            ),
            description = "穏やかで優雅な白猫。やや人見知りですが、一度心を開くと甘えん坊になります。",
            personalityLabels = listOf("✨優雅", "😌穏やか", "🤫人見知り"),
            isFree = false,
            isProOnly = true,imageResName = "pet_cat_2"
        ),
        PetCatalogEntry(
            id = "cat_3",
            type = PetType.CAT,
            variation = 0, // 色違い扱い
            defaultName = "トラ",
            defaultPersonality = Personality(
                energy = 0.85f,
                calmness = 0.3f,
                playfulness = 0.9f,
                sociability = 0.7f,
                routine = 0.3f
            ),
            description = "活発でやんちゃなトラ猫。冒険が大好きで、いつも新しいことを探しています。",
            personalityLabels = listOf("⚡活発", "🗺️冒険好き", "🎾やんちゃ"),
            isFree = false,
            isProOnly = true,imageResName = "pet_cat_3"
        ),
        PetCatalogEntry(
            id = "cat_4",
            type = PetType.CAT,
            variation = 1, // 色違い
            defaultName = "グレイ",
            defaultPersonality = Personality(
                energy = 0.4f,
                calmness = 0.9f,
                playfulness = 0.5f,
                sociability = 0.5f,
                routine = 0.85f
            ),
            description = "知的で落ち着いたロシアンブルー。規則正しい生活を好み、静かな環境が大好きです。",
            personalityLabels = listOf("🧠知的", "😌穏やか", "📅規則正しい"),
            isFree = false,
            isProOnly = true,imageResName = "pet_cat_4"
        ),
        PetCatalogEntry(
            id = "cat_5",
            type = PetType.CAT,
            variation = 2, // 色違い
            defaultName = "チャチャ",
            defaultPersonality = Personality(
                energy = 0.7f,
                calmness = 0.5f,
                playfulness = 0.85f,
                sociability = 0.9f,
                routine = 0.5f
            ),
            description = "フレンドリーな茶トラ。社交的で遊び好き、誰とでもすぐに仲良くなれます。",
            personalityLabels = listOf("👥社交的", "🎾遊び好き", "😊フレンドリー"),
            isFree = false,
            isProOnly = true,imageResName = "pet_cat_5"
        ),
        PetCatalogEntry(
            id = "cat_6",
            type = PetType.CAT,
            variation = 0, // さらに色違い
            defaultName = "サバ",
            defaultPersonality = Personality(
                energy = 0.6f,
                calmness = 0.6f,
                playfulness = 0.75f,
                sociability = 0.4f,
                routine = 0.5f
            ),
            description = "サバトラ模様の猫。やや人見知りですが、遊ぶのは大好きです。",
            personalityLabels = listOf("🎾遊び好き", "🤫人見知り", "🐈気まぐれ"),
            isFree = false,
            isProOnly = true,imageResName = "pet_cat_6"
        ),
        PetCatalogEntry(
            id = "cat_7",
            type = PetType.CAT,
            variation = 1, // さらに色違い
            defaultName = "ハチ",
            defaultPersonality = Personality(
                energy = 0.6f,
                calmness = 0.6f,
                playfulness = 0.6f,
                sociability = 0.6f,
                routine = 0.6f
            ),
            description = "ハチワレ模様の猫。バランスの取れたオールラウンダーで、どんな環境にも適応できます。",
            personalityLabels = listOf("⚖️バランス型", "🌟オールラウンド", "😊優しい"),
            isFree = false,
            isProOnly = true,imageResName = "pet_cat_7"
        ),
        PetCatalogEntry(
            id = "cat_8",
            type = PetType.CAT,
            variation = 2, // さらに色違い
            defaultName = "シャム",
            defaultPersonality = Personality(
                energy = 0.8f,
                calmness = 0.4f,
                playfulness = 0.7f,
                sociability = 0.95f,
                routine = 0.5f
            ),
            description = "おしゃべり好きなシャム猫。とても社交的で活発、いつも何か話しかけてきます。",
            personalityLabels = listOf("💬おしゃべり", "👥社交的", "⚡活発"),
            isFree = false,
            isProOnly = true,imageResName = "pet_cat_8"
        ),
        
        // === 犬（9種類） ===
        PetCatalogEntry(
            id = "dog_0",
            type = PetType.DOG,
            variation = 0,
            defaultName = "ハチ",
            defaultPersonality = Personality(
                energy = 0.7f,
                calmness = 0.6f,
                playfulness = 0.7f,
                sociability = 0.8f,
                routine = 0.85f
            ),
            description = "忠実で規則正しい柴犬。活発ながらも落ち着きがあり、理想的な家族の一員です。",
            personalityLabels = listOf("❤️忠実", "📅規則正しい", "⚡活発"),
            isFree = true,
            isProOnly = false,imageResName = "pet_dog_0"
        ),
        PetCatalogEntry(
            id = "dog_1",
            type = PetType.DOG,
            variation = 1,
            defaultName = "クロ",
            defaultPersonality = Personality(
                energy = 0.5f,
                calmness = 0.85f,
                playfulness = 0.5f,
                sociability = 0.6f,
                routine = 0.8f
            ),
            description = "賢くて穏やかな黒柴。落ち着いた性格で、静かな環境を好みます。",
            personalityLabels = listOf("🧠賢い", "😌穏やか", "🤫落ち着き"),
            isFree = false,
            isProOnly = true,imageResName = "pet_dog_1"
        ),
        PetCatalogEntry(
            id = "dog_2",
            type = PetType.DOG,
            variation = 2,
            defaultName = "ユキ",
            defaultPersonality = Personality(
                energy = 0.75f,
                calmness = 0.5f,
                playfulness = 0.85f,
                sociability = 0.9f,
                routine = 0.6f
            ),
            description = "甘えん坊な白柴。社交的で遊び好き、いつも一緒にいたがります。",
            personalityLabels = listOf("💕甘えん坊", "👥社交的", "🎾遊び好き"),
            isFree = false,
            isProOnly = true,imageResName = "pet_dog_2"
        ),
        PetCatalogEntry(
            id = "dog_3",
            type = PetType.DOG,
            variation = 0, // 色違い
            defaultName = "ゴールド",
            defaultPersonality = Personality(
                energy = 0.7f,
                calmness = 0.7f,
                playfulness = 0.8f,
                sociability = 1.0f,
                routine = 0.7f
            ),
            description = "優しくて社交的なゴールデンレトリバー。誰とでも仲良くなれる温厚な性格です。",
            personalityLabels = listOf("😊優しい", "👥超社交的", "💛温厚"),
            isFree = false,
            isProOnly = true,imageResName = "pet_dog_3"
        ),
        PetCatalogEntry(
            id = "dog_4",
            type = PetType.DOG,
            variation = 1, // 色違い
            defaultName = "ボーダー",
            defaultPersonality = Personality(
                energy = 0.95f,
                calmness = 0.5f,
                playfulness = 0.8f,
                sociability = 0.7f,
                routine = 0.75f
            ),
            description = "エネルギッシュで賢いボーダーコリー。運動が大好きで、いつもアクティブです。",
            personalityLabels = listOf("⚡エネルギッシュ", "🧠賢い", "🏃活動的"),
            isFree = false,
            isProOnly = true,imageResName = "pet_dog_4"
        ),
        PetCatalogEntry(
            id = "dog_5",
            type = PetType.DOG,
            variation = 2, // 色違い
            defaultName = "ダックス",
            defaultPersonality = Personality(
                energy = 0.7f,
                calmness = 0.5f,
                playfulness = 0.9f,
                sociability = 0.7f,
                routine = 0.4f
            ),
            description = "好奇心旺盛なダックスフンド。遊び好きで、いつも新しいものを探しています。",
            personalityLabels = listOf("🔍好奇心旺盛", "🎾遊び好き", "😄元気"),
            isFree = false,
            isProOnly = true,imageResName = "pet_dog_5"
        ),
        PetCatalogEntry(
            id = "dog_6",
            type = PetType.DOG,
            variation = 0, // さらに色違い
            defaultName = "ポメ",
            defaultPersonality = Personality(
                energy = 0.85f,
                calmness = 0.4f,
                playfulness = 0.8f,
                sociability = 0.85f,
                routine = 0.5f
            ),
            description = "元気いっぱいのポメラニアン。小さくても活発で、社交的な性格です。",
            personalityLabels = listOf("⚡元気", "👥社交的", "🐶活発"),
            isFree = false,
            isProOnly = true,imageResName = "pet_dog_6"
        ),
        PetCatalogEntry(
            id = "dog_7",
            type = PetType.DOG,
            variation = 1, // さらに色違い
            defaultName = "フレンチ",
            defaultPersonality = Personality(
                energy = 0.4f,
                calmness = 0.8f,
                playfulness = 0.6f,
                sociability = 0.8f,
                routine = 0.6f
            ),
            description = "のんびり屋のフレンチブルドッグ。穏やかで社交的、マイペースな性格です。",
            personalityLabels = listOf("😌のんびり", "👥社交的", "🐢マイペース"),
            isFree = false,
            isProOnly = true,imageResName = "pet_dog_7"
        ),
        PetCatalogEntry(
            id = "dog_8",
            type = PetType.DOG,
            variation = 2, // さらに色違い
            defaultName = "シェパ",
            defaultPersonality = Personality(
                energy = 0.7f,
                calmness = 0.75f,
                playfulness = 0.6f,
                sociability = 0.7f,
                routine = 0.95f
            ),
            description = "頼れるジャーマンシェパード。賢くて規則正しく、とても信頼できる相棒です。",
            personalityLabels = listOf("🧠賢い", "📅規則正しい", "💪頼れる"),
            isFree = false,
            isProOnly = true,imageResName = "pet_dog_8"
        ),
        
        // === 鳥（9種類） ===
        PetCatalogEntry(
            id = "bird_0",
            type = PetType.BIRD,
            variation = 0,
            defaultName = "ピーちゃん",
            defaultPersonality = Personality(
                energy = 0.75f,
                calmness = 0.5f,
                playfulness = 0.8f,
                sociability = 0.9f,
                routine = 0.6f
            ),
            description = "明るくておしゃべり好きな黄色いセキセイインコ。社交的で、いつも楽しい雰囲気を作ります。",
            personalityLabels = listOf("💬おしゃべり", "👥社交的", "☀️明るい"),
            isFree = true,
            isProOnly = false,imageResName = "pet_bird_0"
        ),
        PetCatalogEntry(
            id = "bird_1",
            type = PetType.BIRD,
            variation = 1,
            defaultName = "アオ",
            defaultPersonality = Personality(
                energy = 0.5f,
                calmness = 0.8f,
                playfulness = 0.6f,
                sociability = 0.6f,
                routine = 0.85f
            ),
            description = "穏やかで規則正しい青いセキセイインコ。落ち着いた性格で、静かな生活を好みます。",
            personalityLabels = listOf("😌穏やか", "📅規則正しい", "🌊落ち着き"),
            isFree = false,
            isProOnly = true,imageResName = "pet_bird_1"
        ),
        PetCatalogEntry(
            id = "bird_2",
            type = PetType.BIRD,
            variation = 2,
            defaultName = "シロ",
            defaultPersonality = Personality(
                energy = 0.4f,
                calmness = 0.9f,
                playfulness = 0.5f,
                sociability = 0.5f,
                routine = 0.8f
            ),
            description = "優雅で落ち着いた白いセキセイインコ。上品な雰囲気を持つ美しい鳥です。",
            personalityLabels = listOf("✨優雅", "😌穏やか", "🕊️上品"),
            isFree = false,
            isProOnly = true,imageResName = "pet_bird_2"
        ),
        PetCatalogEntry(
            id = "bird_3",
            type = PetType.BIRD,
            variation = 0, // 色違い
            defaultName = "オカメ",
            defaultPersonality = Personality(
                energy = 0.6f,
                calmness = 0.6f,
                playfulness = 0.7f,
                sociability = 0.85f,
                routine = 0.6f
            ),
            description = "甘えん坊なオカメインコ。社交的で、いつも一緒にいたがる可愛い性格です。",
            personalityLabels = listOf("💕甘えん坊", "👥社交的", "😊可愛い"),
            isFree = false,
            isProOnly = true,imageResName = "pet_bird_3"
        ),
        PetCatalogEntry(
            id = "bird_4",
            type = PetType.BIRD,
            variation = 1, // 色違い
            defaultName = "ブンタ",
            defaultPersonality = Personality(
                energy = 0.8f,
                calmness = 0.5f,
                playfulness = 0.75f,
                sociability = 0.7f,
                routine = 0.6f
            ),
            description = "小さくて活発な文鳥。元気いっぱいで、いつも飛び回っています。",
            personalityLabels = listOf("⚡活発", "🐦小さい", "😄元気"),
            isFree = false,
            isProOnly = true,imageResName = "pet_bird_4"
        ),
        PetCatalogEntry(
            id = "bird_5",
            type = PetType.BIRD,
            variation = 2, // 色違い
            defaultName = "カナリア",
            defaultPersonality = Personality(
                energy = 0.5f,
                calmness = 0.85f,
                playfulness = 0.5f,
                sociability = 0.6f,
                routine = 0.75f
            ),
            description = "美しい声を持つカナリア。穏やかで、その歌声で癒しを与えてくれます。",
            personalityLabels = listOf("🎵美しい声", "😌穏やか", "✨優雅"),
            isFree = false,
            isProOnly = true,imageResName = "pet_bird_5"
        ),
        PetCatalogEntry(
            id = "bird_6",
            type = PetType.BIRD,
            variation = 0, // さらに色違い
            defaultName = "ミドリ",
            defaultPersonality = Personality(
                energy = 0.7f,
                calmness = 0.5f,
                playfulness = 0.9f,
                sociability = 0.7f,
                routine = 0.5f
            ),
            description = "好奇心旺盛な緑のインコ。遊び好きで、いつも新しいことに興味津々です。",
            personalityLabels = listOf("🔍好奇心旺盛", "🎾遊び好き", "🌿元気"),
            isFree = false,
            isProOnly = true,imageResName = "pet_bird_6"
        ),
        PetCatalogEntry(
            id = "bird_7",
            type = PetType.BIRD,
            variation = 1, // さらに色違い
            defaultName = "アカ",
            defaultPersonality = Personality(
                energy = 0.9f,
                calmness = 0.4f,
                playfulness = 0.85f,
                sociability = 0.75f,
                routine = 0.5f
            ),
            description = "エネルギッシュな赤いインコ。活発で、いつも飛び回っています。",
            personalityLabels = listOf("⚡エネルギッシュ", "🔥活発", "😄元気"),
            isFree = false,
            isProOnly = true,imageResName = "pet_bird_7"
        ),
        PetCatalogEntry(
            id = "bird_8",
            type = PetType.BIRD,
            variation = 2, // さらに色違い
            defaultName = "フクロウ",
            defaultPersonality = Personality(
                energy = 0.3f,
                calmness = 0.95f,
                playfulness = 0.4f,
                sociability = 0.4f,
                routine = 0.9f
            ),
            description = "知的で落ち着いたフクロウ。夜型で、静かな環境を好む賢い鳥です。",
            personalityLabels = listOf("🧠知的", "🌙夜型", "😌穏やか"),
            isFree = false,
            isProOnly = true,imageResName = "pet_bird_8"
        ),
        
        // === ウサギ（3種類） ===
        PetCatalogEntry(
            id = "rabbit_0",
            type = PetType.RABBIT,
            variation = 0,
            defaultName = "ミミ",
            defaultPersonality = Personality(
                energy = 0.5f,
                calmness = 0.85f,
                playfulness = 0.6f,
                sociability = 0.75f,
                routine = 0.8f
            ),
            description = "おっとりとした穏やかな白ウサギ。人懐っこく、静かに過ごすのが好きです。",
            personalityLabels = listOf("🌸おっとり", "😌穏やか", "💕人懐っこい"),
            isFree = true,
            isProOnly = false,
            imageResName = "pet_rabbit_0"
        ),
        PetCatalogEntry(
            id = "rabbit_1",
            type = PetType.RABBIT,
            variation = 1,
            defaultName = "モフ",
            defaultPersonality = Personality(
                energy = 0.8f,
                calmness = 0.4f,
                playfulness = 0.85f,
                sociability = 0.7f,
                routine = 0.5f
            ),
            description = "活発で好奇心旺盛な茶色ウサギ。遊び好きで、いつも元気に飛び跳ねています。",
            personalityLabels = listOf("⚡活発", "🔍好奇心旺盛", "🎾遊び好き"),
            isFree = false,
            isProOnly = false,
            imageResName = "pet_rabbit_1"
        ),
        PetCatalogEntry(
            id = "rabbit_2",
            type = PetType.RABBIT,
            variation = 2,
            defaultName = "ユキ",
            defaultPersonality = Personality(
                energy = 0.6f,
                calmness = 0.9f,
                playfulness = 0.5f,
                sociability = 0.5f,
                routine = 0.85f
            ),
            description = "慎重で静かなグレーウサギ。マイペースで落ち着いた性格。神秘的な雰囲気を纏っています。",
            personalityLabels = listOf("🤫静か", "🐇マイペース", "✨神秘的"),
            isFree = false,
            isProOnly = true,
            imageResName = "pet_rabbit_2"
        )
    )
    
    /**
     * 無料スターターペット（初回選択用）
     */
    val FREE_STARTER_PETS = ALL_PETS.filter { it.isFree }
    
    /**
     * プレミアムペット
     */
    val PREMIUM_PETS = ALL_PETS.filter { !it.isFree }
    
    /**
     * IDでペットを検索
     */
    fun findById(id: String): PetCatalogEntry? = ALL_PETS.find { it.id == id }
    
    /**
     * タイプとバリエーションでペットを検索
     */
    fun findByTypeAndVariation(type: PetType, variation: Int): PetCatalogEntry? =
        ALL_PETS.find { it.type == type && it.variation == variation }
}
