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

rootProject.name = "ArcadeUPB"

include(":core")      // librería compartida: contrato del servicio + cliente + UI común
include(":hub")       // app central: login, huella, menú en cruz, servicio de scores
include(":monopoly")  // juego 1
include(":tetris")    // juego 2
include(":triqui")    // juego 3
include(":culebrita") // juego 4
