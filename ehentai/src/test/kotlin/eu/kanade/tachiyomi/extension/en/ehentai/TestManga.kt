package eu.kanade.tachiyomi.extension.en.ehentai

import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.UpdateStrategy

/**
 * Minimal [SManga] implementation for unit tests.
 *
 * The compile-only extensions-lib stubs `SManga.create()` (the real
 * implementation is provided by the app at runtime), so tests build
 * their own instances instead. Only fields present in extensions-lib
 * 1.4 are used (`memo` does not exist in the legacy API).
 */
class TestManga : SManga {
    override var url: String = ""
    override var title: String = ""
    override var thumbnail_url: String? = null
    override var artist: String? = null
    override var author: String? = null
    override var status: Int = 0
    override var description: String? = null
    override var genre: String? = null
    override var update_strategy: UpdateStrategy = UpdateStrategy.ALWAYS_UPDATE
    override var initialized: Boolean = false
}
