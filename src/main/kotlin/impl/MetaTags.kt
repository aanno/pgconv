package impl;

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import org.apache.logging.log4j.kotlin.logger
import org.jsoup.nodes.Attributes
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Tag

class MetaTags() {

    companion object {
        val logger = logger()

        fun of(doc: Document): MetaTags {
            val result = MetaTags()
            result.storeMeta(doc)
            return result
        }
    }

    private val metaTags: Multimap<String, String> = HashMultimap.create()

    operator fun get(k: String): String? {
        val col = metaTags[k]
        val size = col.size
        if (size == 0) {
            return null
        }
        if (size > 1) {
            throw IllegalStateException()
        }
        return col.iterator().next()
    }

    fun storeMeta(doc: Document) {
        doc.select("meta").forEach {
            var key = it.attr("name")
            if (key.isEmpty()) {
                key = it.attr("http-equiv")
            }
            val value = it.attr("content") ?: ""
            metaTags.put(key, value)
        }
    }

    fun writeMeta(element: Element?) {
        if (element != null) {
            val head: Element = element.selectFirst("head")!!
            var title: String? = null
            logger.info("write ${metaTags.keySet().size} meta tags")
            metaTags.keySet().forEach { k ->
                val v = metaTags.get(k)
                if (v.size == 1) {
                    val attrs = Attributes()
                    if (k == "Content-Type" || k == "content-language") {
                        attrs.add("http-equiv", k)
                    } else {
                        attrs.add("name", k)
                    }
                    val value = v.iterator().next()
                    attrs.add("content", value)
                    head.appendChild(Element(Tag.valueOf("meta"), null, attrs))
                    if (k == "title") {
                        title = value
                    }
                }
            }
            if (title != null && head.selectFirst("title") == null) {
                head.append("<title>${title}</title>")
            }
        }
    }

    @Deprecated("use writeMeta(..)")
    fun addMetaToHtml(html: String): String {
        val startMeta = html.indexOf("<meta ")
        if (startMeta < 0) {
            throw IllegalArgumentException("no meta tag in html?")
        }
        val result = StringBuffer("<html>\n  <head>\n")
        metaTags.keySet().forEach { k ->
            val v = metaTags.get(k)
            if (v.size == 1) {
                result.append("    <meta name=\"${k}\" content=\"${v.iterator().next()}\">\n")
            }
        }
        result.append("\n    ").append(html.subSequence(startMeta, html.lastIndex))
        return result.toString()
    }

    override
    fun toString(): String {
        val sb = StringBuilder("Meta:\n")
        metaTags.keySet().forEach {
            sb.append("${it} -> ${metaTags[it]}")
        }
        return sb.toString()
    }
}
