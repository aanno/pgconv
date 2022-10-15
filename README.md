# pgconv

A converter for projekt-gutenberg.org html books.

## `ebookmaker`

[`ebookmaker`](https://github.com/gutenbergtools/ebookmaker) is a tool that makes most of the job.

```
ebookmaker -v --exclude 'https://www.projekt-gutenberg.org/jeanpaul/hesperus/*-0.html.html' --strip_links  --make=epub.images --max-depth 5 --title "Hesperus" --author "Jean Paul" https://www.projekt-gutenberg.org/jeanpaul/hesperus/hespv11.html
```

See [usage](https://github.com/gutenbergtools/ebookmaker/blob/master/USAGE.md) for some tips and tricks.

## Other epub tools

* [open source ebook creators](https://medevel.com/17-open-source-epub-and-ebook-creators/)
* https://github.com/NiklasGollenstede/epub-creator
* https://transpect.github.io/modules-epubtools-frontend.html

## Other conversion tools

* https://pandoc.org/index.html
* https://github.com/ueberdosis/alldocs.app
* https://github.com/vsch/flexmark-java - markdown parser
* https://github.com/danfickle/openhtmltopdf

## Alternatives to jsoup

* https://github.com/digitalfondue/jfiveparse
* https://github.com/codelibs/nekohtml
* https://github.com/peteroupc/HtmlParser

## Kotlin Coroutines

The following helped me a lot to understand using coroutines in the crawler use-case

* https://github.com/brianmadden/krawler - a coroutine-based crawler in kotlin
* https://stackoverflow.com/questions/73659561/how-to-use-kotlin-coroutines-with-two-for-loops-and-channels-that-update-each-ot

