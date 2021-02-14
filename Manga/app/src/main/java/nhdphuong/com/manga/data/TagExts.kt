package nhdphuong.com.manga.data

import nhdphuong.com.manga.data.entity.book.tags.Artist
import nhdphuong.com.manga.data.entity.book.tags.Category
import nhdphuong.com.manga.data.entity.book.tags.Character
import nhdphuong.com.manga.data.entity.book.tags.Group
import nhdphuong.com.manga.data.entity.book.tags.ITag
import nhdphuong.com.manga.data.entity.book.tags.Language
import nhdphuong.com.manga.data.entity.book.tags.Parody
import nhdphuong.com.manga.data.entity.book.tags.Tag

fun ITag.toTag(): Tag {
    return Tag(id, type, name, url, count)
}

fun ITag.toArtist(): Artist {
    return Artist(id, type, name, url, count)
}

fun ITag.toCharacter(): Character {
    return Character(id, type, name, url, count)
}

fun ITag.toParody(): Parody {
    return Parody(id, type, name, url, count)
}

fun ITag.toGroup(): Group {
    return Group(id, type, name, url, count)
}

fun ITag.toLanguage(): Language {
    return Language(id, type, name, url, count)
}

fun ITag.toCategory(): Category {
    return Category(id, type, name, url, count)
}
