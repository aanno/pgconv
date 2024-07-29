package org.github.aanno.pgconv.impl

import impl.MetaTags
import net.dankito.readability4j.Article
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Entities
import org.jsoup.nodes.Node

fun Article.getContentWithEncodingAsElement(encoding: String): Document {
    /*
    return "<html>\n  <head>\n    <meta charset=\"$encoding\"/>\n  </head>\n  <body>\n    " +
            "$content\n  </body>\n</html>"

     */
    val document = Document("")
    document.outputSettings()
        .escapeMode(Entities.EscapeMode.xhtml)
        .syntax(Document.OutputSettings.Syntax.xml)
        .charset("utf-8")
    val html = document.appendElement("html")
    val head = html.appendElement("head")
    val metaCharset = head.appendElement("meta")
    metaCharset.attr("charset", encoding)
    val body = html.appendElement("body")
    body.appendChild(articleContent!! as Node)
    return document
}

fun Article.getContentWithEncodingAsElement(encoding: String, metaTags: MetaTags): Document {
    val document = getContentWithEncodingAsElement(encoding)
    metaTags.writeMeta(document)
    fixHref(document)
    return document
}

private fun fixHref(element: Element) {
    element.select("a").forEach {
        val v = it.attr("href")
        it.attr("href", fixHref(v))
    }
}

private fun fixHref(string: String): String {
    if (string.startsWith("file://null/")) {
        return string.substring(12)
    }
    return string
}
