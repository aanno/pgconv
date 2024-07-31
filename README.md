# pgconv

A converter for projekt-gutenberg.org html books to EPUB (for ebook readers).

## Usage

```bash
$ ./pgconv-1.0-SNAPSHOT/bin/pgconv --help
  --no-readablility4j=true|false  don't process html with Readability4J
  --write-interim-files=true|false
                                  write interim result (html pages) to disk
  --no-jsoup-cleaner=true|false   don't clean/sanitize with JSoup clean
  --no-epub-checker=true|false    don't run EPUBChecker on generated epub file
  -h, --help                      Show this message and exit

Arguments:
  <url>  URL to book main page on https://www.projekt-gutenberg.org/ e.g.
         https://www.projekt-gutenberg.org/jeanpaul/badereis/badereis.html
```

## Build

This is a normal Gradle project, i.e. build with `gradle build`.

### Use built distribution

```bash
rm -r pgconv-*/
unzip build/distributions/pgconv-1.0-SNAPSHOT.zip 
./pgconv-1.0-SNAPSHOT/bin/pgconv --help
```

## News

* 28.07.2024 Now uses `io.documentnode:epub4j-core` instead of `ebookmaker`.

## Obsolete

### `ebookmaker`

[`ebookmaker`](https://github.com/gutenbergtools/ebookmaker) is a tool that makes most of the job.

Usage on internet:

```
ebookmaker -v --exclude 'https://www.projekt-gutenberg.org/jeanpaul/hesperus/*-0.html.html' --strip_links  --make=epub.images --max-depth 5 --title "Hesperus" --author "Jean Paul" https://www.projekt-gutenberg.org/jeanpaul/hesperus/hespv11.html
```

Usage on stripped down files from `pgconv`:

```
ebookmaker -v --exclude '*-0.html.html' --make=epub.images --max-depth 1000 --title "Hesperus" --author "Jean Paul" ./hesperus.html
```

See [usage](https://github.com/gutenbergtools/ebookmaker/blob/master/USAGE.md) for some tips and tricks.

#### Tips on `ebookmaker`

* `ebookmaker` (verison 0.12.13) currently only works with python 3.8 and 3.9.

## Links to other projects

### EPUB format and specification

* https://de.wikipedia.org/wiki/EPUB
* https://epubknowledge.com/docs/welcome
* https://kb.daisy.org/publishing/docs/epub/

### Other epub tools

* [open source ebook creators](https://medevel.com/17-open-source-epub-and-ebook-creators/)
* https://github.com/NiklasGollenstede/epub-creator (js, firefox add-on)
* https://transpect.github.io/modules-epubtools-frontend.html (java, xproc, epub2/3)
* https://github.com/documentnode/epub4j (java, only epub2)
* https://github.com/seeseekey/epubwriter (java, only epub3)
* https://github.com/w3c/epubcheck

### Other conversion tools

* https://pandoc.org/index.html
* https://github.com/ueberdosis/alldocs.app
* https://github.com/vsch/flexmark-java - markdown parser
* https://github.com/danfickle/openhtmltopdf

### Alternatives to jsoup

* https://github.com/digitalfondue/jfiveparse
* https://github.com/codelibs/nekohtml
* https://github.com/peteroupc/HtmlParser

### Kotlin Coroutines

The following helped me a lot to understand using coroutines in the crawler use-case

* https://github.com/brianmadden/krawler - a coroutine-based crawler in kotlin
* https://stackoverflow.com/questions/73659561/how-to-use-kotlin-coroutines-with-two-for-loops-and-channels-that-update-each-ot
