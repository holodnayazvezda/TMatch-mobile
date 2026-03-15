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
plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		google()
		mavenCentral()
		maven(url = "https://jitpack.io")
	}
}

rootProject.name = "T-match"
include(":app")
include(":api:common")
include(":feature:auth")
include(":feature:chats")
include(":feature:profile")
include(":feature:matching")
include(":common:ui")
include(":api:auth")
include(":api:chats")
include(":api:profile")
include(":api:match")
include(":data:tmatch_api")
