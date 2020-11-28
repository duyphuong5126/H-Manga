package nhdphuong.com.manga.data.repository.installation

import io.reactivex.Single

interface InstallationRepository {
    fun downloadFile(url: String, outputDirectory: String, versionCode: String): Single<String>
}

class InstallationRepositoryImpl(
    private val remoteDataSource: InstallationRemoteDataSource
) : InstallationRepository {
    override fun downloadFile(
        url: String,
        outputDirectory: String,
        versionCode: String
    ): Single<String> {
        return remoteDataSource.downloadFile(url, outputDirectory, versionCode)
    }
}