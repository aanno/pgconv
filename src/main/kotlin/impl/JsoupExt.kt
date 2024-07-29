package org.github.aanno.pgconv.impl

import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Attribute
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.safety.Safelist
import java.net.URL

private var proxyHaveRun = false
private var httpProxy: URL? = null
private var httpsProxy: URL? = null

private fun findEnvValueUrl(propKeyPrefix: String, envKey: String): URL? {
    var result: String? = System.getProperty(propKeyPrefix + "Host")
    if (result == null) {
        result = System.getenv(envKey)
        if (result == null) {
            result = System.getenv(envKey.uppercase())
        }
    } else {
        val port: String? = System.getProperty(propKeyPrefix + "Port")
        if (port == null) {
            result = "http://${result}"
        } else {
            result = "http://${result}:${port}"
        }
    }
    if (result == null) {
        return null
    }
    return URL(result)
}

fun connectWithProxyEnv(url: String): Connection {
    if (!proxyHaveRun) {
        proxyHaveRun = true
        httpProxy = findEnvValueUrl("http.proxy", "http_proxy")
        httpsProxy = findEnvValueUrl("https.proxy", "https_proxy")
    }

    if (url.startsWith("https://")) {
        if (httpsProxy != null) {
            return Jsoup.connect(url).proxy(httpsProxy!!.host, httpsProxy!!.port)
        } else {
            return Jsoup.connect(url)
        }
    }
    if (url.startsWith("http://")) {
        if (httpProxy != null) {
            return Jsoup.connect(url).proxy(httpProxy!!.host, httpProxy!!.port)
        } else {
            return Jsoup.connect(url)
        }
    }
    throw IllegalStateException(url)
}

fun mySafelist(): Safelist {
    var result = Safelist.relaxed()
    result = result.addTags("meta", "head", "title")
    result = result.addAttributes("meta", "name", "content", "http-equiv", "charset")
    result = result.addAttributes("img", "src")
    result = result.addAttributes("li", "value")
    result = result.addAttributes(":all", "id", "class")
    result = result.preserveRelativeLinks(true)
    // Jsoup has problems to understand relative paths _without protocol_.
    // Unsure if this is because of it's strange baseUri handling.
    // baseUri seem not to propagate to inner nodes.
    // This solution is an incomplete HACK.
    return object : Safelist(result) {
        override fun isSafeAttribute(tagName: String, el: Element, attr: Attribute): Boolean {
            val key = attr.key
            if (isSafeTag(tagName) && (key == "src" || key == "href")) {
                val value = attr.value
                if (value.indexOf(":") < 0 && value.indexOf("//") < 0) {
                    return true;
                }
            }
            return super.isSafeAttribute(tagName, el, attr);
        }
    }
}

fun Document.outHtmlWithPreamble(): String {
    // TODO: xml:lang="de"
    val htmlNode = childNodes().get(0)
    htmlNode.attr("xmlns", "http://www.w3.org/1999/xhtml")
    htmlNode.attr("xmlns:ops", "http://www.idpf.org/2007/ops")
    val result = StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
    result.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\"\n")
    result.append("    \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n")
    result.append(html())
    return result.toString()
}
