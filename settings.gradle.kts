// settings.gradle.kts

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        // Register the Parcelize compiler plugin
        id("org.jetbrains.kotlin.plugin.parcelize") version "2.2.0"
        // Register the Navigation Safe Args plugin
        id("androidx.navigation.safeargs.kotlin") version "2.9.3"
        id("com.google.gms.google-services") version "4.4.3" apply false
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Training Weaver"
include(":app")
