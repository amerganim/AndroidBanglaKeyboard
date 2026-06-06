package com.amerganim.banglakeyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
            .height(46.dp)
            .background(colors.background),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        itemsIndexed(candidates) { index, word ->
            if (index > 0) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .padding(vertical = 9.dp)
                        .width(1.dp)
                        .background(colors.outline),
                )
            }
            Text(
                text = word,
                fontSize = 18.sp,
                // The literal transliteration (index 0) is emphasized.
                fontWeight = if (index == 0) FontWeight.SemiBold else FontWeight.Normal,
                color = if (index == 0) colors.primary else colors.onSurface,
                modifier = Modifier
                    .clickable { onClick(word) }
                    .padding(horizontal = 18.dp, vertical = 10.dp),
            )
        }
    }
}
