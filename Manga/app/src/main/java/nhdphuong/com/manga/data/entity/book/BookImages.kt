package nhdphuong.com.manga.data.entity.book

import com.google.gson.annotations.SerializedName
import nhdphuong.com.manga.Constants
import java.io.Serializable

/*
 * Created by nhdphuong on 3/24/18.
 */
class BookImages(@field:SerializedName(Constants.PAGES) val pages: List<ImageMeasurements>,
                 @field:SerializedName(Constants.COVER) val cover: ImageMeasurements,
                 @field:SerializedName(Constants.THUMBNAIL) val thumbnail: ImageMeasurements): Serializable