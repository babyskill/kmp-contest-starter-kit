package com.labellens.app.domain.analyzer

import com.labellens.app.domain.model.AppLanguage

object ProductNameSanitizer {

    private val BLOCKED_PREFIXES = listOf(
        "ingredients",
        "ingredient",
        "ingredientes",
        "ingredienti",
        "ingrédients",
        "thanh phan",
        "thành phần",
        "supplement facts",
        "supplement fact",
        "nutrition facts",
        "nutrition fact",
    )

    private val PLACEHOLDER_NAMES = listOf(
        "scanned product",
        "san pham quet",
        "sản phẩm quét",
        "untitled label",
        "chua dat ten",
        "chưa đặt tên",
    )

    private val TRAILING_FRAGMENT_WORDS = setOf(
        "and", "or", "with", "of", "in", "by", "for",
        "va", "và", "hoac", "hoặc", "cua", "của", "voi", "với",
        "de", "del", "du", "des", "con", "et",
    )

    /**
     * Checks if a string looks like a plausible product name according to the audit rule:
     * - At most 6 words
     * - Does NOT start with "Ingredients" / "Thanh phan" / "Supplement Facts" / "Nutrition Facts"
     *   (case- and diacritic-insensitive)
     * - Is not a sentence fragment ending mid-word (unbalanced brackets, dangling punctuation, or truncated tokens)
     */
    fun looksLikeProductName(candidate: String?): Boolean {
        if (candidate.isNullOrBlank()) return false
        val trimmed = candidate.trim()
        if (trimmed.length < 2) return false

        // At most 6 words
        val words = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.size > 6 || words.isEmpty()) return false

        // Diacritic-insensitive check
        val norm = DiacriticUtils.stripDiacritics(trimmed.lowercase())
        val normNoPunct = norm.trimStart { it == ':' || it == '-' || it == '.' || it.isWhitespace() }

        // Blocked prefixes
        if (BLOCKED_PREFIXES.any { normNoPunct.startsWith(it) }) return false

        // Known generic placeholder strings
        if (PLACEHOLDER_NAMES.any { norm == it }) return false

        // Comma-separated ingredient lists (>= 2 commas in <= 6 words) are not product names
        if (trimmed.count { it == ',' } >= 2) return false

        // Sentence fragment ending mid-word / unbalanced brackets / open parenthesis
        val openParen = trimmed.count { it == '(' }
        val closeParen = trimmed.count { it == ')' }
        if (openParen != closeParen) return false

        val openBracket = trimmed.count { it == '[' }
        val closeBracket = trimmed.count { it == ']' }
        if (openBracket != closeBracket) return false

        // Ends mid-word or trailing open punctuation / connector
        val trailingPunct = listOf("-", "–", "—", "(", "[", "{", "/", "\\", "&", ",", ":", ";", "...", "…")
        if (trailingPunct.any { trimmed.endsWith(it) }) return false

        // Dangling preposition / conjunction at the end of the line
        val lastWordNorm = DiacriticUtils.stripDiacritics(words.last().lowercase().trimEnd('.', '!', '?'))
        if (lastWordNorm in TRAILING_FRAGMENT_WORDS) return false

        return true
    }

    /**
     * Formats display name with localized fallback: "Chưa đặt tên" (VI) / "Untitled label" (EN).
     */
    fun displayProductName(candidate: String?, language: AppLanguage): String = if (looksLikeProductName(candidate)) {
        candidate!!.trim()
    } else {
        when (language) {
            AppLanguage.VIETNAMESE -> "Chưa đặt tên"
            AppLanguage.ENGLISH -> "Untitled label"
        }
    }
}
