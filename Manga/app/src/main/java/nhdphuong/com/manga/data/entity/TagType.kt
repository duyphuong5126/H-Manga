package nhdphuong.com.manga.data.entity

import nhdphuong.com.manga.Constants.Companion.ARTIST
import nhdphuong.com.manga.Constants.Companion.CATEGORY
import nhdphuong.com.manga.Constants.Companion.CHARACTER
import nhdphuong.com.manga.Constants.Companion.GROUP
import nhdphuong.com.manga.Constants.Companion.LANGUAGE
import nhdphuong.com.manga.Constants.Companion.PARODY
import nhdphuong.com.manga.Constants.Companion.TAG

enum class TagType(val value: String) {
    Artist(ARTIST),
    Character(CHARACTER),
    Category(CATEGORY),
    Language(LANGUAGE),
    Parody(PARODY),
    Group(GROUP),
    OtherTag(TAG)
}