package nhdphuong.com.manga.data.repository.installation

import io.reactivex.Single
import nhdphuong.com.manga.api.InstallationApiService
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

interface InstallationRemoteDataSource {
    fun downloadFile(url: String, outputDirectory: String, versionCode: String): Single<String>
}

class InstallationRemoteDataSourceImpl(
    private val apiService: InstallationApiService
) : InstallationRemoteDataSource {
    override fun downloadFile(
        url: String,
        outputDirectory: String,
        versionCode: String
    ): Single<String> {
        return Single.create {
            apiService.downloadRemoteFile(url).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        it.onSuccess(writeFileAndReturnPath(body, outputDirectory, versionCode))
                    } else {
                        it.onError(IllegalStateException("Not success or null body in onResponse"))
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    it.onError(t)
                }
            })
        }
    }

    private fun writeFileAndReturnPath(
        responseBody: ResponseBody,
        outputDirectory: String,
        versionCode: String
    ): String {
        val dirs = File(outputDirectory)
        if (!dirs.mkdirs()) {
            return ""
        }

        val outputFilePath = "$outputDirectory/v${versionCode}.apk"
        val output = File(outputFilePath)
        output.createNewFile()
        val inputStream = responseBody.byteStream()
        val outputStream = FileOutputStream(output)

        val fileReader = ByteArray(BUFFER_SIZE)
        var read = inputStream.read(fileReader)
        while (read >= 0) {
            outputStream.write(fileReader, 0, read)
            read = inputStream.read(fileReader)
        }
        outputStream.flush()

        inputStream.close()
        outputStream.close()

        return output.path
    }

    companion object {
        private const val BUFFER_SIZE = 4096
    }
}