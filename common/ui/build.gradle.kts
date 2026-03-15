plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.kotlin.compose)
}

android {
	namespace = "com.prod.ui"
	compileSdk = 36

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
	buildFeatures {
		compose = true
	}
}

dependencies {
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.compose.foundation)
	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.compose.ui)
	implementation(libs.androidx.compose.ui.graphics)
	implementation(libs.androidx.compose.material3)
	implementation(libs.androidx.material3)
	implementation(libs.androidx.navigation3.runtime)
	implementation(libs.image.crop.view)
	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
}
