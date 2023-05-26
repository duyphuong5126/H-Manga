package com.nonoka.nhentai.feature.reader

enum class ReaderType(val typeCode: Int) {
    HorizontalPage(1),
    VerticalScroll(2),
    ReversedHorizontalPage(3);

    companion object {
        @JvmStatic
        fun fromTypeCode(typeCode: Int): ReaderType {
            return values().firstOrNull { it.typeCode == typeCode }
                ?: throw IllegalStateException("No type matches the code $typeCode")
        }
    }
}