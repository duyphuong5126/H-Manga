package com.nonoka.nhentai.feature

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.nonoka.nhentai.ui.theme.Grey77
import com.nonoka.nhentai.ui.theme.MainColor
import com.nonoka.nhentai.ui.theme.NHentaiTheme
import com.nonoka.nhentai.ui.theme.White
import com.nonoka.nhentai.ui.theme.headerHeight
import com.nonoka.nhentai.ui.theme.mediumRadius
import com.nonoka.nhentai.ui.theme.mediumSpace
import com.nonoka.nhentai.ui.theme.normalSpace
import com.nonoka.nhentai.ui.theme.smallSpace

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NHentaiTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Scaffold(
                        topBar = {
                            Header()
                        }
                    ) {
                        Column(modifier = Modifier.padding(it)) {

                        }
                    }
                }
            }
        }
    }

    companion object {
        fun start(fromContext: Context) {
            fromContext.startActivity(Intent(fromContext, MainActivity::class.java))
        }
    }
}

@Composable
fun Header(modifier: Modifier = Modifier) {
    var searchText by remember { mutableStateOf("") }
    Box(
        modifier = modifier
            .height(headerHeight)
            .background(Grey77)
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = normalSpace, vertical = mediumSpace)
        ) {
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .fillMaxHeight()
                    .background(MainColor)
            )

            Box(
                modifier = Modifier
                    .padding(start = normalSpace)
                    .fillMaxHeight()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = mediumRadius, bottomStart = mediumRadius))
                    .background(White)
                    .padding(horizontal = mediumSpace, vertical = smallSpace)
            ) {
                BasicTextField(
                    modifier = Modifier.align(Alignment.CenterStart),
                    value = searchText,
                    onValueChange = { newText ->
                        searchText = newText
                    },
                    maxLines = 1,
                )
            }
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topEnd = mediumRadius, bottomEnd = mediumRadius))
                    .background(MainColor)
            )
        }
    }
}