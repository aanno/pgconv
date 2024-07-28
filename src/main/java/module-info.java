module org.github.aanno.pgconv {
    requires kotlin.stdlib;
    requires kotlin.reflect;
    requires org.jsoup;
    // requires jdk.unsupported;
    requires org.apache.logging.log4j.api.kotlin;
    requires org.apache.logging.log4j;
    requires kotlinx.coroutines.core;
    requires com.google.common;
    requires org.w3c.epubcheck;
    // not jpms ready dependencies (all in UNNAMED module)
    // requires galimatias;
    // requires readability4j;
    // requires jsr305;
    // requires clikt.jvm;
    // exports org.github.aanno.pgconv;
}
