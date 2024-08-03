pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        gradlePluginPortal()
        mavenCentral()
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication {
                create<BasicAuthentication>("basic")
            }
            credentials {
                username = "mapbox"
                password = "pk.eyJ1IjoiYWhtYWRvYmVpZCIsImEiOiJjbHpiMXBvdnIwZnR3MmtzODFtY3VtdHl1In0.NYNn6VDL7NoKik0GYnV17w"
            }
        }
    }
    plugins {
        id("com.android.application") version "8.4.0" apply false
        id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication {
                create<BasicAuthentication>("basic")
            }
            credentials {
                username = "mapbox"
                password = "pk.eyJ1IjoiYWhtYWRvYmVpZCIsImEiOiJjbHpiMXBvdnIwZnR3MmtzODFtY3VtdHl1In0.NYNn6VDL7NoKik0GYnV17w"
            }
        }
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Coen390AndroidProject_BreathalyzerApp"
include(":app")
