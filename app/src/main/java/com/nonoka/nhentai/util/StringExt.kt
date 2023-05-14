package com.nonoka.nhentai.util

import java.util.Locale

fun String.capitalized(): String =
    this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }