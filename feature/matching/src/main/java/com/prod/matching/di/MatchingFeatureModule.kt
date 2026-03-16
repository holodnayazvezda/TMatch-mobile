package com.prod.matching.di

import com.prod.matching.data.SharedPrefsPendingMatchActionStore
import com.prod.matching.domain.gateway.PendingMatchActionStore
import com.prod.matching.domain.interactor.MatchingDeckCoordinator
import com.prod.matching.domain.interactor.PendingMatchActionQueue
import com.prod.matching.domain.usecase.GetMatchingFeedUseCase
import com.prod.matching.domain.usecase.GetOwnTargetingUseCase
import com.prod.matching.domain.usecase.GetRecommendationsUseCase
import com.prod.matching.domain.usecase.SubmitMatchActionUseCase
import com.prod.matching.domain.usecase.UpsertOwnTargetingUseCase
import com.prod.matching.presentation.MainMatchingViewModel
import com.prod.matching.presentation.RecommendationUiMapper
import com.prod.matching.presentation.TargetingPreferencesViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val matchingFeatureModule = module {
	single { RecommendationUiMapper() }
	single<PendingMatchActionStore> { SharedPrefsPendingMatchActionStore(context = get()) }
	factory { MatchingDeckCoordinator() }
	factory { PendingMatchActionQueue(pendingMatchActionStore = get()) }
	single { GetMatchingFeedUseCase(matchGateway = get()) }
	single { GetOwnTargetingUseCase(matchGateway = get()) }
	single { GetRecommendationsUseCase(matchGateway = get()) }
	single { SubmitMatchActionUseCase(matchGateway = get()) }
	single { UpsertOwnTargetingUseCase(matchGateway = get()) }

	viewModel {
		MainMatchingViewModel(
			getMatchingFeedUseCase = get(),
			getRecommendationsUseCase = get(),
			submitMatchActionUseCase = get(),
			recommendationUiMapper = get(),
			deckCoordinator = get(),
			pendingActionQueue = get()
		)
	}
	viewModel {
		TargetingPreferencesViewModel(
			getOwnTargetingUseCase = get(),
			upsertOwnTargetingUseCase = get()
		)
	}
}
