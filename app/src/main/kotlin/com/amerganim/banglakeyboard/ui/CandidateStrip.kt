package com.amerganim.banglakeyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Horizontally scrollable suggestion chips shown above the keys (phonetic mode). */
@Composable
fun CandidateStrip(
    candidates: List<String>,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(colors.surface),
    ) {
        items(candidates) { word ->
            Text(
                text = word,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface,
                modifier = Modifier
                    .clickable { onClick(word) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }
}
