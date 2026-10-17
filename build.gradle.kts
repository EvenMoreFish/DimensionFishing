plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.shadow)
}

repositories {
    //mavenLocal()
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.papermc.io/repository/maven-public/")

    // mcMMO
    maven("https://nexus.neetgames.com/repository/maven-releases/")
    // WorldGuard
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.mcmmo) {
        exclude("*", "*")
    }
    compileOnly(libs.worldguard)

    implementation(libs.bstats)
}

group = "org.evenmorefish"
version = properties["project-version"] as String
java.sourceCompatibility = JavaVersion.VERSION_21

tasks {
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveBaseName.set(project.name)
        archiveVersion.set(project.version.toString())
        archiveClassifier.set("")

        relocate("org.bstats", "org.evenmorefish.dimensionfishing.stats")
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}

publishing {
    repositories {
        maven {
            url = uri("https://repo.codemc.io/repository/EvenMoreFish/")

            val mavenUsername = System.getenv("JENKINS_USERNAME")
            val mavenPassword = System.getenv("JENKINS_PASSWORD")

            if (mavenUsername != null && mavenPassword != null) {
                credentials {
                    username = mavenUsername
                    password = mavenPassword
                }
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = rootProject.name
            version = project.version.toString()

            from(components["java"])
        }
    }
}
