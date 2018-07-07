package nhdphuong.com.manga.data.entity.book

import com.google.gson.annotations.SerializedName
import nhdphuong.com.manga.Constants
import java.io.Serializable

/*
 * Created by nhdphuong on 3/24/18.
 */
class Tag(@field:SerializedName(Constants.ID) val tagId: Long,
          @field:SerializedName(Constants.TYPE) val type: String,
          @field:SerializedName(Constants.NAME) val name: String,
          @field:SerializedName(Constants.URL) val url: String,
          @field:SerializedName(Constants.COUNT) val count: Long): Serializable