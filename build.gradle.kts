import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    idea
    eclipse
    kotlin("jvm") version "1.7.10"
    application
}

group = "org.github.aanno"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.10")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.7.10")
    implementation("com.google.code.findbugs:jsr305:3.0.2")

    implementation("org.jsoup:jsoup:1.15.3")
    implementation("info.picocli:picocli:4.6.3")
    // implementation("com.google.guava:guava:31.1-jre")

    implementation("org.apache.logging.log4j:log4j-api:2.19.0")
    implementation("org.apache.logging.log4j:log4j-core:2.19.0")
    implementation("org.apache.logging.log4j:log4j-api-kotlin:1.2.0")

    testImplementation(kotlin("test"))
}

/*
idea {
    module {
        downloadJavadoc = true
        downloadSources = true
    }
}
 */

/*
eclipse {
    classpath {
        downloadJavadoc = true
        downloadSources = true
    }
}
 */

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
