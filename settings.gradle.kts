pluginManagement {
    includeBuild("build-logic")
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

rootProject.name = "app_ariz"

include(":app")

// Core: infraestructura compartida. Nunca depende de :feature:* ni de :app.
include(":core:common")
include(":core:model")
include(":core:network")
include(":core:session")
include(":core:database")
include(":core:datastore")
include(":core:designsystem")
include(":core:ui")

// Features: una por modulo del backend/web. Nunca dependen entre si.
include(":feature:auth")
include(":feature:home")
include(":feature:search")
include(":feature:listings")
include(":feature:projects")
include(":feature:favorites")
include(":feature:leads")
include(":feature:alerts")
include(":feature:agents")
include(":feature:account")
include(":feature:rewards")
