package com.prod.ui.presentation.network

import androidx.annotation.StringRes
import com.prod.ui.R

data class NoInternetUiState(
	@param:StringRes val titleRes: Int = R.string.no_internet_title,
	@param:StringRes val descriptionRes: Int = R.string.no_internet_description,
	@param:StringRes val retryButtonLabelRes: Int = R.string.no_internet_retry
)
