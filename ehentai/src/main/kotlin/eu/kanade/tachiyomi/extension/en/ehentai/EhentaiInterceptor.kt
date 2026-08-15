package eu.kanade.tachiyomi.extension.en.ehentai

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds the configured browser User-Agent to every request, and the login
 * cookie only to e-hentai.org / exhentai.org (and their subdomains).
 *
 * Referer headers are set at request-construction time (page requests use
 * `baseUrl/`, image requests use the viewer page URL) because the two kinds
 * of requests are built by different code paths.
 */
class EhentaiInterceptor(private val prefs: EhentaiPreferences) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()

        builder.header("User-Agent", prefs.userAgent)

        val host = request.url.host
        if (prefs.isSiteHost(host)) {
            val cookie = prefs.cookie
            if (cookie.isNotEmpty()) {
                builder.header("Cookie", cookie)
            }
        }

        return chain.proceed(builder.build())
    }
}
