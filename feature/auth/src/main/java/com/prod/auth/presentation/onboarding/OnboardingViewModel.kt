package com.prod.auth.presentation.onboarding

import androidx.lifecycle.ViewModel
import com.prod.auth.presentation.onboarding.model.OnboardingPageUiModel

class OnboardingViewModel : ViewModel() {
	val uiState = OnboardingUiState(
		appTitle = "T-match",
		pages = listOf(
			OnboardingPageUiModel(
				label = "T-Match",
				title = "Ищите людей с похожим ритмом жизни, а не случайные карточки.",
				description = "Подбор строится на безопасных поведенческих сигналах и помогает быстрее переходить от просмотра к реальному диалогу."
			),
			OnboardingPageUiModel(
				label = "Объяснимость",
				title = "Совместимость объясняется понятным языком без чувствительных деталей.",
				description = "Приложение показывает общие lifestyle-паттерны: похожий темп жизни, сценарии досуга и привычки, не раскрывая конкретные места и транзакции."
			),
			OnboardingPageUiModel(
				label = "Контроль общения",
				title = "Вы сами задаёте, кто может написать первым.",
				description = "Приватность работает на стороне сервера: можно принимать сообщения от всех, только от взаимно заинтересованных или отключить новые чаты."
			),
			OnboardingPageUiModel(
				label = "Безопасность",
				title = "Блок, жалоба и завершение контакта встроены в основной путь.",
				description = "Нежелательные контакты не возвращаются в активный сценарий сразу, а ключевые действия фиксируются и не теряются в интерфейсе."
			)
		)
	)
}
