package eu.kanade.tachiyomi.extension.en.ehentai

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.awaitSuccess
import eu.kanade.tachiyomi.source.ConfigurableSource
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.SMangaUpdate
import eu.kanade.tachiyomi.source.online.HttpSource
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.supervisorScope
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import androidx.preference.PreferenceScreen
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * E-Hentai (e-hentai.org) source for Mihon.
 *
 * Implemented against the site's current HTML (verified 2026-08-15, see
 * VERIFICATION.md). Highlights:
 * - list/search/popular pages share one row structure (`table.itg tr` rows);
 * - search pagination is cursor based (`next`/`prev` parameters) — `page=N`
 *   is ignored by the server, so the cursor of the previous request is reused;
 * - the gallery page lists viewer URLs (`#gdt a[href*=/s/]`) with `?p=N`
 *   pagination (20 thumbnails per page);
 * - images are resolved lazily per page from the viewer page (`img#img`,
 *   original via the `/fullimg/` link when logged in).
 *
 * The login cookie is only ever sent to e-hentai.org / exhentai.org hosts;
 * exhentai.org additionally requires it (friendly error otherwise).
 */
class Ehentai : HttpSource(), ConfigurableSource {

    override val name = "E-Hentai"
    override val lang = "en"
    override val supportsLatest = false
    override val versionId = 1

    private val preferences: android.content.SharedPreferences by lazy {
        Injekt.get<android.app.Application>()
            .getSharedPreferences("source_$id", android.content.Context.MODE_PRIVATE)
    }

    private val prefs by lazy { EhentaiPreferences(preferences) }

    override val baseUrl: String
        get() = prefs.baseUrl

    override val client: OkHttpClient by lazy {
        network.client.newBuilder()
            .addInterceptor(EhentaiInterceptor(prefs))
            .build()
    }

    /** Cursor of the next results page, keyed by the base search URL. */
    private val nextPageCursors = ConcurrentHashMap<String, String>()

    /** Timestamp of the last page-type request, for the request-interval preference. */
    private val lastPageRequestAt = AtomicLong(0L)

    // ------------------------------------------------------------------
    // Browse / search
    // ------------------------------------------------------------------

    override suspend fun getPopularManga(page: Int): MangasPage {
        val url = "$baseUrl/popular"
        val html = fetchPageHtml(url)
        return MangasPage(parseMangaList(Jsoup.parse(html, url)).onEach { it.setUrlWithoutDomain(it.url) }, false)
    }

    override suspend fun getLatestUpdates(page: Int): MangasPage {
        throw UnsupportedOperationException("Not used")
    }

    override suspend fun getSearchManga(page: Int, query: String, filters: FilterList): MangasPage {
        val searchUrl = buildSearchParams(baseUrl, query, filters).build().toString()
        if (query.isBlank() && filters.hasNoActiveFilters()) {
            return getPopularManga(page)
        }

        val url = if (page <= 1) searchUrl else nextPageCursors[searchUrl] ?: searchUrl
        val html = fetchPageHtml(url)
        val nextUrl = parseNextUrl(html)
        if (nextUrl != null) {
            nextPageCursors[searchUrl] = nextUrl
        } else {
            nextPageCursors.remove(searchUrl)
        }
        val mangas = parseMangaList(Jsoup.parse(html, url)).onEach { it.setUrlWithoutDomain(it.url) }
        return MangasPage(mangas, hasNextPage(html))
    }

    // ------------------------------------------------------------------
    // Details & chapters (one gallery = one chapter)
    // ------------------------------------------------------------------

    override suspend fun getMangaUpdate(
        manga: SManga,
        chapters: List<SChapter>,
        fetchDetails: Boolean,
        fetchChapters: Boolean,
    ): SMangaUpdate = supervisorScope {
        val galleryDoc = if (fetchDetails || fetchChapters) {
            async { fetchGalleryDocument(manga) }
        } else {
            null
        }

        val updatedManga = if (fetchDetails) {
            async { parseGalleryDetails(galleryDoc!!.await(), manga) }
        } else {
            null
        }

        val updatedChapters = if (fetchChapters) {
            async {
                val doc = galleryDoc!!.await()
                listOf(SChapter.create().apply {
                    url = manga.url
                    name = "Full Gallery"
                    chapter_number = 1f
                    date_upload = parsePostedDate(doc)
                    scanlator = null
                })
            }
        } else {
            null
        }

        SMangaUpdate(updatedManga?.await() ?: manga, updatedChapters?.await() ?: chapters)
    }

    private suspend fun fetchGalleryDocument(manga: SManga): Document {
        val url = "$baseUrl${manga.url}"
        return Jsoup.parse(fetchPageHtml(url), url)
    }

    // ------------------------------------------------------------------
    // Pages
    // ------------------------------------------------------------------

    override suspend fun getPageList(chapter: SChapter): List<Page> {
        val galleryUrl = "$baseUrl${chapter.url}"
        val viewerUrls = ArrayList<String>()
        var thumbnailPage = 0
        var pageCount = -1

        while (true) {
            val url = if (thumbnailPage == 0) galleryUrl else "$galleryUrl?p=$thumbnailPage"
            val html = fetchPageHtml(url)
            val doc = Jsoup.parse(html, url)
            if (thumbnailPage == 0) {
                pageCount = parsePageCount(doc)
            }
            val links = parseViewerLinks(doc)
            viewerUrls.addAll(links)
            thumbnailPage++

            if (pageCount > 0 && viewerUrls.size >= pageCount) break
            if (links.isEmpty()) break
            if (thumbnailPage > Constants.MAX_THUMB_PAGES) break
        }

        val preResolve = prefs.preResolveImages
        return viewerUrls.mapIndexed { index, viewerUrl ->
            if (preResolve) {
                Page(index, url = viewerUrl, imageUrl = getImageUrl(Page(index, viewerUrl)))
            } else {
                Page(index, url = viewerUrl)
            }
        }
    }

    override suspend fun getImageUrl(page: Page): String {
        val html = fetchPageHtml(page.url)
        val doc = Jsoup.parse(html, page.url)
        val wantOriginal = prefs.wantOriginal && prefs.cookie.isNotEmpty()
        return parseImageUrl(doc, wantOriginal)
    }

    override fun imageRequest(page: Page): Request {
        val headers = Headers.Builder()
            .add("Referer", page.url)
            .build()
        return GET(page.imageUrl!!, headers)
    }

    // ------------------------------------------------------------------
    // URLs
    // ------------------------------------------------------------------

    override fun getMangaUrl(manga: SManga): String = "$baseUrl${manga.url}"

    override fun getChapterUrl(chapter: SChapter): String = "$baseUrl${chapter.url}"

    // ------------------------------------------------------------------
    // Filters & preferences
    // ------------------------------------------------------------------

    override fun getFilterList(): FilterList = ehentaiFilterList()

    override fun setupPreferenceScreen(screen: PreferenceScreen) = prefs.setupPreferenceScreen(screen)

    // ------------------------------------------------------------------
    // Networking helpers
    // ------------------------------------------------------------------

    /**
     * Fetches a page-type resource (list / gallery / viewer) as a string.
     * Applies the exhentai cookie check, the request-interval throttle and
     * a page Referer. Image downloads do NOT go through this helper.
     */
    private suspend fun fetchPageHtml(url: String): String {
        checkExhentaiAccess()
        throttlePageRequest()
        return try {
            val response = client.newCall(GET(url, pageHeaders())).awaitSuccess()
            response.use { it.body.string() }
        } catch (e: Exception) {
            throw Exception(
                "Failed to fetch $url (${e.message}). Check your network connection and the " +
                    "User-Agent / 登录 Cookie settings of the E-Hentai source.",
                e,
            )
        }
    }

    private fun pageHeaders(): Headers = Headers.Builder()
        .add("Referer", "$baseUrl/")
        .build()

    /** exhentai.org requires a login cookie; fail with a readable message instead of a bare 403. */
    private fun checkExhentaiAccess() {
        if (prefs.isExhentai() && prefs.cookie.isEmpty()) {
            throw Exception(
                "exhentai.org requires a login cookie. Fill in the '登录 Cookie' preference " +
                    "(ipb_member_id / ipb_pass_hash / igneous) in the source settings first.",
            )
        }
    }

    /**
     * Simple throttle between page-type requests (list / gallery / viewer).
     * The first request after a pause is not delayed; subsequent ones wait so
     * that the gap between request starts is at least the configured interval.
     */
    private suspend fun throttlePageRequest() {
        val intervalMs = prefs.requestIntervalMs
        if (intervalMs <= 0L) return
        while (true) {
            val now = System.currentTimeMillis()
            val last = lastPageRequestAt.get()
            val waitMs = intervalMs - (now - last)
            if (waitMs <= 0L) {
                if (lastPageRequestAt.compareAndSet(last, now)) return
            } else {
                delay(waitMs)
            }
        }
    }
}
