package nhdphuong.com.manga.data;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import nhdphuong.com.manga.Constants;

/*
 * Created by nhdphuong on 5/13/18.
 */
@StringDef(value = {
        Constants.ARTISTS,
        Constants.CHARACTERS,
        Constants.PARODIES,
        Constants.GROUPS,
        Constants.TAGS
})
@Retention(RetentionPolicy.SOURCE)
public @interface Tag {

}
