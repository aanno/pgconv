package org.github.aanno.pgconv

import com.adobe.epubcheck.tool.EpubChecker

fun main(args: Array<String>) {
    val checker = EpubChecker()
    checker.run(args)
}
