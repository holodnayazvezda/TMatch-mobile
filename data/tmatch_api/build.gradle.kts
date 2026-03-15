plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.kotlin.serialization)
}

android {
	namespace = "com.prod.tmatch_api.data"
	compileSdk = 36

	buildFeatures {
		buildConfig = true
	}

	defaultConfig {
		minSdk = 26

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		consumerProguardFiles("consumer-rules.pro")
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
}

dependencies {
	implementation(project(":api:auth"))
	implementation(libs.koin.core)
	implementation(libs.kotlinx.serialization.json)
	implementation(libs.ktor.client.content.negotiation)
	implementation(libs.ktor.client.core)
	implementation(libs.ktor.client.logging)
	implementation(libs.ktor.client.okhttp)
	implementation(libs.ktor.serialization.kotlinx.json)
	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
}
