import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion by properties
val jsr305Version by properties
val kotlinxCoroutines by properties
val jsoupVersion by properties
val log4jVersion by properties
val log4jApiKotlinVersion by properties

plugins {
    java
    idea
    eclipse
    kotlin("jvm") version "1.9.0"
    application
}

group = "org.github.aanno.pgconv"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${kotlinxCoroutines}")

    implementation("com.google.code.findbugs:jsr305:${jsr305Version}")

    implementation("org.jsoup:jsoup:${jsoupVersion}")
    // implementation("info.picocli:picocli:4.6.3")
    // implementation("com.google.guava:guava:31.1-jre")

    implementation("org.apache.logging.log4j:log4j-api:${log4jVersion}")
    implementation("org.apache.logging.log4j:log4j-core:${log4jVersion}")
    implementation("org.apache.logging.log4j:log4j-api-kotlin:${log4jApiKotlinVersion}")

    testImplementation(kotlin("test"))
}


idea {
    module {
        setDownloadJavadoc(true)
        setDownloadSources(true)
    }
}

eclipse {
    classpath {
        setDownloadJavadoc(true)
        setDownloadSources(true)
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    test {
        useJUnitPlatform()
    }

    wrapper {
        distributionType = Wrapper.DistributionType.ALL
    }
}


application {
    mainClass.set("MainKt")
}
