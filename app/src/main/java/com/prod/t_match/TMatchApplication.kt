package com.prod.t_match

import android.app.Application
import com.prod.auth.di.authFeatureModule
import com.prod.profile.di.profileFeatureModule
import com.prod.tmatch_api.di.tmatchApiDataModule
import com.prod.t_match.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TMatchApplication : Application() {
	override fun onCreate() {
		super.onCreate()

		startKoin {
			androidContext(this@TMatchApplication)
			modules(
				tmatchApiDataModule(baseUrl = BuildConfig.API_BASE_URL),
				authFeatureModule,
				profileFeatureModule
			)
		}
	}
}
