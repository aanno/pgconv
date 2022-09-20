package org.github.aanno.pgconv

import org.jsoup.Connection
import org.jsoup.Jsoup
import java.lang.IllegalStateException
import java.net.URL

fun connectWithProxyEnv(url: String): Connection {
    fun findEnvValueUrl(propKeyPrefix: String, envKey: String): URL? {
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

    val httpProxy = findEnvValueUrl("http.proxy", "http_proxy")
    val httpsProxy = findEnvValueUrl("https.proxy", "https_proxy")

    if (url.startsWith("https://")) {
        if (httpsProxy != null) {
            return Jsoup.connect(url).proxy(httpsProxy.host, httpsProxy.port)
        } else {
            return Jsoup.connect(url)
        }
    }
    if (url.startsWith("http://")) {
        if (httpProxy != null) {
            return Jsoup.connect(url).proxy(httpProxy.host, httpProxy.port)
        } else {
            return Jsoup.connect(url)
        }
    }
    throw IllegalStateException(url)
}
