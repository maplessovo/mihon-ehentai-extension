package eu.kanade.tachiyomi.extension.en.ehentai

import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Parser tests against HTML snapshots fetched from the live site
 * (2026-08-15). Kept under src/test/resources for offline reproducibility.
 */
class EhentaiParsersTest {

    private fun doc(name: String) = Jsoup.parse(html(name), "https://e-hentai.org/")

    private fun html(name: String): String =
        File("src/test/resources/$name").readText()

    // ------------------------------------------------------------------
    // List pages
    // ------------------------------------------------------------------

    @Test
    fun `parseMangaList - search page rows`() {
        val mangas = parseMangaList(doc("search.html")) { TestManga() }
        assertEquals(25, mangas.size)

        val first = mangas.first()
        assertTrue(first.title.startsWith("[PRESTIGE COMIC"))
        assertEquals("https://e-hentai.org/g/4120392/731f429218/", first.url)
        assertTrue(first.thumbnail_url.orEmpty().startsWith("https://ehgt.org/"))
        assertNotNull(first.genre)
        assertTrue(first.genre.orEmpty().contains("parody:original"))
        assertTrue(first.genre.orEmpty().contains("artist:gunkan amaebi"))
    }

    @Test
    fun `parseMangaList - popular page rows`() {
        val mangas = parseMangaList(doc("popular.html")) { TestManga() }
        assertTrue(mangas.size >= 20)
        assertTrue(mangas.all { it.title.isNotBlank() })
        assertTrue(mangas.all { it.url.startsWith("https://e-hentai.org/g/") })
        // covers may be lazy-loaded via data-src; both forms must be handled
        assertTrue(mangas.all { it.thumbnail_url.orEmpty().startsWith("https://") })
    }

    @Test
    fun `parseMangaList - skips non-gallery rows`() {
        // header/category rows must not be parsed as galleries
        val mangas = parseMangaList(doc("search.html")) { TestManga() }
        assertTrue(mangas.none { it.title.contains("Published") })
        assertTrue(mangas.none { it.title.contains("Doujinshi") && it.url.isBlank() })
    }

    // ------------------------------------------------------------------
    // Pagination
    // ------------------------------------------------------------------

    @Test
    fun `parseNextUrl - search page has next`() {
        val next = parseNextUrl(html("search.html"))
        assertEquals("https://e-hentai.org/?f_search=original&next=4120012", next)
        assertTrue(hasNextPage(html("search.html")))
    }

    @Test
    fun `parseNextUrl - second page has next and prev`() {
        val next = parseNextUrl(html("search_next.html"))
        assertEquals("https://e-hentai.org/?f_search=original&next=4119625", next)
    }

    @Test
    fun `parseNextUrl - popular has no pagination`() {
        assertNull(parseNextUrl(html("popular.html")))
        assertFalse(hasNextPage(html("popular.html")))
    }

    // ------------------------------------------------------------------
    // Gallery detail page
    // ------------------------------------------------------------------

    @Test
    fun `parseGalleryDetails - metadata`() {
        val manga = TestManga()
        parseGalleryDetails(doc("gallery.html"), manga)

        assertTrue(manga.title.startsWith("[PRESTIGE COMIC"))
        assertTrue(manga.thumbnail_url.orEmpty().startsWith("https://ehgt.org/"))
        assertEquals("entor", manga.author)
        assertEquals(0, manga.status)
        assertTrue(manga.initialized)
        assertTrue(manga.genre.orEmpty().contains("parody:original"))
        assertTrue(manga.genre.orEmpty().contains("artist:gunkan amaebi"))
        assertTrue(manga.genre.orEmpty().contains("reclass:manga"))
    }

    @Test
    fun `parseGalleryDetails - description is empty for galleries without one`() {
        val manga = TestManga()
        parseGalleryDetails(doc("gallery.html"), manga)
        assertNull(manga.description)
    }

    @Test
    fun `parseMeta - Posted and Length`() {
        val d = doc("gallery.html")
        assertEquals("2026-08-15 05:47", parseMeta(d, "Posted"))
        assertEquals("27 pages", parseMeta(d, "Length"))
    }

    @Test
    fun `parsePageCount - from Length metadata`() {
        assertEquals(27, parsePageCount(doc("gallery.html")))
    }

    @Test
    fun `parsePageCount - from gpc footer`() {
        assertEquals(27, parsePageCount(doc("gallery_p1.html")))
    }

    @Test
    fun `parseViewerLinks - first thumbnail page has 20 links`() {
        val links = parseViewerLinks(doc("gallery.html"))
        assertEquals(20, links.size)
        assertEquals("https://e-hentai.org/s/82559c457b/4120392-1", links.first())
        assertTrue(links.all { it.startsWith("https://e-hentai.org/s/") })
    }

    @Test
    fun `parseViewerLinks - second thumbnail page`() {
        val links = parseViewerLinks(doc("gallery_p1.html"))
        assertEquals(7, links.size)
        assertEquals("https://e-hentai.org/s/5798dc29b0/4120392-21", links.first())
        assertEquals("https://e-hentai.org/s/27fd504e45/4120392-27", links.last())
    }

    @Test
    fun `parsePostedDate - current format`() {
        assertEquals(parseLegacy("2026-08-15 05:47"), parsePostedDate(doc("gallery.html")))
    }

    // ------------------------------------------------------------------
    // Viewer page
    // ------------------------------------------------------------------

    @Test
    fun `parseImageUrl - standard image`() {
        val url = parseImageUrl(doc("viewer.html"), wantOriginal = false)
        assertTrue(url.startsWith("https://"))
        assertTrue(url.contains("hath.network") || url.contains("e-hentai.org"))
    }

    @Test
    fun `parseImageUrl - original link when requested`() {
        val url = parseImageUrl(doc("viewer.html"), wantOriginal = true)
        assertEquals("https://e-hentai.org/fullimg/4120392/1/kkc0eptamyy/1.jpg", url)
    }

    // ------------------------------------------------------------------
    // Dates
    // ------------------------------------------------------------------

    @Test
    fun `parseDateToEpoch - current and legacy formats`() {
        assertTrue(parseDateToEpoch("2026-08-15 05:47") > 0L)
        assertTrue(parseDateToEpoch("17 September 2024, 12:00") > 0L)
        assertEquals(0L, parseDateToEpoch(""))
        assertEquals(0L, parseDateToEpoch("garbage"))
        assertEquals(0L, parseDateToEpoch(null))
    }

    private fun parseLegacy(text: String): Long {
        val fmt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
        return fmt.parse(text)?.time ?: 0L
    }
}
