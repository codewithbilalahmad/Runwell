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
    }
}

rootProject.name = "Runwell"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":app")
include(":auth:domain")
include(":auth:presentation")
include(":core:domain")
include(":core:database")
include(":core:notification")
include(":core:data")
include(":auth:data")
include(":run:domain")
include(":run:location")
include(":run:data")
include(":run:network")
include(":run:presentation")
include(":core:presentation:ui")
include(":core:connectivity:domain")
include(":core:presentation:designsystem")
include(":core:connectivity:data")
include(":analytics:domain")
include(":analytics:presentation")
include(":analytics:analytics_feature")
include(":analytics:data")
