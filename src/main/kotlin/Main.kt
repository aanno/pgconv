package org.github.aanno.pgconv

import org.github.aanno.pgconv.impl.CrawlAndRemoveContentDropdown

fun main(args: Array<String>) {
    println("Hello World!")

    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")
    // https://www.projekt-gutenberg.org/jeanpaul/badereis/badereis.html

    val url = "https://www.projekt-gutenberg.org/jeanpaul/badereis/badereis.html"
    // val url = "https://www.projekt-gutenberg.org/jeanpaul/hesperus/hespv11.html"
    val idx = url.lastIndexOf('/')
    val base = url.substring(0, idx)
    val root = url.substring(idx + 1)

    val start: Long = System.currentTimeMillis();
    val main: CrawlAndRemoveContentDropdown = CrawlAndRemoveContentDropdown(base);
    main.parsePageRec(root)
    println("time for conversion: " + (System.currentTimeMillis() - start) + "ms")
}
