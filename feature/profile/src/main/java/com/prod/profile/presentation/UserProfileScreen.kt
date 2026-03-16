package com.prod.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.prod.profile.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun UserProfileScreen(
	userId: String,
	onBack: () -> Unit,
	onUnauthorized: () -> Unit,
	onRequestNoInternet: (retryAction: () -> Unit) -> Unit,
	modifier: Modifier = Modifier,
	viewModel: UserProfileViewModel = koinViewModel()
) {
	LaunchedEffect(userId, viewModel, onUnauthorized, onRequestNoInternet) {
		viewModel.initialize(
			userId = userId,
			onUnauthorized = onUnauthorized,
			onNoInternet = onRequestNoInternet
		)
	}

	val uiState = viewModel.uiState

	Box(
		modifier = modifier.fillMaxSize()
	) {
		when {
			uiState.profileCard != null -> {
				UserProfileContent(
					profileCard = uiState.profileCard,
					onBack = onBack,
					modifier = Modifier.fillMaxSize()
				)
			}

			uiState.isLoading -> {
				CircularProgressIndicator(
					modifier = Modifier.align(Alignment.Center),
					color = MaterialTheme.colorScheme.primary
				)
			}

			uiState.error != null -> {
				ProfileLoadingError(
					message = uiState.error,
					onRetry = {
						viewModel.retry(
							onUnauthorized = onUnauthorized,
							onNoInternet = onRequestNoInternet
						)
					},
					modifier = Modifier
						.align(Alignment.Center)
						.padding(horizontal = 24.dp)
				)
			}
		}
	}
}

@Composable
private fun UserProfileContent(
	profileCard: UserProfileCardUiState,
	onBack: () -> Unit,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
	) {
		UserProfileHeader(
			onBack = onBack
		)

		Column(
			modifier = Modifier
				.weight(1f)
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 20.dp, vertical = 6.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(16.dp)
		) {
			UserProfileHeroCard(
				profileCard = profileCard,
				modifier = Modifier
					.fillMaxWidth(0.9f)
					.widthIn(max = 360.dp)
			)

			ProfileAccountInfo(
				profileCard = ProfileCardUiState(
					firstName = profileCard.fullName,
					fullName = profileCard.fullName,
					age = profileCard.age,
					birthDateLabel = profileCard.birthDateLabel,
					region = profileCard.region,
					datingPhotoUrls = profileCard.datingPhotoUrls
				),
				modifier = Modifier.fillMaxWidth()
			)

			if (profileCard.visibleTraitLabels.isNotEmpty()) {
				Surface(
					modifier = Modifier.fillMaxWidth(),
					shape = RoundedCornerShape(22.dp),
					color = MaterialTheme.colorScheme.surface,
					tonalElevation = 2.dp
				) {
					Column(
						modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
						verticalArrangement = Arrangement.spacedBy(12.dp)
					) {
						Text(
							text = stringResource(R.string.user_profile_traits_title),
							style = MaterialTheme.typography.titleMedium,
							color = MaterialTheme.colorScheme.onSurface,
							fontWeight = FontWeight.SemiBold
						)
						FlowRow(
							horizontalArrangement = Arrangement.spacedBy(8.dp),
							verticalArrangement = Arrangement.spacedBy(8.dp)
						) {
							profileCard.visibleTraitLabels.forEach { traitLabel ->
								Surface(
									shape = CircleShape,
									color = MaterialTheme.colorScheme.secondaryContainer
								) {
									Text(
										text = traitLabel,
										modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
										style = MaterialTheme.typography.labelLarge,
										color = MaterialTheme.colorScheme.onSecondaryContainer
									)
								}
							}
						}
					}
				}
			}

			Spacer(modifier = Modifier.height(12.dp))
		}
	}
}

@Composable
private fun UserProfileHeader(
	onBack: () -> Unit,
	modifier: Modifier = Modifier
) {
	Box(
		modifier = modifier
			.fillMaxWidth()
			.padding(start = 12.dp, top = 8.dp, end = 20.dp, bottom = 4.dp)
	) {
		TextButton(
			modifier = Modifier.align(Alignment.CenterStart),
			onClick = onBack
		) {
			Text(
				text = stringResource(R.string.user_profile_back_button),
				color = MaterialTheme.colorScheme.onBackground
			)
		}
		Text(
			text = stringResource(R.string.user_profile_title),
			modifier = Modifier.align(Alignment.Center),
			style = MaterialTheme.typography.titleLarge,
			color = MaterialTheme.colorScheme.onBackground
		)
	}
}

@Composable
private fun UserProfileHeroCard(
	profileCard: UserProfileCardUiState,
	modifier: Modifier = Modifier
) {
	Surface(
		modifier = modifier,
		shape = RoundedCornerShape(28.dp),
		color = MaterialTheme.colorScheme.surface,
		tonalElevation = 2.dp
	) {
		Column(
			modifier = Modifier.padding(vertical = 14.dp),
			verticalArrangement = Arrangement.spacedBy(14.dp)
		) {
			LazyRow(
				contentPadding = PaddingValues(horizontal = 14.dp),
				horizontalArrangement = Arrangement.spacedBy(12.dp)
			) {
				itemsIndexed(profileCard.heroImages()) { index, imageUrl ->
					UserProfilePhoto(
						imageUrl = imageUrl,
						contentDescription = "${profileCard.fullName} ${index + 1}",
						modifier = Modifier.fillParentMaxWidth()
					)
				}
			}

			Column(
				modifier = Modifier.padding(horizontal = 18.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				Text(
					text = profileCard.displayTitle(),
					style = MaterialTheme.typography.headlineSmall,
					color = MaterialTheme.colorScheme.onSurface,
					fontWeight = FontWeight.SemiBold
				)
				Row(
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					profileCard.region?.takeIf { it.isNotBlank() }?.let { region ->
						Surface(
							shape = CircleShape,
							color = MaterialTheme.colorScheme.primaryContainer
						) {
							Text(
								text = region,
								modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
								style = MaterialTheme.typography.labelLarge,
								color = MaterialTheme.colorScheme.onPrimaryContainer
							)
						}
					}
				}
			}
		}
	}
}

@Composable
private fun UserProfilePhoto(
	imageUrl: String?,
	contentDescription: String,
	modifier: Modifier = Modifier
) {
	Surface(
		modifier = modifier,
		shape = RoundedCornerShape(22.dp),
		color = MaterialTheme.colorScheme.surfaceContainerHigh
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(420.dp)
				.background(MaterialTheme.colorScheme.surfaceContainerHigh),
			contentAlignment = Alignment.Center
		) {
			if (imageUrl.isNullOrBlank()) {
				Text(
					text = stringResource(R.string.user_profile_photo_placeholder),
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			} else {
				SubcomposeAsyncImage(
					model = imageUrl,
					contentDescription = contentDescription,
					contentScale = ContentScale.Crop,
					modifier = Modifier.fillMaxSize(),
					loading = {
						CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
					},
					error = {
						Text(
							text = stringResource(R.string.user_profile_photo_placeholder),
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
				)
			}
		}
	}
}

private fun UserProfileCardUiState.heroImages(): List<String?> {
	return datingPhotoUrls.takeIf { it.isNotEmpty() } ?: listOf(avatarUrl)
}

private fun UserProfileCardUiState.displayTitle(): String {
	return age?.let { "$fullName, $it" } ?: fullName
}
