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
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.0.5") {
        exclude("org.yaml", "snakeyaml")
    }
    //implementation("org.postgresql:postgresql:42.6.0")
    runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:1.8.21")
    implementation("com.h2database:h2:2.1.214")
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
    withType<Jar> {
        manifest {
            attributes["Main-Class"] = application.mainClass
        }

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        from(sourceSets.main.get().output)

        dependsOn(configurations.runtimeClasspath)
        from({
            configurations.compileClasspath.get().filter { it.name.endsWith("jar") }.map {zipTree(it)}
        })
    }
}
