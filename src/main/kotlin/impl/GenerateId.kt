package org.github.aanno.pgconv.impl

import org.apache.logging.log4j.kotlin.logger
import java.net.URLEncoder
import java.text.Normalizer
import java.util.concurrent.ConcurrentSkipListMap
import kotlin.math.max
import kotlin.random.Random
import kotlin.random.nextUBytes

class GenerateId(val random: Random, val titleLen: Int, val hashLen: Int) {

    companion object {
        val logger = logger()
        val cs = Charsets.UTF_8
        val replaceWithUs: Regex = "\\s+".toRegex()
        val accents: Regex = "\\p{M}".toRegex()
    }

    private val title2Id: MutableMap<String, String> =
        ConcurrentSkipListMap<String,String>()

    fun build(title: String): String {
        if (title.length == 0) {
            throw IllegalArgumentException("title must not be empty")
        }
        val t = shortenTitle(title)
        val hash = generateHash()
        val id = StringBuilder(t).append('-').append(hash).toString()
        title2Id[title] = id
        return id
    }

    // TODO: not thread-safe
    fun buildNew(title: String): String? {
        if (get(title) != null) {
            return null
        }
        return build(title)
    }

    fun get(title: String): String? = title2Id[title]

    // TODO: not thread-safe
    fun put(title: String, id: String): Boolean {
        if (get(title) != null) {
            return false
        }
        // title2Id[title] = id
        return title2Id.put(title, id) == null
    }

    private fun shortenTitle(title: String): String {
        var t = if (title.length > titleLen) title.substring(0, titleLen) else title
        // https://www.baeldung.com/java-remove-accents-from-text
        t = Normalizer.normalize(t.lowercase(), Normalizer.Form.NFKD).replace(accents, "")
        // special support for german
        t = t.replace("ä", "ae")
            .replace("ö", "oe")
            .replace("ü", "ue")
            .replace("ß", "ss")
        // end of special support for german
        t = URLEncoder.encode(t.replace(replaceWithUs, "_"), cs)
        return t
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun generateHash(): String {
        val len = max(hashLen/2, 1)
        val hash = random.nextUBytes(len).toHexString()
        return hash
    }
}
