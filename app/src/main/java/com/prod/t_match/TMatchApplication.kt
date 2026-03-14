package com.prod.t_match

import android.app.Application
import com.prod.auth.di.authApiModule
import com.prod.auth.di.authFeatureModule
import com.prod.t_match.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TMatchApplication : Application() {
	override fun onCreate() {
		super.onCreate()

		startKoin {
			androidContext(this@TMatchApplication)
			modules(
				appModule,
				authApiModule,
				authFeatureModule
			)
		}
	}
}
