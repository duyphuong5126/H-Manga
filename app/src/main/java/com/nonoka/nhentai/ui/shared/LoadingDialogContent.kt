package com.nonoka.nhentai.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.nonoka.nhentai.R
import com.nonoka.nhentai.ui.theme.White
import com.nonoka.nhentai.ui.theme.headlineLargeStyle
import com.nonoka.nhentai.ui.theme.mediumRadius
import com.nonoka.nhentai.ui.theme.smallSpace

@Composable
fun LoadingDialogContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(horizontal = smallSpace)
            .clip(
                RoundedCornerShape(mediumRadius)
            )
            .background(color = White)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            GifImage(
                data = R.drawable.ic_loading_cat_transparent,
                modifier = Modifier.size(80.dp)
            )

            Text(
                text = "Loading, please wait",
                style = MaterialTheme.typography.headlineLargeStyle
            )
        }
    }
}