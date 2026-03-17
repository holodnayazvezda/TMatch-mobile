package com.prod.t_match.debug

import android.os.Process
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prod.t_match.R
import com.prod.ui.R as UiR
import com.prod.ui.components.AppCircularActionButton
import com.prod.ui.components.AppScreenTopBar
import com.prod.ui.components.AppSectionCard
import com.prod.ui.components.buttons.AppBigButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DebugAppLogsScreen(
	onBack: () -> Unit,
	modifier: Modifier = Modifier
) {
	var isLoading by remember { mutableStateOf(true) }
	var logs by remember { mutableStateOf<List<String>>(emptyList()) }
	var error by remember { mutableStateOf<String?>(null) }
	val listState = rememberLazyListState()
	val coroutineScope = rememberCoroutineScope()

	suspend fun reloadLogs() {
		isLoading = true
		error = null
		val result = runCatching { loadApplicationLogs() }
		result.onSuccess { lines ->
			logs = lines
			isLoading = false
		}.onFailure { throwable ->
			logs = emptyList()
			error = throwable.message ?: "Не удалось загрузить логи приложения"
			isLoading = false
		}
	}

	Column(
		modifier = modifier
			.fillMaxSize()
			.safeDrawingPadding()
	) {
		AppScreenTopBar(
			title = "Логи приложения",
			titleFontSize = 18.sp,
			applySafeDrawingPadding = false,
			navigation = {
				TextButton(onClick = onBack) {
					Row(
						horizontalArrangement = Arrangement.spacedBy(6.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						Icon(
							painter = painterResource(R.drawable.back),
							contentDescription = "Назад",
							tint = MaterialTheme.colorScheme.onBackground
						)
						Text(text = "Назад")
					}
				}
			}
		)

		when {
			isLoading -> {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f),
					contentAlignment = Alignment.Center
				) {
					CircularProgressIndicator()
				}
			}

			error != null -> {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f)
						.padding(20.dp),
					contentAlignment = Alignment.Center
				) {
					AppSectionCard {
						Text(
							text = error.orEmpty(),
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onSurface
						)
						AppBigButton(
							onClick = {
								error = null
								isLoading = true
							},
							buttonText = "Повторить",
							isLoading = false
						)
					}
				}
			}

			else -> {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f)
				) {
					LazyColumn(
						modifier = Modifier.fillMaxSize(),
						state = listState,
						contentPadding = PaddingValues(
							start = 20.dp,
							top = 16.dp,
							end = 20.dp,
							bottom = 92.dp
						),
						verticalArrangement = Arrangement.spacedBy(12.dp)
					) {
						stickyHeader(key = "header") {
							Box(
								modifier = Modifier
									.fillMaxWidth()
									.padding(bottom = 12.dp)
							) {
								AppSectionCard {
									Text(
										text = "Всего строк: ${logs.size}",
										style = MaterialTheme.typography.titleMedium,
										fontWeight = FontWeight.SemiBold
									)
									AppBigButton(
										onClick = {
											error = null
											isLoading = true
										},
										buttonText = "Обновить логи",
										isLoading = false
									)
								}
							}
						}

						itemsIndexed(logs, key = { index, line -> "$index-$line" }) { index, line ->
							AppSectionCard(
								containerColor = MaterialTheme.colorScheme.surfaceContainerLow
							) {
								Text(
									text = "${index + 1}",
									style = MaterialTheme.typography.labelMedium,
									color = MaterialTheme.colorScheme.onSurfaceVariant
								)
								Text(
									text = line,
									style = MaterialTheme.typography.bodySmall,
									color = MaterialTheme.colorScheme.onSurface
								)
							}
						}
					}

					if (logs.isNotEmpty()) {
						AppCircularActionButton(
							iconRes = UiR.drawable.outline_arrow_drop_down_24,
							contentDescription = "Прокрутить вниз",
							onClick = {
								coroutineScope.launch {
									listState.animateScrollToItem(logs.lastIndex + 1)
								}
							},
							modifier = Modifier
								.align(Alignment.BottomEnd)
								.padding(horizontal = 20.dp, vertical = 16.dp),
							containerColor = MaterialTheme.colorScheme.primary,
							contentColor = MaterialTheme.colorScheme.onPrimary
						)
					}
				}
			}
		}
	}

	LaunchedEffect(isLoading) {
		if (isLoading) {
			reloadLogs()
		}
	}
}

private suspend fun loadApplicationLogs(): List<String> = withContext(Dispatchers.IO) {
	val process = ProcessBuilder(
		"logcat",
		"-d",
		"--pid=${Process.myPid()}",
		"-v",
		"time"
	)
		.redirectErrorStream(true)
		.start()

	try {
		val output = process.inputStream.bufferedReader().use { it.readText() }
		process.waitFor()
		output
			.lineSequence()
			.map { it.trimEnd() }
			.filter { it.isNotBlank() }
			.toList()
	} finally {
		process.destroy()
	}
}
