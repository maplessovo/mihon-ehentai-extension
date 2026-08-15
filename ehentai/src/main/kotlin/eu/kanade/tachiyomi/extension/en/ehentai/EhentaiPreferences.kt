package eu.kanade.tachiyomi.extension.en.ehentai

import android.content.SharedPreferences
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreferenceCompat
import eu.kanade.tachiyomi.extension.en.ehentai.Constants.DEFAULT_BASE_URL
import eu.kanade.tachiyomi.extension.en.ehentai.Constants.DEFAULT_USER_AGENT
import eu.kanade.tachiyomi.extension.en.ehentai.Constants.DOMAIN_CUSTOM
import eu.kanade.tachiyomi.extension.en.ehentai.Constants.DOMAIN_EHENTAI
import eu.kanade.tachiyomi.extension.en.ehentai.Constants.DOMAIN_EXHENTAI
import eu.kanade.tachiyomi.extension.en.ehentai.Constants.EXHENTAI_BASE_URL
import eu.kanade.tachiyomi.extension.en.ehentai.Constants.PREF_COOKIE
import eu.kanade.tachiyomi.extension.en.ehentai.Constants.PREF_CUSTOM_DOMAIN
import eu.kanade.tachiyomi.extension.en.ehentai.Constants.PREF_DOMAIN
import eu.kanade.tachiyomi.extension.en.ehentai.Constants.PREF_IMAGE_QUALITY
import eu.kanade.tachiyomi.extension.en.ehentai.Constants.PREF_PRE_RESOLVE_IMAGES
import eu.kanade.tachiyomi.extension.en.ehentai.Constants.PREF_REQUEST_INTERVAL
import eu.kanade.tachiyomi.extension.en.ehentai.Constants.PREF_USER_AGENT
import eu.kanade.tachiyomi.extension.en.ehentai.Constants.QUALITY_ORIGINAL
import eu.kanade.tachiyomi.extension.en.ehentai.Constants.QUALITY_STANDARD

/**
 * Thin wrapper over the source-scoped [SharedPreferences].
 *
 * Values are read on every access (no caching) so that preference changes
 * take effect without restarting the app.
 */
class EhentaiPreferences(private val preferences: SharedPreferences) {

    val baseUrl: String
        get() = when (preferences.getString(PREF_DOMAIN, DOMAIN_EHENTAI)) {
            DOMAIN_EXHENTAI -> EXHENTAI_BASE_URL
            DOMAIN_CUSTOM -> {
                val custom = preferences.getString(PREF_CUSTOM_DOMAIN, "")
                    ?.trim()
                    .orEmpty()
                    .removeSuffix("/")
                custom.ifEmpty { DEFAULT_BASE_URL }
            }
            else -> DEFAULT_BASE_URL
        }

    /** Raw preference value of the domain selector ("e-hentai.org" / "exhentai.org" / "custom"). */
    val domainValue: String
        get() = preferences.getString(PREF_DOMAIN, DOMAIN_EHENTAI) ?: DOMAIN_EHENTAI

    /** Login cookie string, e.g. `ipb_member_id=…; ipb_pass_hash=…; igneous=…`. Empty = not logged in. */
    val cookie: String
        get() = preferences.getString(PREF_COOKIE, "")?.trim().orEmpty()

    val userAgent: String
        get() = preferences.getString(PREF_USER_AGENT, DEFAULT_USER_AGENT)
            ?.trim()
            ?.ifBlank { DEFAULT_USER_AGENT }
            ?: DEFAULT_USER_AGENT

    /** Whether the user asked for original images (requires a valid login cookie). */
    val wantOriginal: Boolean
        get() = preferences.getString(PREF_IMAGE_QUALITY, QUALITY_STANDARD) == QUALITY_ORIGINAL

    val preResolveImages: Boolean
        get() = preferences.getBoolean(PREF_PRE_RESOLVE_IMAGES, false)

    /** Delay between page-type requests (list / gallery / viewer), 0 = disabled. */
    val requestIntervalMs: Long
        get() = preferences.getString(PREF_REQUEST_INTERVAL, "0")?.toLongOrNull() ?: 0L

    fun isExhentai(): Boolean = domainValue == DOMAIN_EXHENTAI

    /**
     * True when the host (or any subdomain) belongs to the e-hentai family,
     * i.e. the only place the login cookie may be sent.
     */
    fun isSiteHost(host: String): Boolean {
        return host == DOMAIN_EHENTAI || host.endsWith(".$DOMAIN_EHENTAI") ||
            host == DOMAIN_EXHENTAI || host.endsWith(".$DOMAIN_EXHENTAI")
    }

    fun setupPreferenceScreen(screen: PreferenceScreen) {
        val context = screen.context

        ListPreference(context).apply {
            key = PREF_DOMAIN
            title = "站点域名 (Domain)"
            summary = "exhentai.org 需要先填写登录 Cookie，否则无法访问"
            entries = arrayOf("e-hentai.org（默认）", "exhentai.org（需登录 Cookie）", "自定义（镜像）")
            entryValues = arrayOf(DOMAIN_EHENTAI, DOMAIN_EXHENTAI, DOMAIN_CUSTOM)
            setDefaultValue(DOMAIN_EHENTAI)
        }.let { screen.addPreference(it) }

        EditTextPreference(context).apply {
            key = PREF_CUSTOM_DOMAIN
            title = "自定义域名 (Custom domain)"
            summary = "仅当域名选择「自定义」时生效，例如 https://example.com（不要带结尾斜杠）"
            dialogTitle = "自定义域名"
            setDefaultValue("")
        }.let { screen.addPreference(it) }

        EditTextPreference(context).apply {
            key = PREF_COOKIE
            title = "登录 Cookie"
            summary = "格式：ipb_member_id=xxx; ipb_pass_hash=yyy; igneous=zzz。留空则不发送。exhentai.org 必填。" +
                "敏感信息仅保存在本机，请勿分享。"
            dialogTitle = "登录 Cookie"
            setDefaultValue("")
        }.let { screen.addPreference(it) }

        EditTextPreference(context).apply {
            key = PREF_USER_AGENT
            title = "User-Agent"
            summary = "默认使用浏览器 UA；若被 Cloudflare 拦截（403/503）可尝试更换"
            dialogTitle = "User-Agent"
            setDefaultValue(DEFAULT_USER_AGENT)
        }.let { screen.addPreference(it) }

        ListPreference(context).apply {
            key = PREF_IMAGE_QUALITY
            title = "图片质量 (Image quality)"
            summary = "原图需要有效登录 Cookie；获取失败时自动回退标准图"
            entries = arrayOf("标准图（默认）", "原图")
            entryValues = arrayOf(QUALITY_STANDARD, QUALITY_ORIGINAL)
            setDefaultValue(QUALITY_STANDARD)
        }.let { screen.addPreference(it) }

        SwitchPreferenceCompat(context).apply {
            key = PREF_PRE_RESOLVE_IMAGES
            title = "预解析图片地址"
            summary = "进入阅读前就解析全部图片地址；大画廊会明显变慢，默认关闭"
            setDefaultValue(false)
        }.let { screen.addPreference(it) }

        ListPreference(context).apply {
            key = PREF_REQUEST_INTERVAL
            title = "请求间隔 (Request interval)"
            summary = "页面类请求（列表/详情/查看页）之间的等待时间，用于避免 429；图片下载不受影响"
            entries = arrayOf("无（默认）", "0.5 秒", "1 秒", "2 秒")
            entryValues = arrayOf("0", "500", "1000", "2000")
            setDefaultValue("0")
        }.let { screen.addPreference(it) }
    }
}
