package org.github.aanno.pgconv.impl

import impl.MetaTags
import net.dankito.readability4j.Article
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

fun Article.getContentWithEncodingAsElement(encoding: String): Document {
    /*
    return "<html>\n  <head>\n    <meta charset=\"$encoding\"/>\n  </head>\n  <body>\n    " +
            "$content\n  </body>\n</html>"

     */
    val document = Document("")
    val html = document.appendElement("html")
    val head = html.appendElement("head")
    val metaCharset = head.appendElement("meta")
    metaCharset.attr("charset", encoding)
    val body = html.appendElement("body")
    body.appendChild(articleContent)
    return document
}

fun Article.getContentWithEncodingAsElement(encoding: String, metaTags: MetaTags): Document {
    val document = getContentWithEncodingAsElement(encoding)
    metaTags.writeMeta(document)
    return document
}
