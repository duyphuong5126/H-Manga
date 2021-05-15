package nhdphuong.com.manga.data

import nhdphuong.com.manga.Constants

/*
 * Created by nhdphuong on 3/17/18.
 */
enum class Tab(val defaultName: String) {
    RECENT(Constants.RECENT),
    FAVORITE(Constants.FAVORITE),
    DOWNLOADED(Constants.DOWNLOADED),
    DOWNLOADING(Constants.DOWNLOADING),

    RANDOM(Constants.RANDOM),
    ARTISTS(Constants.ARTISTS),
    TAGS(Constants.TAGS),
    CHARACTERS(Constants.CHARACTERS),
    GROUPS(Constants.GROUPS),
    PARODIES(Constants.PARODIES),
    INFO(Constants.INFO),

    SETTINGS(Constants.SETTINGS),
    FEEDBACK(Constants.FEEDBACK),
    ADMIN(Constants.ADMIN),

    NONE(Constants.RANDOM);

    companion object {
        fun fromString(name: String): Tab {
            return values().firstOrNull { it.defaultName == name } ?: NONE
        }
    }

}
