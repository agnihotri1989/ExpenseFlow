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

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "ExpenseFlow"

// --- MULTI-MODULE ATTEMPT (INCOMPLETE / PARKED) ---
// Attempted modularization split to decouple domain and analytics:
// include(":core:model")
// include(":core:database")
// include(":feature:analytics")
// NOTE: Multi-module extraction parked; core models remain in :app single-module.

include(":app")
