import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.21"
    application
    id("org.jetbrains.kotlin.plugin.noarg") version "1.8.21"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.8.21"
}

group = "ru.handh"
version = "1.0"

repositories {
    mavenCentral()
}

noArg {
    annotation("jakarta.persistence.Entity")
}

allOpen {
    annotations("jakarta.persistence.Entity")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.springframework.boot:spring-boot-gradle-plugin:3.0.5")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.21")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.21")
    implementation("org.telegram:telegrambots-spring-boot-starter:6.5.0") {
        exclude("commons-codec", "commons-codec")
    }
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.0.6") {
        exclude("org.yaml", "snakeyaml")
        exclude("org.springframework.boot", "spring-boot-starter-logging")
    }
    implementation("org.springframework.boot:spring-boot-starter-logging:3.0.6")
    implementation("org.postgresql:postgresql:42.6.0")
    runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:1.8.21")
}

application {
    mainClass.set("ru.handh.afisha.bot.AfishaBotApplicationKt")
}

tasks {
    test {
        useJUnitPlatform()
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
    val myFatJar = register<Jar>("myFatJar") {
        dependsOn.addAll(listOf("compileJava", "compileKotlin", "processResources"))
        archiveClassifier.set("standalone")
        isZip64 = true
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest { attributes(mapOf("Main-Class" to application.mainClass)) }
        val sourcesMain = sourceSets.main.get()
        from(
            configurations.runtimeClasspath.get()
                .map { if (it.isDirectory) it else zipTree(it) } +
                    sourcesMain.output
        )
    }

    build {
        dependsOn(myFatJar)
    }
}
