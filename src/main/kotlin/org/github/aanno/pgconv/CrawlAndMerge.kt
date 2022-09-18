package org.github.aanno.pgconv

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.apache.logging.log4j.kotlin.Logging

class CrawlAndMerge {

    companion object : Logging

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

    fun parse() {
        val doc: Document = Jsoup.connect("https://www.projekt-gutenberg.org/jeanpaul/hesperus/hespv11.html").get()
        logger.info(doc.title())
        // remove table stuff
        doc.select(".navi-gb-ed15").remove()
        // remove content stuff
        doc.select(".dropdown").remove()
        // remove content dropdown
        doc.select(".dropdown-content").remove()
        // remove author on the right
        doc.select(".right").remove()
        // remove hr at end
        doc.select("hr").remove()
        val headers: Elements = doc.select("h1, h2, h3, h4, h5")
        logger.info(headers)

        System.out.println()
        System.out.println(doc.html())
    }
}
