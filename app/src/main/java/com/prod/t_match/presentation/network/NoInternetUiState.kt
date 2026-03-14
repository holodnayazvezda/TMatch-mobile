package com.prod.t_match.presentation.network

data class NoInternetUiState(
	val title: String = "Нет подключения к интернету",
	val description: String = "Запрос не удалось отправить. Проверьте соединение и повторите попытку.",
	val retryButtonLabel: String = "Попробовать ещё раз"
)
