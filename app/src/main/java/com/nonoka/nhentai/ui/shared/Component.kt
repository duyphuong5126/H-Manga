package com.nonoka.nhentai.ui.shared

import android.view.Gravity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.nonoka.nhentai.ui.theme.MainColor
import com.nonoka.nhentai.ui.theme.bodyNormalBold
import com.nonoka.nhentai.ui.theme.bodyNormalRegular
import com.nonoka.nhentai.ui.theme.headlineLargeStyle
import com.nonoka.nhentai.ui.theme.largeSpace
import com.nonoka.nhentai.ui.theme.mediumSpace
import com.nonoka.nhentai.ui.theme.normalRadius
import com.nonoka.nhentai.ui.theme.normalSpace
import com.nonoka.nhentai.ui.theme.smallSpace
import com.nonoka.nhentai.ui.theme.tinySpace

@Composable
fun LoadingDialog(
    message: String,
    onDismiss: () -> Unit = {},
    properties: DialogProperties = DialogProperties(),
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = properties.let {
            DialogProperties(
                dismissOnBackPress = it.dismissOnBackPress,
                dismissOnClickOutside = it.dismissOnClickOutside,
                securePolicy = it.securePolicy,
                usePlatformDefaultWidth = false
            )
        },
    ) {
        val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
        dialogWindowProvider.window.setGravity(Gravity.BOTTOM)

        Surface(
            modifier = Modifier
                .padding(mediumSpace)
                .fillMaxWidth(),
            shape = RoundedCornerShape(normalRadius),
        ) {
            LoadingDialogContent(message)
        }
    }
}

@Composable
fun YesNoDialog(
    title: String,
    description: String,
    yesLabel: String = "Yes",
    noLabel: String = "No",
    onDismiss: () -> Unit,
    onAnswerYes: () -> Unit = {},
    onAnswerNo: () -> Unit = {},
    properties: DialogProperties = DialogProperties(),
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = properties.let {
            DialogProperties(
                dismissOnBackPress = it.dismissOnBackPress,
                dismissOnClickOutside = it.dismissOnClickOutside,
                securePolicy = it.securePolicy,
                usePlatformDefaultWidth = false
            )
        },
    ) {
        val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
        dialogWindowProvider.window.setGravity(Gravity.BOTTOM)

        Surface(
            modifier = Modifier
                .padding(normalSpace)
                .fillMaxWidth(),
            shape = RoundedCornerShape(largeSpace),
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLargeStyle,
                    modifier = Modifier
                        .padding(
                            vertical = normalSpace,
                            horizontal = largeSpace,
                        )
                        .fillMaxWidth()
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyNormalRegular,
                    modifier = Modifier
                        .padding(
                            vertical = normalSpace,
                            horizontal = largeSpace,
                        )
                        .fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        modifier = Modifier
                            .padding(
                                horizontal = normalSpace,
                                vertical = mediumSpace,
                            )
                            .weight(1f),
                        onClick = {
                            onDismiss()
                            onAnswerNo()
                        },
                    ) {
                        Text(
                            text = noLabel,
                            style = MaterialTheme.typography.bodyNormalBold.copy(color = MainColor)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(vertical = smallSpace)
                            .width(tinySpace)
                            .height(normalSpace)
                            .background(color = MaterialTheme.colorScheme.outline)
                    )

                    TextButton(
                        modifier = Modifier
                            .padding(
                                horizontal = normalSpace,
                                vertical = mediumSpace,
                            )
                            .weight(1f),
                        onClick = {
                            onDismiss()
                            onAnswerYes()
                        },
                    ) {
                        Text(
                            text = yesLabel,
                            style = MaterialTheme.typography.bodyNormalBold.copy(color = MainColor)
                        )
                    }
                }
            }
        }
    }
}