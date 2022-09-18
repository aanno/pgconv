# pgconv

A converter for projekt-gutenberg.org html books.

## `ebookmaker`

[`ebookmaker`](https://github.com/gutenbergtools/ebookmaker) is a tool that makes most of the job.

```
ebookmaker -v --exclude 'https://www.projekt-gutenberg.org/jeanpaul/hesperus/*-0.html.html' --strip_links  --make=epub.images --max-depth 5 --title "Hesperus" --author "Jean Paul" https://www.projekt-gutenberg.org/jeanpaul/hesperus/hespv11.html
```
