package com.app.szone.presentation.screen.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.szone.presentation.ui.theme.SZoneTheme

@Composable
fun WelcomeScreen(
	onContinueClick: () -> Unit,
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(horizontal = 24.dp),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Text(
			text = "Chao mung ban den voi AppCuaNhat",
			style = MaterialTheme.typography.headlineMedium,
			textAlign = TextAlign.Center,
		)
		Text(
			text = "Man hinh chao don don gian de ban test nhanh luong app.",
			style = MaterialTheme.typography.bodyMedium,
			textAlign = TextAlign.Center,
			modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
		)

		Button(onClick = onContinueClick) {
			Text(text = "Bat dau")
		}
	}
}

@Preview(showBackground = true)
@Composable
private fun WelcomeScreenPreview() {
	SZoneTheme {
		WelcomeScreen(onContinueClick = {})
	}
}

