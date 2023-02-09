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
  id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
  // Use Maven Central for resolving dependencies.
  mavenCentral()

  // Bukkit APIs
  maven("https://papermc.io/repo/repository/maven-public/")
  // MockBukkit
  maven("https://jitpack.io")

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

dependencies {

  //region Kotlin & Scripting support
  val fromKotlin = "org.jetbrains.kotlin:"
  bundled(fromKotlin, "kotlin-stdlib-jdk8")
  bundled(fromKotlin, "kotlin-scripting-common")
  bundled(fromKotlin, "kotlin-scripting-jvm")
  bundled(fromKotlin, "kotlin-scripting-jvm-host")
  //endregion

  //region Bukkit
  // compileOnly("io.papermc.paper", "paper-api", "1.17-R0.1-SNAPSHOT")
  // compileOnly("org.spigotmc", "spigot-api", "1.16.5-R0.1-SNAPSHOT")
  compileOnly("org.bukkit", "bukkit", "1.15.2-R0.1-SNAPSHOT")
  //endregion

  //region Plugin.yml generation
  val ymlAnnotations = create("org.spigotmc", "plugin-annotations", "1.2.3-SNAPSHOT")
  ymlAnnotations.let {
    compileOnly(it)
    annotationProcessor(it)
    // and also in tests
    testCompileOnly(it)
    testAnnotationProcessor(it)
  }
  //endregion

  //region A helper annotation for making Warnings
  /*
  compileOnly("com.pushtorefresh:javac-warning-annotation:1.0.0")
  annotationProcessor("com.pushtorefresh:javac-warning-annotation:1.0.0")
  */
  //endregion

  //region Testing

  // Use MockBukkit and JUnit for testing.
  testImplementation("com.github.seeseemelk:MockBukkit-v1.15:0.3.1-SNAPSHOT")
  testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
  //endregion
}

tasks.test {
  // Use JUnit Platform for unit tests
  useJUnitPlatform()

  testLogging {
    showStandardStreams = true
    showExceptions = true
  }
}

//region Configure Java version target
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
//endregion

base {
  // Set JAR output directory to /build
  this.libsDirectory.set(layout.buildDirectory)
  this.distsDirectory.set(layout.buildDirectory)
}

/**/

// TODO Not Working
// include as individual jars in .jar/libs/
val jarInJar by tasks.registering(Jar::class) {
  from(sourceSets.main.get().output)

  val libDir = "libs"

  manifest {
    attributes(
      "Class-Path" to "EventHook/$libDir/*"
    )
  }

  into(libDir) {
    from(*configurations.runtimeClasspath.get().files.toTypedArray())
  }

  archiveClassifier.set("nested")
}

// TODO The only format which seems to work
// pack the whole classpath into the jar
val messyJar by tasks.registering(Jar::class) {
  from(sourceSets.main.get().output)

  from(configurations.runtimeClasspath.map {
      config -> config.map { if(it.isDirectory) it else zipTree(it) }
  })

  archiveClassifier.set("amalgamated")

  duplicatesStrategy = DuplicatesStrategy.WARN
}

// TODO NOT WORKING
tasks.shadowJar {
  archiveClassifier.set("bundled")
  minimize()
  // dependsOn(relocation)
}

// TODO WILL NOT WORK
tasks.jar {
  archiveClassifier.set("bare")
}

tasks.assemble {
  this.dependsOn(jarInJar, messyJar, tasks.shadowJar) // tasks.jar already in depended
}
