package com.prod.profile.di

import com.prod.profile.domain.usecase.GetOwnDatingProfileUseCase
import com.prod.profile.domain.usecase.UpsertOwnDatingProfileUseCase
import com.prod.profile.domain.usecase.UploadDatingPhotoUseCase
import com.prod.profile.domain.usecase.CreateOwnDatingProfileUseCase
import com.prod.profile.presentation.ProfileViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val profileFeatureModule = module {
	single { GetOwnDatingProfileUseCase(profileGateway = get()) }
	single { UpsertOwnDatingProfileUseCase(profileGateway = get()) }
	single { UploadDatingPhotoUseCase(profileGateway = get()) }
	single { CreateOwnDatingProfileUseCase(upsertOwnDatingProfileUseCase = get()) }

	viewModel {
		ProfileViewModel(
			getOwnDatingProfileUseCase = get(),
			createOwnDatingProfileUseCase = get(),
			uploadDatingPhotoUseCase = get()
		)
	}
}
