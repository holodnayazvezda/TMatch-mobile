package com.prod.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.prod.common.api.model.RecommendationReasonSummary
import com.prod.profile.R
import com.prod.ui.R as UiR
import org.koin.androidx.compose.koinViewModel

@Composable
fun UserProfileScreen(
	userId: String,
	onBack: () -> Unit,
	reasons: List<RecommendationReasonSummary>,
	showFeedActions: Boolean,
	onLike: () -> Unit,
	onDislike: () -> Unit,
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
	onBan: () -> Unit,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
	) {
		UserProfileHeader(
			onBack = onBack,
			showMenu = showFeedActions,
			onBan = onBan
		)

		Column(
			modifier = Modifier
				.weight(1f)
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 20.dp, vertical = 6.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Top
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.widthIn(max = 360.dp),
				verticalArrangement = Arrangement.spacedBy(16.dp),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				val contentCardModifier = Modifier.fillMaxWidth()

				UserProfileHeroCard(
					profileCard = profileCard,
					modifier = contentCardModifier
				)

				UserProfileAboutSection(
					profileCard = profileCard,
					modifier = contentCardModifier
				)

				if (profileCard.visibleTraitLabels.isNotEmpty()) {
					Surface(
						modifier = contentCardModifier,
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
	onBan: () -> Unit,
	modifier: Modifier = Modifier
) {
	var isMenuExpanded by remember { mutableStateOf(false) }

	Row(
		modifier = modifier
			.fillMaxWidth()
			.padding(horizontal = 12.dp, vertical = 8.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Box(
			modifier = Modifier.weight(1f),
			contentAlignment = Alignment.CenterStart
		) {
			TextButton(
				onClick = onBack
			) {
				Text(
					text = stringResource(R.string.user_profile_back_button),
					color = MaterialTheme.colorScheme.onBackground
				)
			}
		}
		Text(
			text = stringResource(R.string.user_profile_title),
			modifier = Modifier.weight(1f),
			style = MaterialTheme.typography.titleLarge,
			color = MaterialTheme.colorScheme.onBackground,
			textAlign = TextAlign.Center
		)
		Box(
			modifier = Modifier.weight(1f),
			contentAlignment = Alignment.CenterEnd
		) {
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
						text = { Text(stringResource(R.string.user_profile_ban_action)) },
						onClick = {
							isMenuExpanded = false
							onBan()
						}
					)
				}
			}
		}
	}
}

@Composable
private fun UserProfileHeroCard(
	profileCard: UserProfileCardUiState,
	modifier: Modifier = Modifier
) {
	val heroImages = profileCard.heroImages()
	val pagerState = rememberPagerState(
		initialPage = 0,
		pageCount = { heroImages.size }
	)

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
			HorizontalPager(
				state = pagerState,
				modifier = Modifier.fillMaxWidth(),
				pageSpacing = 0.dp
			) { index ->
					UserProfilePhoto(
						imageUrl = heroImages[index],
						contentDescription = "${profileCard.fullName} ${index + 1}",
						modifier = Modifier
							.fillMaxWidth()
							.padding(horizontal = 14.dp)
					)
			}

			if (heroImages.size > 1) {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.Center
				) {
					Row(
						horizontalArrangement = Arrangement.spacedBy(6.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						repeat(heroImages.size) { index ->
							Surface(
								modifier = Modifier.size(
									width = if (pagerState.currentPage == index) 18.dp else 8.dp,
									height = 8.dp
								),
								shape = CircleShape,
								color = if (pagerState.currentPage == index) {
									MaterialTheme.colorScheme.primary
								} else {
									MaterialTheme.colorScheme.outlineVariant
								}
							) {}
						}
					}
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
			}
		}
	}
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun UserProfileAboutSection(
	profileCard: UserProfileCardUiState,
	modifier: Modifier = Modifier
) {
	Surface(
		modifier = modifier,
		shape = RoundedCornerShape(22.dp),
		color = MaterialTheme.colorScheme.surface,
		tonalElevation = 2.dp
	) {
		Column(
			modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
			verticalArrangement = Arrangement.spacedBy(14.dp)
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.Top
			) {
				Column(
					modifier = Modifier.weight(1f),
					verticalArrangement = Arrangement.spacedBy(4.dp)
				) {
					Text(
						text = stringResource(R.string.user_profile_about_title),
						style = MaterialTheme.typography.titleMedium,
						color = MaterialTheme.colorScheme.onSurface,
						fontWeight = FontWeight.SemiBold
					)
				}
				Surface(
					shape = CircleShape,
					color = MaterialTheme.colorScheme.tertiaryContainer
				) {
					Text(
						text = profileCard.photoCountLabel(),
						modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
						style = MaterialTheme.typography.labelLarge,
						color = MaterialTheme.colorScheme.onTertiaryContainer
					)
				}
			}

			FlowRow(
				horizontalArrangement = Arrangement.spacedBy(10.dp),
				verticalArrangement = Arrangement.spacedBy(10.dp)
			) {
				UserProfileFactCard(
					title = stringResource(R.string.user_profile_age_title),
					value = profileCard.age?.toString() ?: stringResource(R.string.user_profile_not_specified),
					modifier = Modifier.fillMaxWidth(0.48f)
				)
				UserProfileFactCard(
					title = stringResource(R.string.user_profile_birth_date_title),
					value = profileCard.birthDateLabel,
					modifier = Modifier.fillMaxWidth(0.48f)
				)
				UserProfileFactCard(
					title = stringResource(R.string.user_profile_region_title),
					value = profileCard.region ?: stringResource(R.string.user_profile_not_specified),
					modifier = Modifier.fillMaxWidth()
				)
			}
		}
	}
}

@Composable
private fun UserProfileFactCard(
	title: String,
	value: String,
	modifier: Modifier = Modifier
) {
	Surface(
		modifier = modifier,
		shape = RoundedCornerShape(18.dp),
		color = MaterialTheme.colorScheme.surfaceContainerHigh
	) {
		Column(
			modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
			verticalArrangement = Arrangement.spacedBy(4.dp)
		) {
			Text(
				text = title,
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			Text(
				text = value,
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onSurface,
				fontWeight = FontWeight.SemiBold
			)
		}
	}
}

@Composable
private fun UserProfileReasonsSection(
	reasons: List<RecommendationReasonSummary>,
	modifier: Modifier = Modifier
) {
	Surface(
		modifier = modifier,
		shape = RoundedCornerShape(22.dp),
		color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
		contentColor = MaterialTheme.colorScheme.onPrimaryContainer
	) {
		Column(
			modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			Text(
				text = stringResource(R.string.user_profile_reasons_title),
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.SemiBold
			)
			Text(
				text = stringResource(R.string.user_profile_reasons_description),
				style = MaterialTheme.typography.bodyMedium
			)
			Column(
				verticalArrangement = Arrangement.spacedBy(10.dp)
			) {
				reasons.forEach { reason ->
					Surface(
						shape = RoundedCornerShape(18.dp),
						color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
					) {
						Column(
							modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
							verticalArrangement = Arrangement.spacedBy(4.dp)
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
}

@Composable
private fun UserProfileActionsSection(
	onLike: () -> Unit,
	onDislike: () -> Unit,
	modifier: Modifier = Modifier
) {
	Surface(
		modifier = modifier,
		shape = RoundedCornerShape(22.dp),
		color = MaterialTheme.colorScheme.surface,
		tonalElevation = 2.dp
	) {
		Column(
			modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			Text(
				text = stringResource(R.string.user_profile_actions_title),
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onSurface,
				fontWeight = FontWeight.SemiBold
			)
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceEvenly
			) {
				UserProfileActionIconButton(
					iconRes = UiR.drawable.ic_dislike_24,
					contentDescription = stringResource(R.string.user_profile_dislike_action),
					containerColor = MaterialTheme.colorScheme.errorContainer,
					contentColor = MaterialTheme.colorScheme.onErrorContainer,
					onClick = onDislike
				)
				UserProfileActionIconButton(
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
private fun UserProfileActionIconButton(
	iconRes: Int,
	contentDescription: String,
	containerColor: androidx.compose.ui.graphics.Color,
	contentColor: androidx.compose.ui.graphics.Color,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	Surface(
		modifier = modifier,
		shape = CircleShape,
		color = containerColor
	) {
		IconButton(
			onClick = onClick,
			modifier = Modifier.size(72.dp)
		) {
			Icon(
				painter = painterResource(iconRes),
				contentDescription = contentDescription,
				tint = contentColor,
				modifier = Modifier.size(28.dp)
			)
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

private fun UserProfileCardUiState.photoCountLabel(): String {
	val count = datingPhotoUrls.size.coerceAtLeast(1)
	return "$count фото"
}
