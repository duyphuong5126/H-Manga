package com.nonoka.nhentai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nonoka.nhentai.R

val notoSansFontFamily = FontFamily(
    Font(R.font.notosans_regular, weight = FontWeight.Thin),
    Font(R.font.notosans_italic, weight = FontWeight.Thin, style = FontStyle.Italic),
    Font(R.font.notosans_regular, weight = FontWeight.ExtraLight),
    Font(
        R.font.notosans_italic,
        weight = FontWeight.ExtraLight,
        style = FontStyle.Italic
    ),
    Font(R.font.notosans_regular, weight = FontWeight.Light),
    Font(R.font.notosans_italic, weight = FontWeight.Light, style = FontStyle.Italic),
    Font(R.font.notosans_regular, weight = FontWeight.Normal),
    Font(R.font.notosans_italic, weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(R.font.notosans_bold, weight = FontWeight.SemiBold),
    Font(R.font.notosans_bolditalic, weight = FontWeight.SemiBold, style = FontStyle.Italic),
    Font(R.font.notosans_bold, weight = FontWeight.Medium),
    Font(R.font.notosans_bolditalic, weight = FontWeight.Medium, style = FontStyle.Italic),
    Font(R.font.notosans_bold, weight = FontWeight.Bold),
    Font(R.font.notosans_bolditalic, weight = FontWeight.Bold, style = FontStyle.Italic),
    Font(R.font.notosans_bold, weight = FontWeight.ExtraBold),
    Font(
        R.font.notosans_bolditalic,
        weight = FontWeight.ExtraBold,
        style = FontStyle.Italic
    ),
)

val Typography.doujinshiPrimaryTitleStyle: TextStyle
    get() = TextStyle(
        fontFamily = notoSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    )

val Typography.readerTitleStyle: TextStyle
    get() = TextStyle(
        fontFamily = notoSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        fontStyle = FontStyle.Italic
    )

val Typography.doujinshiSecondaryTitleStyle: TextStyle
    get() = TextStyle(
        fontFamily = notoSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )

val Typography.bodyNormalBold: TextStyle
    get() = TextStyle(
        fontFamily = notoSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    )

val Typography.bodyNormalRegular: TextStyle
    get() = TextStyle(
        fontFamily = notoSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )

val Typography.bodySmallBold: TextStyle
    get() = TextStyle(
        fontFamily = notoSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
    )

val Typography.captionRegular: TextStyle
    get() = TextStyle(
        fontFamily = notoSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )

@Composable
fun MaterialTheme.brandTypography() = typography.copy(
    displayLarge = TextStyle(
        lineHeight = 64.sp,
        fontSize = 57.sp,
        letterSpacing = 0.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = notoSansFontFamily,
    ),
    displayMedium = TextStyle(
        lineHeight = 52.sp,
        fontSize = 45.sp,
        letterSpacing = 0.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = notoSansFontFamily,
    ),
    displaySmall = TextStyle(
        lineHeight = 44.sp,
        fontSize = 36.sp,
        letterSpacing = 0.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = notoSansFontFamily,
    ),
    headlineLarge = TextStyle(
        lineHeight = 40.sp,
        fontSize = 32.sp,
        letterSpacing = 0.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = notoSansFontFamily,
    ),
    headlineMedium = TextStyle(
        lineHeight = 36.sp,
        fontSize = 28.sp,
        letterSpacing = 0.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = notoSansFontFamily,
    ),
    headlineSmall = TextStyle(
        lineHeight = 32.sp,
        fontSize = 24.sp,
        letterSpacing = 0.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = notoSansFontFamily,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        lineHeight = 28.sp,
        fontSize = 22.sp,
        letterSpacing = 0.sp,
        fontFamily = notoSansFontFamily,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        lineHeight = 24.sp,
        fontSize = 16.sp,
        letterSpacing = 0.15.sp,
        fontFamily = notoSansFontFamily,
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp,
        fontSize = 14.sp,
        letterSpacing = 0.1.sp,
        fontFamily = notoSansFontFamily,
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        lineHeight = 24.sp,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp,
        fontFamily = notoSansFontFamily,
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
        fontSize = 14.sp,
        letterSpacing = 0.25.sp,
        fontFamily = notoSansFontFamily,
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp,
        fontSize = 12.sp,
        letterSpacing = 0.4.sp,
        fontFamily = notoSansFontFamily,
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp,
        fontSize = 14.sp,
        letterSpacing = 0.1.sp,
        fontFamily = notoSansFontFamily,
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp,
        fontSize = 12.sp,
        letterSpacing = 0.5.sp,
        fontFamily = notoSansFontFamily,
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp,
        fontFamily = notoSansFontFamily,
    ),
)