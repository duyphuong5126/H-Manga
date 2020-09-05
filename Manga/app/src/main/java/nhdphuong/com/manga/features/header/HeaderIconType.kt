package nhdphuong.com.manga.features.header

import java.lang.RuntimeException

enum class HeaderIconType(val typeCode: Int) {
    Logo(1),
    Back(2);

    companion object {
        fun fromTypeCode(typeCode: Int): HeaderIconType {
            return values().firstOrNull {
                it.typeCode == typeCode
            } ?: throw RuntimeException("Invalid type code")
        }
    }
}
