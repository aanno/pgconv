package org.github.aanno.pgconv.impl

import impl.MetaTags
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import net.dankito.readability4j.extended.Readability4JExtended
import org.apache.logging.log4j.kotlin.Logging
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.select.Elements
import java.io.File
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.atomic.AtomicLong
import javax.annotation.Nullable

private val USE_READABILITY4J = true

private data class ReadabilityDocument(val file: File, val document: Document, val metaTags: MetaTags)

class CrawlAndRemoveContentDropdown(private val base: String) {

    companion object : Logging

    private var root: String? = null

    private val lastAction = AtomicLong(System.currentTimeMillis())

    private val pageChannel = Channel<String>(Channel.UNLIMITED)
    private val allPages: MutableSet<String> = ConcurrentSkipListSet<String>()
    private val visitedPages: MutableSet<String> = ConcurrentSkipListSet<String>()

    private val readability = Channel<ReadabilityDocument>(Channel.UNLIMITED)

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

    @kotlinx.coroutines.ExperimentalCoroutinesApi
    fun parsePageRec(root: String) {
        this.root = root
        runBlocking<Unit>(Dispatchers.Default) {
            GlobalScope.launch {
                pageChannel.send(root)
            }
            GlobalScope.launch {
                applyReadability()
            }
            var current = System.currentTimeMillis()
            do {
                if (pageChannel.isEmpty) {
                    delay(500)
                    current = System.currentTimeMillis()
                } else {
                    parsePage()
                }
            } while (current - lastAction.get() <= 5000)
            pageChannel.close()
            // TODO
            // readability.close()
            logger.info("allPages: ${allPages}")
        }
    }

    suspend fun parsePage() {
        val page = pageChannel.receive()
        if (visitedPages.add(page)) GlobalScope.launch {
            lastAction.set(System.currentTimeMillis())
            val doc: Document = connectWithProxyEnv(base + "/" + page).get()
            logger.info("Processing ${page}")

            // not already done
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

            parseNav(page, doc)
            val meta = MetaTags.of(doc)

            /*
            val headers: Elements = doc.select("h1, h2, h3, h4, h5")
            logger.debug(headers)
             */

            val pageFile = File(page) // .canonicalFile
            readability.send(ReadabilityDocument(pageFile, doc, meta))
        }
    }

    suspend fun applyReadability() {
        do {
            val readabilityDocument = readability.receive()
            GlobalScope.launch {
                logger.info("process ${readabilityDocument.file}")
                // writeMeta(readabilityDocument.document)
                applyReadability(readabilityDocument)
            }
        } while (!readability.isEmpty || !readability.isClosedForReceive)
    }

    private suspend fun applyReadability(rd: ReadabilityDocument) {
        var extractedContentHtmlWithUtf8Encoding: Document? = null
        if (USE_READABILITY4J) {
            val readability = Readability4JExtended(parentFileUri(rd.file), rd.document)
            // https://github.com/bejean/Readability4J
            val article = readability.parse()

            // rewrite meta tags into article
            // writeMeta(article.articleContent)
            // to get content wrapped in <html> tags and encoding set to UTF-8, see chapter 'Output encoding'
            // extractedContentHtmlWithUtf8Encoding = article.contentWithUtf8Encoding
            extractedContentHtmlWithUtf8Encoding = article.getContentWithEncodingAsElement(
                "utf-8", rd.metaTags
            )
            // val extractedContentPlainText: String = article.getTextContent()
            val title = article.title
            // both not implemented
            // val byline = article.byline
            // val excerpt = article.excerpt
            logger.info("${rd.file}: ${title}")
        } else {
            extractedContentHtmlWithUtf8Encoding = rd.document
        }
        rd.file.bufferedWriter().use {
            // it.write(rd.metaTags.addMetaToHtml(extractedContentHtmlWithUtf8Encoding!!))
            it.write(extractedContentHtmlWithUtf8Encoding?.outerHtml())
        }
    }

    suspend fun parseNav(page: String, doc: Document) {
        // author or previous
        var prevNav: Element? = doc.selectFirst("a")
        if (prevNav != null) {
            var cont = true
            // delete all before until body
            do {
                val test = prevNav.previousElementSibling()
                if (test != null && !test.`is`("body")) {
                    test.remove()
                } else {
                    cont = false
                }
            } while (cont)
            // author tag => use next anchor
            if (prevNav.attr("href").startsWith("/autoren/")) {
                val auth = prevNav
                prevNav = toNextElementSibling(prevNav, "a", true)
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
            // var nextNav: Element? = doc.selectFirst("a")
            val nextNav: Element? = toNextElementSibling(prevNav, "a", true)

            val prevHref = prevNav.attr("href")
            val prevKnown = allPages.contains(prevHref)

            var nextHref: String? = null
            val nextKnown = if (nextNav != null) {
                nextHref = nextNav.attr("href")
                allPages.contains(nextHref)
            } else {
                false
            }

            logger.debug("prevNav: ${prevNav} ${prevKnown}")
            logger.debug("nextNav: ${nextNav} ${nextKnown}")

            if (!prevKnown) {
                logger.info("schedule unknown previous ${prevHref}")
                sendPreviousPage(prevHref, page)
            }
            if (!nextKnown && nextHref != null) {
                logger.info("schedule unknown next ${nextHref}")
                sendNextPage(nextHref!!, page)
            }
        }
    }


    suspend fun parseTocContent(dropdownContent: Elements) {
        logger.debug("parseTocContent")
        val elements = dropdownContent
            .select("a")
            .map { it.attr("href") }
        elements
            .forEach {
                if (!allPages.contains(it)) {
                    sendNextTocPage(it)
                }
            }
        // decreased performance
        // allPages.addAll(elements)
    }


    suspend fun anchor2Page(anchor: String): String {
        val size = anchor.length;
        if (size <= 1) {
            return anchor
        } else if (anchor.get(0) == '#') {
            val result = anchor.substring(1) + ".html"
            if (!allPages.contains(result)) {
                logger.error("${result} does not refer to known page")
                // ???
                sendNextTocPage(result)
            }
        }
        return anchor
    }

    private suspend fun sendNextTocPage(newPage: String) {
        allPages.add(newPage)
        logger.debug("sendNextTocPage: ${newPage}")
        pageChannel.send(newPage)
    }

    private suspend fun sendPreviousPage(newPage: String, refPage: String) {
        val idx = allPages.indexOf(refPage)
        if (idx < 0) throw IllegalArgumentException()
        // allPages.add(idx, newPage);
        allPages.add(newPage)
        logger.debug("sendPreviousPage: ${newPage}")
        pageChannel.send(newPage)
    }

    private suspend fun sendNextPage(newPage: String, refPage: String) {
        val idx = allPages.indexOf(refPage)
        if (idx < 0) throw IllegalArgumentException()
        // allPages.add(idx + 1, newPage);
        allPages.add(newPage)
        logger.debug("sendPreviousPage: ${newPage}")
        pageChannel.send(newPage)
    }

}

@Nullable
fun toNextElementSibling(el: Node, expected: String, remove: Boolean): Element? {
    var result: Node? = el;
    do {
        val old = result
        result = result!!.nextSibling()
        if (remove && old != el) old!!.remove()
    } while (result != null &&
        !((result is Element) && (result.`is`(expected) || !result.`is`("br")))
    )
    // only return if it is the expected element
    if (result != null && !(result as Element).`is`(expected)) {
        return null
    }
    return result as Element?
}

fun parentFileUri(file: File): String {
    if (file.parentFile == null) {
        // https://stackoverflow.com/questions/7857416/file-uri-scheme-and-relative-files
        return "file://./"
    }
    return file.parentFile.toURI().toString()
}
