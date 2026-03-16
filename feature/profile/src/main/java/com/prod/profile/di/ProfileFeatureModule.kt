package com.prod.profile.di

import com.prod.profile.domain.usecase.GetOwnAccountProfileUseCase
import com.prod.profile.domain.usecase.GetOwnDatingProfileUseCase
import com.prod.profile.domain.usecase.GetUserCardUseCase
import com.prod.profile.domain.usecase.UpsertOwnDatingProfileUseCase
import com.prod.profile.domain.usecase.UploadDatingPhotoUseCase
import com.prod.profile.domain.usecase.CreateOwnDatingProfileUseCase
import com.prod.profile.presentation.ProfileViewModel
import com.prod.profile.presentation.UserProfileViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val profileFeatureModule = module {
	single { GetOwnAccountProfileUseCase(authGateway = get()) }
	single { GetOwnDatingProfileUseCase(profileGateway = get()) }
	single { GetUserCardUseCase(profileGateway = get()) }
	single { UpsertOwnDatingProfileUseCase(profileGateway = get()) }
	single { UploadDatingPhotoUseCase(profileGateway = get()) }
	single { CreateOwnDatingProfileUseCase(upsertOwnDatingProfileUseCase = get()) }

	viewModel {
		ProfileViewModel(
			getOwnAccountProfileUseCase = get(),
			getOwnDatingProfileUseCase = get(),
			upsertOwnDatingProfileUseCase = get(),
			uploadDatingPhotoUseCase = get()
		)
	}
	viewModel {
		UserProfileViewModel(
			getUserCardUseCase = get()
		)
	}
}
