import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.FileInputStream
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.3.1/userguide/building_java_projects.html
 */

plugins {
  java
  id("org.jetbrains.kotlin.jvm") version "1.8.0"
  id("com.github.johnrengelman.shadow") version "7.1.2"
}

buildscript {
  dependencies {
    classpath("org.yaml", "snakeyaml", "1.33") // For plugin.yml libraries population
  }
}

repositories {
  // Use Maven Central for resolving dependencies.
  mavenCentral()

  // Bukkit APIs
  maven("https://papermc.io/repo/repository/maven-public/")
  // MockBukkit
  maven("https://jitpack.io")

}

// Those who shall be deferred and appear in plugin.yml
val delegated: Configuration by configurations.creating delegated@{
  isCanBeResolved = false
  isCanBeConsumed = false

  // Make `implementation` config extend this
  configurations.implementation.get().extendsFrom(this@delegated)

  this.isTransitive = true
}

dependencies {

  //region Kotlin & Scripting support
  val fromKotlin = "org.jetbrains.kotlin"
  delegated(fromKotlin, "kotlin-stdlib-jdk8")
  delegated(fromKotlin, "kotlin-scripting-jvm-host")
  implementation(fromKotlin, "kotlin-scripting-common")
  implementation(fromKotlin, "kotlin-scripting-jvm")
  //endregion

  //region Bukkit
  compileOnly("org.bukkit", "bukkit", "1.15.2-R0.1-SNAPSHOT")
  // compileOnly("org.spigotmc", "spigot-api", "1.16.5-R0.1-SNAPSHOT")
  // compileOnly("io.papermc.paper", "paper-api", "1.17-R0.1-SNAPSHOT")
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
  testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
  testImplementation("com.github.seeseemelk:MockBukkit-v1.15:0.3.1-SNAPSHOT")
  //endregion
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

tasks.withType<KotlinCompile>().configureEach {
  compilerOptions {
    val jvmUsed = javaTarget.let { (if(it < JavaVersion.VERSION_1_9) "1." else "") + it.majorVersion }
    jvmTarget.set(JvmTarget.fromTarget(jvmUsed))
  }
}
//endregion

base {
  // Set JAR output directory to /build
  this.libsDirectory.set(layout.buildDirectory)
  this.distsDirectory.set(layout.buildDirectory)
}

tasks.compileJava {
  doLast {
    val pluginYmlFile = run {
      val buildDir = sourceSets.main.flatMap { it.java.destinationDirectory.asFile }
      File(buildDir.get(), "plugin.yml")
    }

    val importList = delegated.dependencies.map {
      it.run { "$group:$name:$version" }
    }

    val origYaml: Map<String, Any> = FileInputStream(pluginYmlFile).use {
      val yamlHandler = Yaml()
      yamlHandler.load(it)
    }

    val updatedYaml = origYaml + ("libraries" to importList)

    // Try to keep the comment line :(
    val commentLine = buildString {

      val dtString = LocalDateTime.now().let {
        val pattern = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss", Locale.ENGLISH)
        pattern.format(it)
      }

      appendLine("# Auto-generated plugin.yml,")

      append("# generated at ")
      appendLine(dtString)

      // See if the FQ class name changes :) fuck
      appendLine("# by org.bukkit.plugin.java.annotation.PluginAnnotationProcessor")

    }

    updatedYaml.let {
      val yamlHandler = Yaml(DumperOptions().apply {
        defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
      })

      val there = FileWriter(pluginYmlFile)
      there.use { here ->
        here.append(commentLine)
        here.append("\n\n")
        yamlHandler.dump(it, here)
      }
    }
  }
}

tasks.test {
  // Use JUnit Platform for unit tests
  useJUnitPlatform()

  testLogging {
    showStandardStreams = true
    showExceptions = true
  }
}
