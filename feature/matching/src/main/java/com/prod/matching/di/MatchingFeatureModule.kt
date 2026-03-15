package com.prod.matching.di

import com.prod.matching.domain.usecase.GetMatchingFeedUseCase
import com.prod.matching.domain.usecase.SubmitMatchActionUseCase
import com.prod.matching.presentation.MainMatchingViewModel
import com.prod.matching.presentation.RecommendationUiMapper
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val matchingFeatureModule = module {
	single { RecommendationUiMapper() }
	single { GetMatchingFeedUseCase(matchGateway = get()) }
	single { SubmitMatchActionUseCase(matchGateway = get()) }

	viewModel {
		MainMatchingViewModel(
			getMatchingFeedUseCase = get(),
			submitMatchActionUseCase = get(),
			recommendationUiMapper = get()
		)
	}
}
