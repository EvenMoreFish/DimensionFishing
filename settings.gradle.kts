rootProject.name = "DimensionFishing"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // compileOnly dependencies
            library("paper-api", "io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
            library("mcmmo", "com.gmail.nossr50.mcMMO:mcMMO:2.2.053")
            library("worldguard", "com.sk89q.worldguard:worldguard-bukkit:7.0.12")

            // implementation dependencies
            library("bstats", "org.bstats:bstats-bukkit:3.2.1")

            // bukkitLibrary dependencies

            // Gradle plugins
            plugin("shadow", "com.gradleup.shadow").version("9.4.1")
        }
    }
}
