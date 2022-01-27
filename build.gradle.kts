/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.3.1/userguide/building_java_projects.html
 */

plugins {
  java
  id("org.jetbrains.kotlin.jvm") version "1.6.10"
}

repositories {
  // Use Maven Central for resolving dependencies.
  mavenCentral()

  maven { url = uri("https://papermc.io/repo/repository/maven-public/") }

  // For MockBukkit
  maven { url = uri("https://jitpack.io") }
}

val bundled: Configuration by configurations.creating bundled@{
  isCanBeResolved = false
  isCanBeConsumed = false

  with(configurations.implementation.get()) impl@{
    // Make `implementation` config extend this
    this@impl.extendsFrom(*this@impl.extendsFrom.toTypedArray(), this@bundled)
  }

  this.isTransitive = true
}

// Kotlin related
dependencies {
  val org = "org.jetbrains.kotlin:"
  bundled(org + "kotlin-stdlib-jdk8")

  // Scripting
  bundled(org + "kotlin-scripting-common")
  bundled(org + "kotlin-scripting-jvm")
  bundled(org + "kotlin-scripting-jvm-host")
}

// Bukkit
dependencies {
  // // This dependency is used by the application.
  // implementation("com.google.guava:guava:30.1.1-jre")

  // compileOnly("io.papermc.paper:paper-api:1.17-R0.1-SNAPSHOT")
  // compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
  compileOnly("org.bukkit:bukkit:1.15.2-R0.1-SNAPSHOT")
}

// Plugin.yml generation
dependencies {
  val depAnnotations = "org.spigotmc:plugin-annotations:1.2.3-SNAPSHOT"
  compileOnly(depAnnotations)
  annotationProcessor(depAnnotations)
  // and also in tests
  testCompileOnly(depAnnotations)
  testAnnotationProcessor(depAnnotations)
}

/*
// A helper annotation for making Warnings
dependencies {
  compileOnly("com.pushtorefresh:javac-warning-annotation:1.0.0")
  annotationProcessor("com.pushtorefresh:javac-warning-annotation:1.0.0")
}
*/

// Testing
dependencies {
  // Use MockBukkit and JUnit for testing.
  testImplementation("com.github.seeseemelk:MockBukkit-v1.15:0.3.1-SNAPSHOT")
  testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
}

tasks.test {
  // Use JUnit Platform for unit tests
  useJUnitPlatform()

  testLogging {
    showStandardStreams = true
    showExceptions = true
  }
}

val javaTarget = JavaVersion.VERSION_1_8

if(JavaVersion.current() >= JavaVersion.VERSION_1_9) {
  tasks.withType<JavaCompile>().configureEach {
    options.release.set(javaTarget.majorVersion.toInt())
  }
} else {
  java {
    sourceCompatibility = javaTarget
    targetCompatibility = javaTarget
  }
}

typealias KotlinCompile = org.jetbrains.kotlin.gradle.tasks.KotlinCompile
tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions {
    jvmTarget = javaTarget.let { (if(it < JavaVersion.VERSION_1_9) "1." else "") + it.majorVersion }
  }
}

base {
  // Set JAR output directory to /build
  this.libsDirectory.set(layout.buildDirectory)
  this.distsDirectory.set(layout.buildDirectory)
}

tasks.jar {
  // pack the whole classpath into the jar
  from(configurations.runtimeClasspath.map {
      config -> config.map { if(it.isDirectory) it else zipTree(it) }
  })

  duplicatesStrategy = DuplicatesStrategy.WARN
}

