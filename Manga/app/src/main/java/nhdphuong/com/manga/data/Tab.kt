package nhdphuong.com.manga.data

import nhdphuong.com.manga.Constants

/*
 * Created by nhdphuong on 3/17/18.
 */
enum class Tab(val defaultName: String) {
    RECENT(Constants.RECENT),
    FAVORITE(Constants.FAVORITE),

    RANDOM(Constants.RANDOM),
    ARTISTS(Constants.ARTISTS),
    TAGS(Constants.TAGS),
    CHARACTERS(Constants.CHARACTERS),
    GROUPS(Constants.GROUPS),
    PARODIES(Constants.PARODIES),
    INFO(Constants.INFO),

    NONE(Constants.RANDOM);

    companion object {
        fun fromString(name: String): Tab {
            var result = NONE
            for (value in Tab.values()) {
                if (value.defaultName == name) {
                    result = value
                    break
                }
            }
            return result
        }
    }

    val label
        get() = defaultName
}