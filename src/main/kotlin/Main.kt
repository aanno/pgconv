import org.github.aanno.pgconv.CrawlAndRemoveContentDropdown

fun main(args: Array<String>) {
    println("Hello World!")

    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")

    val main: CrawlAndRemoveContentDropdown = CrawlAndRemoveContentDropdown("https://www.projekt-gutenberg.org/jeanpaul/hesperus");
    main.parsePageRec("hespv11.html");
}
