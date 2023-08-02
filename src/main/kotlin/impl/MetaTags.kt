package impl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.logging.log4j.kotlin.logger
import org.jsoup.nodes.Attributes
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element
import org.jsoup.parser.Tag
import java.lang.IllegalArgumentException
import kotlin.text.StringBuilder

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

    fun storeMeta(doc: Document) {
        doc.select("meta").forEach {
            val key = it.attr("name")
            // val httpEquiv = it.attr("http-equiv")
            // val key = if (name.isNullOrBlank()) httpEquiv else name
            val value = it.attr("content") ?: ""
            if (key != null) {
                metaTags.put(key, value)
            }
        }
    }

    @Deprecated("use addMetaToHtml(..)")
    fun writeMeta(element: Element?) {
        if (element != null) {
            val head: Element = element.selectFirst("head")!!
            logger.info("write ${metaTags.keySet().size} meta tags")
            metaTags.keySet().forEach { k ->
                val v = metaTags.get(k)
                if (v.size == 1) {
                    val attrs = Attributes()
                    attrs.add("name", k)
                    attrs.add("content", v.iterator().next())
                    head.appendChild(Element(Tag.valueOf("meta"), null, attrs))
                }
            }
        }
    }

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
