package nhdphuong.com.manga.features.recent;

/*
 * Created by nhdphuong on 6/10/18.
 */

import androidx.annotation.StringDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import nhdphuong.com.manga.Constants;

@StringDef(value = {Constants.RECENT, Constants.FAVORITE})
@Retention(RetentionPolicy.SOURCE)
public @interface RecentType {
}
