package eu.kanade.tachiyomi.extension.en.ehentai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import okhttp3.HttpUrl.Companion.toHttpUrl

class EhentaiFiltersTest {

    private val baseUrl = "https://e-hentai.org"

    private fun build(query: String = "", filters: List<eu.kanade.tachiyomi.source.model.Filter<*>>): String {
        return buildSearchParams(baseUrl, query, eu.kanade.tachiyomi.source.model.FilterList(filters))
            .build()
            .toString()
    }

    @Test
    fun `no filters - only query`() {
        assertEquals("https://e-hentai.org/?f_search=test", build("test", listOf()))
    }

    @Test
    fun `blank query and no filters - plain base url`() {
        assertEquals("https://e-hentai.org/", build("", listOf()))
    }

    @Test
    fun `category manga - exclusion mask`() {
        // Manga bit = 4 -> f_cats = 1023 xor 4 = 1019
        val filter = CategoryFilter().apply { state = 2 }
        val url = build("", listOf(filter))
        assertTrue(url.contains("f_cats=1019"))
    }

    @Test
    fun `category all - no f_cats param`() {
        val filter = CategoryFilter().apply { state = 0 }
        val url = build("", listOf(filter))
        assertFalse(url.contains("f_cats"))
    }

    @Test
    fun `rating 5 stars - f_srdd=5`() {
        val filter = RatingFilter().apply { state = 4 }
        val url = build("", listOf(filter))
        assertTrue(url.contains("f_srdd=5"))
    }

    @Test
    fun `rating any - no f_srdd param`() {
        val filter = RatingFilter().apply { state = 0 }
        val url = build("", listOf(filter))
        assertFalse(url.contains("f_srdd"))
    }

    @Test
    fun `language chinese - keyword appended to f_search`() {
        val filter = LanguageFilter().apply { state = 2 }
        val url = build("original", listOf(filter))
        val search = url.toHttpUrl().queryParameter("f_search")
        assertEquals("original language:chinese", search)
    }

    @Test
    fun `language alone - keyword only`() {
        val filter = LanguageFilter().apply { state = 1 }
        val url = build("", listOf(filter))
        assertEquals("language:english", url.toHttpUrl().queryParameter("f_search"))
    }

    @Test
    fun `pages range - f_spf and f_spt`() {
        val url = build("", listOf(PagesFromFilter().apply { state = "10" }, PagesToFilter().apply { state = "20" }))
        assertTrue(url.contains("f_spf=10"))
        assertTrue(url.contains("f_spt=20"))
    }

    @Test
    fun `blank page range - omitted`() {
        val url = build("", listOf(PagesFromFilter().apply { state = "  " }, PagesToFilter().apply { state = "" }))
        assertFalse(url.contains("f_spf"))
        assertFalse(url.contains("f_spt"))
    }

    @Test
    fun `advanced checkboxes`() {
        val url = build("", listOf(ExpungedFilter().apply { state = true }, TorrentFilter().apply { state = true }))
        assertTrue(url.contains("f_sh=on"))
        assertTrue(url.contains("f_sto=on"))
    }

    @Test
    fun `exhentai base url is preserved`() {
        val url = buildSearchParams("https://exhentai.org", "test", eu.kanade.tachiyomi.source.model.FilterList())
            .build()
            .toString()
        assertEquals("https://exhentai.org/?f_search=test", url)
    }

    @Test
    fun `hasNoActiveFilters - defaults`() {
        assertTrue(eu.kanade.tachiyomi.source.model.FilterList(
            CategoryFilter(),
            RatingFilter(),
            LanguageFilter(),
            PagesFromFilter(),
            PagesToFilter(),
            ExpungedFilter(),
            TorrentFilter(),
        ).hasNoActiveFilters())
    }

    @Test
    fun `hasNoActiveFilters - active category`() {
        val filters = eu.kanade.tachiyomi.source.model.FilterList(
            listOf<eu.kanade.tachiyomi.source.model.Filter<*>>(CategoryFilter().apply { state = 1 }),
        )
        assertFalse(filters.hasNoActiveFilters())
    }
}
