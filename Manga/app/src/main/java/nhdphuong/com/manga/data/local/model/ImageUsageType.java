package nhdphuong.com.manga.data.local.model;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static nhdphuong.com.manga.data.local.model.ImageUsageType.COVER;
import static nhdphuong.com.manga.data.local.model.ImageUsageType.THUMBNAIL;
import static nhdphuong.com.manga.data.local.model.ImageUsageType.BOOK_PAGE;

@StringDef({COVER, THUMBNAIL, BOOK_PAGE})
@Retention(RetentionPolicy.SOURCE)
public @interface ImageUsageType {
    String COVER = "cover";
    String THUMBNAIL = "thumbnail";
    String BOOK_PAGE = "bookPage";
}
