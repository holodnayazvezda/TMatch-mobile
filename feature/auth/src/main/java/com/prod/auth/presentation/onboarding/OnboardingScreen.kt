package com.prod.auth.presentation.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.prod.auth.presentation.onboarding.components.OnboardingCard
import com.prod.auth.presentation.onboarding.components.OnboardingPagerIndicator
import com.prod.ui.components.AppGradientBackground
import com.prod.ui.components.buttons.AppBigButton
import com.prod.ui.components.buttons.AppMediumButton
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingScreen(
	onCreateAccount: () -> Unit,
	onLogin: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: OnboardingViewModel = koinViewModel()
) {
	val uiState = viewModel.uiState
	val pagerState = rememberPagerState(pageCount = { uiState.pages.size })
	val scope = rememberCoroutineScope()
	val isLastPage = pagerState.currentPage == uiState.pages.lastIndex

	AppGradientBackground(modifier = modifier) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = 24.dp)
				.statusBarsPadding()
				.navigationBarsPadding(),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Spacer(modifier = Modifier.height(16.dp))
			Text(
				text = uiState.appTitle,
				style = MaterialTheme.typography.titleLarge,
				color = MaterialTheme.colorScheme.onBackground,
				textAlign = TextAlign.Center,
				modifier = Modifier.fillMaxWidth()
			)
			Spacer(modifier = Modifier.height(12.dp))
			HorizontalPager(
				state = pagerState,
				modifier = Modifier
					.weight(1f)
					.fillMaxWidth()
			) { pageIndex ->
				OnboardingCard(page = uiState.pages[pageIndex])
			}
			Spacer(modifier = Modifier.height(24.dp))
			OnboardingPagerIndicator(
				total = uiState.pages.size,
				current = pagerState.currentPage
			)
			Spacer(modifier = Modifier.height(20.dp))
			AppMediumButton(
				buttonText = "У меня уже есть аккаунт",
				isLoading = false,
				onClick = onLogin
			)
			Spacer(modifier = Modifier.height(12.dp))
			AppBigButton(
				buttonText = if (isLastPage) "Перейти к регистрации" else "Далее",
				isLoading = false,
				onClick = {
					if (isLastPage) {
						onCreateAccount()
					} else {
						scope.launch {
							pagerState.animateScrollToPage(pagerState.currentPage + 1)
						}
					}
				}
			)
			Spacer(modifier = Modifier.height(24.dp))
		}
	}
}
