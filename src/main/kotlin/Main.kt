package org.github.aanno.pgconv

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.boolean
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.github.aanno.pgconv.impl.CrawlAndRemoveContentDropdown

private class Command : CliktCommand() {

    val url by argument()
        .help("URL to book main page on https://www.projekt-gutenberg.org/ e.g. https://www.projekt-gutenberg.org/jeanpaul/badereis/badereis.html")

    val noReadablility4j by option(
        help = "don't process html with Readability4J"
    ).boolean().default(false)

    val writeInterimFiles by option(
        help = "write interim result (html pages) to disk"
    ).boolean().default(false)

    val noJsoupCleaner by option(
        help = "don't clean/sanitize with JSoup clean"
    ).boolean().default(false)

    val noEpubChecker by option(
        help = "don't run EPUBChecker on generated epub file"
    ).boolean().default(false)

    @ExperimentalCoroutinesApi
    override
    fun run() {
        val idx = url.lastIndexOf('/')
        val base = url.substring(0, idx)
        val root = url.substring(idx + 1)

        val start: Long = System.currentTimeMillis();
        val main: CrawlAndRemoveContentDropdown = CrawlAndRemoveContentDropdown(
            base, noReadablility4j, writeInterimFiles, noJsoupCleaner
        );
        val epub = main.parsePageRec(root)
        val afterGenerate: Long = System.currentTimeMillis()
        println("time for conversion: " + (afterGenerate - start) + "ms")
        if (!noEpubChecker) {
            val checker = PgconvEpubChecker()
            checker.run(arrayOf(epub!!.canonicalPath, "--profile", "default", "--out", "check.xml"))
            // Exception in thread "main" java.lang.UnsupportedOperationException: ResourceBundle.Control not supported in named modules
            // evokeCheckFromKotlin(arrayOf(epub!!.canonicalPath))
            val afterCheck: Long = System.currentTimeMillis()
            println("time for check: " + (afterCheck - afterGenerate) + "ms")
        }
    }
}

fun main(args: Array<String>) {
    // https://www.projekt-gutenberg.org/jeanpaul/badereis/badereis.html
    // val url = "https://www.projekt-gutenberg.org/jeanpaul/badereis/badereis.html"
    // val url = "https://www.projekt-gutenberg.org/jeanpaul/hesperus/hespv11.html"

    Command().main(args)
}
