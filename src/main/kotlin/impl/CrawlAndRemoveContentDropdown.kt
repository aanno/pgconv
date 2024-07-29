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
import org.jsoup.safety.Cleaner
import org.jsoup.safety.Safelist
import org.jsoup.select.Elements
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.atomic.AtomicLong
import javax.annotation.Nullable

internal data class ReadabilityDocument(val hrefPath: String, val document: Document, val metaTags: MetaTags)

private val WAIT_MS = 2000;
private val CLEANER = Cleaner(mySafelist())

class CrawlAndRemoveContentDropdown(
    private val base: String,
    private val noReadablility4j: Boolean = false,
    private val writeInterimFiles: Boolean = false
) {
    companion object : Logging

    private var root: String? = null

    private val lastAction = AtomicLong(System.currentTimeMillis())

    private val pageChannel = Channel<String>(Channel.UNLIMITED)
    private val allPages: MutableSet<String> = ConcurrentSkipListSet<String>()
    private val pageSequenceFactory = SequenceFactory()
    private val visitedPages: MutableSet<String> = ConcurrentSkipListSet<String>()
    private val path2Document: MutableMap<String, ReadabilityDocument> =
        ConcurrentSkipListMap<String, ReadabilityDocument>()

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
                // sendNextTocPage(root)
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
            } while (current - lastAction.get() <= WAIT_MS)
            pageChannel.close()
            // TODO
            // readability.close()
            logger.info("allPages: ${allPages.size} ${allPages}")
            val sequence = pageSequenceFactory.build()
            logger.info("sequence: ${sequence.size} ${sequence}")
            // logger.info("path2Document: ${path2Document}")
            val generator = GenerateEpub(path2Document)
            generator.add(sequence)
            val idx = root.indexOf('.')
            val outfile = root.substring(0, idx) + ".epub"
            generator.writeTo(File(outfile))
            logger.info("epub written to ${outfile}")
        }
    }

    suspend fun parsePage() {
        val page = pageChannel.receive()
        if (visitedPages.add(page)) GlobalScope.launch {
            lastAction.set(System.currentTimeMillis())
            // https://jsoup.org/cookbook/cleaning-html/safelist-sanitizer
            // val doc: Document = connectWithProxyEnv(base + "/" + page).get()
            val doc: Document = CLEANER.clean(connectWithProxyEnv(base + "/" + page).get())
            // does _not_ really set baseUri
            doc.setBaseUri(base)
            logger.info("Processing ${page}")

            // not already done
            logger.debug(doc.title())
            // remove table stuff
            doc.select(".navi-gb-ed15").remove()
            doc.select(".navi-gb").remove()
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
            // remove adversing
            doc.select(".anzeige-chap").remove()

            // title page processing
            val toc: Elements = doc.select(".toc")
            toc.select("a").forEach { e ->
                e.attr("href", anchor2Page(e.attr("href"), page))
            }
            val normalPage = toc.isEmpty()

            parseNav(page, doc)
            val meta = MetaTags.of(doc)

            /*
            val headers: Elements = doc.select("h1, h2, h3, h4, h5")
            logger.debug(headers)
             */

            readability.send(ReadabilityDocument(page, doc, meta))
        }
    }

    @kotlinx.coroutines.ExperimentalCoroutinesApi
    suspend fun applyReadability() {
        do {
            val readabilityDocument = readability.receive()
            GlobalScope.launch {
                logger.info("process ${readabilityDocument.hrefPath}")
                // writeMeta(readabilityDocument.document)
                applyReadability(readabilityDocument)
            }
        } while (!readability.isEmpty || !readability.isClosedForReceive)
    }

    private suspend fun applyReadability(rd: ReadabilityDocument) {
        var doc: ReadabilityDocument = rd
        if (!noReadablility4j) {
            val readability = Readability4JExtended(parentFileUri(rd.hrefPath), rd.document)
            // https://github.com/bejean/Readability4J
            val article = readability.parse()

            // rewrite meta tags into article
            // writeMeta(article.articleContent)
            // to get content wrapped in <html> tags and encoding set to UTF-8, see chapter 'Output encoding'
            // extractedContentHtmlWithUtf8Encoding = article.contentWithUtf8Encoding
            doc = ReadabilityDocument(
                doc.hrefPath, article.getContentWithEncodingAsElement(
                    "utf-8", rd.metaTags
                ), doc.metaTags
            )
            // val extractedContentPlainText: String = article.getTextContent()
            val title = article.title
            // both not implemented
            // val byline = article.byline
            // val excerpt = article.excerpt
            logger.info("${rd.hrefPath}: ${title}")
        }
        if (writeInterimFiles) {
            // doc.document.setBaseUri(base)
            try {
                File(rd.hrefPath).bufferedWriter().use {
                    it.write(doc.document.outHtmlWithPreamble())
                }
                /*
                File("cleaned-" + rd.hrefPath).bufferedWriter().use {
                    it.write(CLEANER.clean(doc.document).outHtmlWithPreamble())
                }
                 */
            } catch (e: IOException) {
                logger.warn("Can't write ${rd}")
            }
        }
        path2Document.put(rd.hrefPath, doc)
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
                logger.info("schedule unknown previous ${prevHref} ${page}")
                sendPreviousPage(prevHref, page)
            }
            if (!nextKnown && nextHref != null) {
                logger.info("schedule unknown next ${nextHref} ${page}")
                sendNextPage(nextHref, page)
            }
            // remove previous nav (it will still be there at end of page)
            prevNav.remove()
            if (nextNav != null) {
                // remove next nav (it will still be there at end of page)
                nextNav.remove()
            }
        }
    }


    suspend fun parseTocContent(dropdownContent: Elements) {
        logger.debug("parseTocContent")
        val elements = dropdownContent
            .select("a")
            .map { it.attr("href") }
        elements.zipWithNext()
            .forEach {
                if (!allPages.contains(it.first)) {
                    sendNextTocPage(it.first, it.second)
                }
            }
        // decreased performance
        // allPages.addAll(elements)
    }


    suspend fun anchor2Page(anchor: String, refPage: String): String {
        val size = anchor.length;
        if (size <= 1) {
            return anchor
        } else if (anchor.get(0) == '#') {
            return anchor.substring(1) + ".html"
            /*
            if (!allPages.contains(result)) {
                logger.error("${result} does not refer to known page")
                // ???
                sendNextTocPage(result, refPage)
            }
             */
        }
        throw IllegalStateException()
    }

    private suspend fun sendNextTocPage(newPage1: String, newPage2: String) {
        if (sendPage(newPage1)) {
            pageSequenceFactory.add(newPage1, newPage2)
            sendPage(newPage2)
        }
    }

    private suspend fun sendPage(newPage: String): Boolean {
        val result = allPages.add(newPage)
        if (result) {
            pageChannel.send(newPage)
        }
        return result
    }

    private suspend fun sendPreviousPage(newPage: String, refPage: String) {
        // if (newPage != null && newPage.length > 0) {
        val idx = allPages.indexOf(refPage)
        if (idx < 0)
            throw IllegalArgumentException()
        if (sendPage(newPage)) {
            pageSequenceFactory.add(newPage, refPage)
            logger.debug("sendPreviousPage: ${newPage}")
        }
        // }
    }

    private suspend fun sendNextPage(newPage: String, refPage: String) {
        // if (newPage != null && newPage.length > 0) {
        val idx = allPages.indexOf(refPage)
        if (idx < 0)
            throw IllegalArgumentException()
        if (sendPage(newPage)) {
            pageSequenceFactory.add(refPage, newPage)
            logger.debug("sendPreviousPage: ${newPage}")
        }
        // }
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

fun parentFileUri(hrefPath: String): String {
    val idx = hrefPath.lastIndexOf('/')
    val parent = if (idx >= 0) hrefPath.substring(0, idx) else null
    if (parent == null) {
        // https://stackoverflow.com/questions/7857416/file-uri-scheme-and-relative-files
        return "file://./"
    }
    return parent
}
