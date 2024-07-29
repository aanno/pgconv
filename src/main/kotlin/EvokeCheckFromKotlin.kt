package org.github.aanno.pgconv

import com.adobe.epubcheck.tool.EpubChecker

fun evokeCheckFromKotlin(args: Array<String>) {
    val checker = EpubChecker()
    println("run EpubChecker on " + args)
    // Exception in thread "main" java.lang.UnsupportedOperationException: ResourceBundle.Control not supported in named modules
    checker.run(args)
}
