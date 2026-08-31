package eu.kanade.tachiyomi.extension.en.ehentai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class EhentaiKeywordFilterTest {

    @Test
    fun `parse terms supports all separators and removes duplicates`() {
        assertEquals(
            listOf("guro", "horror", "artist:name", "中文"),
            parseKeywordFilterTerms(" guro, horror；artist:name\n中文;GURO "),
        )
    }

    @Test
    fun `filter matches title without case sensitivity`() {
        val keep = TestManga().apply { title = "A normal gallery" }
        val hide = TestManga().apply { title = "Contains HORROR" }

        assertEquals(listOf(keep), filterMangasByKeywords(listOf(keep, hide), listOf("horror")))
    }

    @Test
    fun `filter also matches list tags`() {
        val keep = TestManga().apply { title = "Keep"; genre = "artist:someone" }
        val hide = TestManga().apply { title = "Hide"; genre = "female:guro, language:english" }

        assertEquals(listOf(keep), filterMangasByKeywords(listOf(keep, hide), listOf("female:guro")))
    }

    @Test
    fun `empty terms preserve the original list`() {
        val mangas = listOf(TestManga().apply { title = "Anything" })
        assertSame(mangas, filterMangasByKeywords(mangas, emptyList()))
    }
}
