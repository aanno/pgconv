import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinxCoroutines by properties

val kotlinVersion by properties
val jsr305Version by properties
val jsoupVersion by properties
val log4jVersion by properties
val log4jApiKotlinVersion by properties
val pgconvModuleName by properties
val jdkVersion by properties
val readability4jVersion by properties
val guavaVersion by properties

plugins {
    java
    idea
    eclipse
    kotlin("jvm") version "1.9.0"
    application
    distribution
}

group = "org.github.aanno.pgconv"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(jdkVersion.toString().toInt())
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

// needed?
java {
    modularity.inferModulePath.set(true)
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${kotlinxCoroutines}")

    implementation("com.google.code.findbugs:jsr305:${jsr305Version}")

    implementation("org.jsoup:jsoup:${jsoupVersion}")
    implementation("net.dankito.readability4j:readability4j:${readability4jVersion}")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:${log4jVersion}")
    // implementation("info.picocli:picocli:4.6.3")
    implementation("com.google.guava:guava:${guavaVersion}")

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
        kotlinOptions {
            jvmTarget = jdkVersion.toString()
            moduleName = pgconvModuleName.toString()
        }
        compilerOptions {
            moduleName = pgconvModuleName.toString()
            // freeCompilerArgs.add("-module-name ${pgconvModuleName}")
        }
    }

    // https://kotlinlang.org/docs/whatsnew19.html#compiler-option-for-kotlin-native-module-name
    /*
    named<org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile>("compileKotlinLinuxX64") {
        compilerOptions {
            moduleName.set("" + pgconvModuleName)
        }
    }
     */

    // https://kotlinlang.org/docs/gradle-configure-project.html#configure-with-java-modules-jpms-enabled
    named("compileJava", JavaCompile::class.java) {
        options.compilerArgumentProviders.add(CommandLineArgumentProvider {
            // Provide compiled Kotlin classes to javac – needed for Java/Kotlin mixed sources to work
            listOf("--patch-module", "${pgconvModuleName}=${sourceSets["main"].output.asPath}")
        })
    }

    test {
        useJUnitPlatform()
    }

    wrapper {
        distributionType = Wrapper.DistributionType.ALL
    }
}


application {
    // https://docs.gradle.org/current/userguide/application_plugin.html#sec:application_modular
    mainModule = pgconvModuleName.toString()
    mainClass = "org.github.aanno.pgconv.MainKt"
    applicationDefaultJvmArgs = listOf(
        // reflective access
        // "--add-opens", "org.github.aanno.pgconv/org.github.aanno.pgconv=ALL-UNNAMED"
        // reflective access
        // "--add-opens", "org.github.aanno.pgconv/org.github.aanno.pgconv.impl=ALL-UNNAMED"
        // compile-time access
        "--add-reads", "org.github.aanno.pgconv=ALL-UNNAMED"
    )
}
