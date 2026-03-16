package com.prod.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.prod.common.api.model.RecommendationReasonSummary
import com.prod.profile.R
import com.prod.ui.components.AppBottomScrimInfo
import com.prod.ui.components.AppBackButton
import com.prod.ui.components.AppHeroCardFrame
import com.prod.ui.components.AppHeroPagerIndicator
import com.prod.ui.components.AppCircularActionButton
import com.prod.ui.components.AppFactCard
import com.prod.ui.components.AppInfoChip
import com.prod.ui.components.AppScreenTopBar
import com.prod.ui.components.AppSectionCard
import com.prod.ui.components.AppSectionTitle
import com.prod.ui.R as UiR
import com.prod.ui.theme.LocalDimensions
import com.prod.ui.theme.LocalTmatchExtraColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun UserProfileScreen(
	userId: String,
	onBack: () -> Unit,
	reasons: List<RecommendationReasonSummary>,
	showFeedActions: Boolean,
	onLike: () -> Unit,
	onDislike: () -> Unit,
	onOpenChat: () -> Unit,
	onBan: () -> Unit,
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
					reasons = reasons,
					showFeedActions = showFeedActions,
					onLike = onLike,
					onDislike = onDislike,
					onOpenChat = onOpenChat,
					onBan = onBan,
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
	reasons: List<RecommendationReasonSummary>,
	showFeedActions: Boolean,
	onLike: () -> Unit,
	onDislike: () -> Unit,
	onOpenChat: () -> Unit,
	onBan: () -> Unit,
	modifier: Modifier = Modifier
) {
	val dim = LocalDimensions.current

	Column(
		modifier = modifier
	) {
		UserProfileHeader(
			onBack = onBack,
			showMenu = showFeedActions,
			onOpenChat = onOpenChat,
			onBan = onBan
		)

		Column(
			modifier = Modifier
				.weight(1f)
				.verticalScroll(rememberScrollState())
				.padding(horizontal = dim.medium, vertical = dim.cardOuterPadding),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Top
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth(),
				verticalArrangement = Arrangement.spacedBy(16.dp),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				val contentCardModifier = Modifier.fillMaxWidth()

				UserProfileHeroCard(
					profileCard = profileCard,
					modifier = contentCardModifier
						.aspectRatio(dim.profileCardAspectRatio)
				)

				UserProfileAboutSection(
					profileCard = profileCard,
					modifier = contentCardModifier
				)

				if (profileCard.visibleTraitLabels.isNotEmpty()) {
					AppSectionCard(
						modifier = contentCardModifier,
						containerColor = MaterialTheme.colorScheme.surface
					) {
						Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
							AppSectionTitle(title = stringResource(R.string.user_profile_traits_title))
							FlowRow(
								horizontalArrangement = Arrangement.spacedBy(8.dp),
								verticalArrangement = Arrangement.spacedBy(8.dp)
							) {
								profileCard.visibleTraitLabels.forEach { traitLabel ->
									AppInfoChip(text = traitLabel)
								}
							}
						}
					}
				}

				if (showFeedActions && reasons.isNotEmpty()) {
					UserProfileReasonsSection(
						reasons = reasons,
						modifier = contentCardModifier
					)
				}

				if (showFeedActions) {
					UserProfileActionsSection(
						onLike = onLike,
						onDislike = onDislike,
						modifier = contentCardModifier
					)
				}

				Spacer(modifier = Modifier.height(12.dp))
			}
		}
	}
}

@Composable
private fun UserProfileHeader(
	onBack: () -> Unit,
	showMenu: Boolean,
	onOpenChat: () -> Unit,
	onBan: () -> Unit,
	modifier: Modifier = Modifier
) {
	val dim = LocalDimensions.current
	var isMenuExpanded by remember { mutableStateOf(false) }

	AppScreenTopBar(
		title = stringResource(R.string.user_profile_title),
		modifier = modifier
			.fillMaxWidth()
			.padding(horizontal = dim.small, vertical = dim.verySmall),
		navigation = {
			AppBackButton(
				text = stringResource(R.string.user_profile_back_button),
				onClick = onBack
			)
		},
		actions = {
			if (showMenu) {
				IconButton(
					onClick = { isMenuExpanded = true }
				) {
					Icon(
						painter = painterResource(UiR.drawable.ic_menu_24),
						contentDescription = stringResource(R.string.user_profile_menu_button),
						tint = MaterialTheme.colorScheme.onBackground
					)
				}
				DropdownMenu(
					expanded = isMenuExpanded,
					onDismissRequest = { isMenuExpanded = false }
				) {
					DropdownMenuItem(
						text = { Text(stringResource(R.string.user_profile_open_chat_action)) },
						onClick = {
							isMenuExpanded = false
							onOpenChat()
						}
					)
					DropdownMenuItem(
						text = { Text(stringResource(R.string.user_profile_ban_action)) },
						onClick = {
							isMenuExpanded = false
							onBan()
						}
					)
				}
			}
		}
	)
}

@Composable
private fun UserProfileHeroCard(
	profileCard: UserProfileCardUiState,
	modifier: Modifier = Modifier
) {
	val dim = LocalDimensions.current
	val extraColors = LocalTmatchExtraColors.current
	val heroImages = profileCard.heroImages()
	val displayTitle = profileCard.age?.let {
		stringResource(R.string.user_profile_display_title_with_age, profileCard.fullName, it)
	} ?: profileCard.fullName
	val pagerState = rememberPagerState(
		initialPage = 0,
		pageCount = { heroImages.size }
	)

	AppHeroCardFrame(
		modifier = modifier
	) {
		HorizontalPager(
			state = pagerState,
			modifier = Modifier.fillMaxSize(),
			pageSpacing = 0.dp
		) { index ->
			UserProfilePhoto(
				imageUrl = heroImages[index],
				contentDescription = stringResource(
					R.string.user_profile_photo_content_description,
					profileCard.fullName,
					index + 1
				),
				modifier = Modifier.fillMaxSize()
			)
		}

		AppHeroPagerIndicator(
			total = heroImages.size,
			current = pagerState.currentPage
		)

		AppBottomScrimInfo {
			Text(
				text = displayTitle,
				style = MaterialTheme.typography.titleLarge,
				color = extraColors.onBaseScrim,
				fontWeight = FontWeight.SemiBold
			)
		}
	}
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun UserProfileAboutSection(
	profileCard: UserProfileCardUiState,
	modifier: Modifier = Modifier
) {
	val dim = LocalDimensions.current
	AppSectionCard(
		modifier = modifier,
		containerColor = MaterialTheme.colorScheme.surface
	) {
		Column(verticalArrangement = Arrangement.spacedBy(dim.small + dim.extraSmall / 2)) {
			AppSectionTitle(title = stringResource(R.string.user_profile_about_title))

			FlowRow(
				horizontalArrangement = Arrangement.spacedBy(dim.small - dim.extraSmall / 2),
				verticalArrangement = Arrangement.spacedBy(dim.small - dim.extraSmall / 2)
			) {
				AppFactCard(
					title = stringResource(R.string.user_profile_age_title),
					value = profileCard.age?.toString() ?: stringResource(R.string.user_profile_not_specified),
					modifier = Modifier.fillMaxWidth(0.48f)
				)
				AppFactCard(
					title = stringResource(R.string.user_profile_birth_date_title),
					value = profileCard.birthDateLabel,
					modifier = Modifier.fillMaxWidth(0.48f)
				)
				AppFactCard(
					title = stringResource(R.string.user_profile_region_title),
					value = profileCard.region ?: stringResource(R.string.user_profile_not_specified),
					modifier = Modifier.fillMaxWidth()
				)
			}
		}
	}
}

@Composable
private fun UserProfileReasonsSection(
	reasons: List<RecommendationReasonSummary>,
	modifier: Modifier = Modifier
) {
	AppSectionCard(
		modifier = modifier,
		containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
		contentColor = MaterialTheme.colorScheme.onPrimaryContainer
	) {
		Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
			AppSectionTitle(title = stringResource(R.string.user_profile_reasons_title))
			Text(
				text = stringResource(R.string.user_profile_reasons_description),
				style = MaterialTheme.typography.bodyMedium
			)
			Column(
				verticalArrangement = Arrangement.spacedBy(10.dp)
			) {
				reasons.forEach { reason ->
					AppSectionCard(
						containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
						shape = com.prod.core.ui.theme.LocalShapes.current.mediumRounded,
						tonalElevation = 0.dp
					) {
							Text(
								text = reason.label,
								style = MaterialTheme.typography.titleSmall,
								fontWeight = FontWeight.SemiBold
							)
							if (reason.description.isNotBlank()) {
								Text(
									text = reason.description,
									style = MaterialTheme.typography.bodyMedium
								)
							}
					}
				}
			}
		}
	}
}

@Composable
private fun UserProfileActionsSection(
	onLike: () -> Unit,
	onDislike: () -> Unit,
	modifier: Modifier = Modifier
) {
	AppSectionCard(
		modifier = modifier,
		containerColor = MaterialTheme.colorScheme.surface
	) {
		Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
			AppSectionTitle(title = stringResource(R.string.user_profile_actions_title))
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceEvenly
			) {
				AppCircularActionButton(
					iconRes = UiR.drawable.ic_dislike_24,
					contentDescription = stringResource(R.string.user_profile_dislike_action),
					containerColor = MaterialTheme.colorScheme.errorContainer,
					contentColor = MaterialTheme.colorScheme.onErrorContainer,
					onClick = onDislike
				)
				AppCircularActionButton(
					iconRes = UiR.drawable.ic_like_24,
					contentDescription = stringResource(R.string.user_profile_like_action),
					containerColor = MaterialTheme.colorScheme.primaryContainer,
					contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
					onClick = onLike
				)
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
		shape = RoundedCornerShape(28.dp),
		color = MaterialTheme.colorScheme.surfaceContainerHigh
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
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
