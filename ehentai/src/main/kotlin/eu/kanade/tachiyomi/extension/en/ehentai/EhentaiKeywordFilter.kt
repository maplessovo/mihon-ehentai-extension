package eu.kanade.tachiyomi.extension.en.ehentai

import eu.kanade.tachiyomi.source.model.SManga
import java.util.Locale

private val KEYWORD_SEPARATOR = Regex("[,;，；\\r\\n]+")

/** Parses a user-entered list while ignoring empty and duplicate keywords. */
internal fun parseKeywordFilterTerms(raw: String): List<String> =
    raw.split(KEYWORD_SEPARATOR)
        .map(String::trim)
        .filter(String::isNotEmpty)
        .distinctBy { it.lowercase(Locale.ROOT) }

/**
 * Removes entries whose title or list-page tags contain a blocked keyword.
 * The original list is returned unchanged when no keywords are configured.
 */
internal fun filterMangasByKeywords(
    mangas: List<SManga>,
    blockedKeywords: List<String>,
): List<SManga> {
    if (blockedKeywords.isEmpty()) return mangas

    return mangas.filterNot { manga ->
        val searchableText = buildString {
            append(manga.title)
            append('\n')
            append(manga.genre.orEmpty())
        }
        blockedKeywords.any { keyword -> searchableText.contains(keyword, ignoreCase = true) }
    }
}
