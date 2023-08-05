package org.github.aanno.pgconv.impl

import impl.MetaTags
import io.documentnode.epub4j.domain.*
import io.documentnode.epub4j.epub.EpubWriter
import org.apache.logging.log4j.kotlin.Logging
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream

class GenerateEpub internal constructor(private val path2Document: MutableMap<String, ReadabilityDocument>) {

    companion object : Logging

    val provider: LazyResourceProvider = EpubLazyResourceProvider(path2Document)
    val book = Book()
    val metadata = book.metadata

    fun test() {
        // metadata.
    }

    fun addBookMeta(metaTags: MetaTags) {
        val title = metaTags["title"]
        val author = metaTags["author"]
        val isbn = metaTags["isbn"]
        val type = metaTags["type"]

        if (title != null) metadata.addTitle(title)
        if (author != null) metadata.addAuthor(Author(author))
        if (isbn != null) metadata.addIdentifier(Identifier("isbn", isbn))
        if (type != null) metadata.addType(type)
    }

    fun addSection(title: String, href: String, ref: TOCReference? = null): TOCReference {
        logger.info("processing section ${title} (${href})")
        if (ref == null) {
            return book.addSection(title, getResource(href))
        } else {
            return book.addSection(ref, title, getResource(href))
        }
    }

    internal fun add(sequence: List<String>) {
        if (sequence.size < 1 || path2Document.size < 1 || sequence.size > path2Document.size) {
            throw IllegalArgumentException()
        }

        val first = sequence[0]
        if (first != null) {
            val document = path2Document[first]
            if (document != null) {
                addBookMeta(document.metaTags)
            }
        }

        sequence.forEach {
            val document = path2Document[it]
            if (document != null) {
                addSection(it, document.hrefPath)
            }
        }
    }

    fun writeTo(file: File) {
        val writer = EpubWriter()
        BufferedOutputStream(file.outputStream()).use {
            writer.write(book, it)
        }
        logger.info("epub written to ${file}")
    }

    fun getResource(href: String): Resource {
        return LazyResource(provider, href)
    }

}

private class EpubLazyResourceProvider(private val path2Document: MutableMap<String, ReadabilityDocument>) :
    LazyResourceProvider {

    override
    fun getResourceStream(href: String?): InputStream {
        val doc = path2Document[href]
        if (doc == null) {
            throw IllegalArgumentException("href ${href} not found")
        }
        return ByteArrayInputStream(doc.document.outerHtml().toByteArray())
    }

}


