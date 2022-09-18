package org.github.aanno.pgconv

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.apache.logging.log4j.kotlin.Logging
import org.jsoup.nodes.Element
import java.io.File

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
                String.format("%s\n\t%s",
                headline.attr("title"), headline.absUrl("href"))
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
        val doc: Document = Jsoup.connect(base + "/" + page).get()
        allPages.add(page)
        visitedPages.add(page)

        logger.info(doc.title())
        // remove table stuff
        doc.select(".navi-gb-ed15").remove()
        // remove content dropdown
        val tocContent: Elements = doc.select(".dropdown-content").remove()
        // remove content stuff
        doc.select(".dropdown").remove()
        // remove author on the right
        doc.select(".right").remove()
        // remove hr at end
        doc.select("hr").remove()

        parseContent(tocContent)

        val headers: Elements = doc.select("h1, h2, h3, h4, h5")
        logger.info(headers)

        // System.out.println()
        // System.out.println(doc.html())
        File(page).bufferedWriter().use { it.write(doc.html()) }
    }

    fun parseContent(dropdownContent: Elements) {
        val contentRefs: MutableSet<String> = dropdownContent
            .select("a")
            .fold(mutableSetOf()) {
            s: MutableSet<String>, e: Element ->
            s.add(e.attr("href"))
            s
        }
        allPages.addAll(contentRefs)
        // logger.info("contentRefs: ${contentRefs}")
        // return contentRefs
    }
}
