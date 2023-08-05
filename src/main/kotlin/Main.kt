package org.github.aanno.pgconv

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.github.aanno.pgconv.impl.CrawlAndRemoveContentDropdown

private class Command: CliktCommand() {

    val url by argument()
        .help("URL to book main page on https://www.projekt-gutenberg.org/")

    @ExperimentalCoroutinesApi
    override
    fun run() {
        val idx = url.lastIndexOf('/')
        val base = url.substring(0, idx)
        val root = url.substring(idx + 1)

        val start: Long = System.currentTimeMillis();
        val main: CrawlAndRemoveContentDropdown = CrawlAndRemoveContentDropdown(base);
        main.parsePageRec(root)
        println("time for conversion: " + (System.currentTimeMillis() - start) + "ms")
    }
}

fun main(args: Array<String>) {
    // https://www.projekt-gutenberg.org/jeanpaul/badereis/badereis.html
    // val url = "https://www.projekt-gutenberg.org/jeanpaul/badereis/badereis.html"
    // val url = "https://www.projekt-gutenberg.org/jeanpaul/hesperus/hespv11.html"

    Command().main(args)
}
