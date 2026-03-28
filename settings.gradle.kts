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
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        flatDir {
            dirs("./unityLibrary/libs")
        }
    }
}

rootProject.name = "yunjing"
include(":app")
include(":unityLibrary:xrmanifest.androidlib")
include(":unityLibrary")
project(":unityLibrary").projectDir = file("unityLibrary")
include(":unityLibrary")
include(":unityLibrary:xrmanifest.androidlib")
