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
<<<<<<< HEAD
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io")}
=======
        maven { url = uri("https://jitpack.io") }
>>>>>>> 5th
    }
}

rootProject.name = "BudgetBee"
include(":app")
 