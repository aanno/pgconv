package org.github.aanno.pgconv

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.apache.logging.log4j.kotlin.Logging
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import java.io.File
import javax.annotation.Nullable

class CrawlAndRemoveContentDropdown(private val base: String) {

    companion object : Logging

    private val allPages: MutableSet<String> = mutableSetOf()
    private val visitedPages: MutableSet<String> = mutableSetOf()

    fun parseOld() {
        val doc: Document = Jsoup.connect("https://en.wikipedia.org/").get()
        logger.info(doc.title())
        val newsHeadlines: Elements = doc.select("#mp-itn b a")
        for (headline in newsHeadlines) {
            logger.info(
                String.format(
                    "%s\n\t%s",
                    headline.attr("title"), headline.absUrl("href")
                )
            )
        }
    }

    fun parsePageRec(root: String) {
        parsePage(root)
        do {
            val notVisited = HashSet<String>(allPages)
            notVisited.removeAll(visitedPages)
            notVisited.forEach { parsePage(it) }
        } while (!notVisited.isEmpty())
    }

    fun parsePage(page: String) {
        logger.info("Processing ${page}")
        val doc: Document = connectWithProxyEnv(base + "/" + page).get()
        allPages.add(page)
        visitedPages.add(page)

        logger.debug(doc.title())
        // remove table stuff
        doc.select(".navi-gb-ed15").remove()
        // remove content dropdown
        val tocContent: Elements = doc.select(".dropdown-content").remove()
        parseTocContent(tocContent)
        // remove content stuff
        doc.select(".dropdown").remove()
        // remove author on the right
        doc.select(".right").remove()
        // remove hr at end
        doc.select("hr").remove()
        // remove all js
        doc.select("script").remove()

        // title page processing
        val toc: Elements = doc.select(".toc")
        toc.select("a").forEach { e ->
            e.attr("href", anchor2Page(e.attr("href")))
        }
        val normalPage = toc.isEmpty()

        parseNav(doc)

        val headers: Elements = doc.select("h1, h2, h3, h4, h5")
        logger.debug(headers)

        // System.out.println()
        // System.out.println(doc.html())
        File(page).bufferedWriter().use { it.write(doc.html()) }
    }

    fun parseNav(doc: Document) {
        // author or previous
        var prevNav: Element? = doc.selectFirst("a")
        if (prevNav != null) {
            var cont = true
            // delete all before until body
            do {
                val test = prevNav!!.previousElementSibling()
                if (test != null && !test.`is`("body")) {
                    test.remove()
                } else {
                    cont = false
                }
            } while (cont)
            // author tag => use next anchor
            if (prevNav.attr("href").startsWith("/autoren/")) {
                val auth = prevNav
                prevNav = toNextElementSibling(prevNav, "a")
                if (prevNav == null) {
                    logger.warn("Expected prevNav after author")
                } else if (!prevNav.`is`("a")) {
                    logger.warn("Expected prevNav as anchor after author")
                    prevNav = null
                }
                auth.remove()
            }
        }
        // find nextNav
        if (prevNav != null) {
            var nextNav: Element? = doc.selectFirst("a")
            logger.debug("prevNav: ${prevNav}")
            logger.debug("nextNav: ${nextNav}")
        }
    }


    fun parseTocContent(dropdownContent: Elements) {
        val contentRefs: MutableSet<String> = dropdownContent
            .select("a")
            .fold(mutableSetOf()) { s: MutableSet<String>, e: Element ->
                s.add(e.attr("href"))
                s
            }
        allPages.addAll(contentRefs)
    }


    fun anchor2Page(anchor: String): String {
        val size = anchor.length;
        if (size <= 1) {
            return anchor
        } else if (anchor.get(0) == '#') {
            val result = anchor.substring(1) + ".html"
            if (!allPages.contains(result)) {
                logger.error("${result} does not refer to known page")
            }
        }
        return anchor
    }
}

@Nullable
fun toNextElementSibling(el: Node, expected: String): Element? {
    var result: Node? = el;
    do {
        result = result!!.nextSibling()
    } while (result != null &&
        !((result is Element) && ((result as Element).`is`(expected) || !(result as Element).`is`("br"))))
    return result as Element?
}
