package nhdphuong.com.manga.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface InstallationApiService {
    @GET
    @Streaming
    fun downloadRemoteFile(@Url fileUrl: String): Call<ResponseBody>

}