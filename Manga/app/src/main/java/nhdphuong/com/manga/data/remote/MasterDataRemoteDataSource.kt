package nhdphuong.com.manga.data.remote

import io.reactivex.Single
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.api.MasterDataApiService
import nhdphuong.com.manga.data.MasterDataSource
import nhdphuong.com.manga.data.entity.alternativedomain.AlternativeDomainGroup
import nhdphuong.com.manga.data.entity.appversion.AppVersionInfo
import nhdphuong.com.manga.data.entity.appversion.LatestAppVersion
import nhdphuong.com.manga.data.entity.book.tags.Artist
import nhdphuong.com.manga.data.entity.book.tags.Category
import nhdphuong.com.manga.data.entity.book.tags.Tag
import nhdphuong.com.manga.data.entity.book.tags.Character
import nhdphuong.com.manga.data.entity.book.tags.Group
import nhdphuong.com.manga.data.entity.book.tags.Language
import nhdphuong.com.manga.data.entity.book.tags.Parody
import nhdphuong.com.manga.data.entity.book.tags.UnknownTag
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MasterDataRemoteDataSource(private val masterDataApiService: MasterDataApiService) :
    MasterDataSource.Remote {
    private val logger: Logger by lazy {
        Logger("TagRemoteDataSource")
    }

    override suspend fun fetchArtistsList(onSuccess: (List<Artist>?) -> Unit, onError: () -> Unit) {
        masterDataApiService.getArtistsList().enqueue(object : Callback<List<Artist>> {
            override fun onFailure(call: Call<List<Artist>>, t: Throwable) {
                logger.e("Artists list fetching failed with error=$t")
                onError()
            }

            override fun onResponse(call: Call<List<Artist>>, response: Response<List<Artist>>) {
                logger.d("Artists list fetching completed")
                onSuccess(response.body())
            }
        })
    }

    override suspend fun fetchCharactersList(
        onSuccess: (List<Character>?) -> Unit,
        onError: () -> Unit
    ) {
        masterDataApiService.getCharactersList().enqueue(object : Callback<List<Character>> {
            override fun onFailure(call: Call<List<Character>>, t: Throwable) {
                logger.e("Characters list fetching failed with error=$t")
                onError()
            }

            override fun onResponse(
                call: Call<List<Character>>,
                response: Response<List<Character>>
            ) {
                logger.d("Characters list fetching completed")
                onSuccess(response.body())
            }
        })
    }

    override suspend fun fetchCategoriesList(
        onSuccess: (List<Category>?) -> Unit,
        onError: () -> Unit
    ) {
        masterDataApiService.getCategoriesList().enqueue(object : Callback<List<Category>> {
            override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                logger.e("Categories list fetching failed with error=$t")
                onError()
            }

            override fun onResponse(
                all: Call<List<Category>>,
                response: Response<List<Category>>
            ) {
                logger.d("Categories list fetching completed")
                onSuccess(response.body())
            }
        })
    }

    override suspend fun fetchGroupsList(onSuccess: (List<Group>?) -> Unit, onError: () -> Unit) {
        masterDataApiService.getGroupsList().enqueue(object : Callback<List<Group>> {
            override fun onFailure(call: Call<List<Group>>, t: Throwable) {
                logger.e("Groups list fetching failed with error=$t")
                onError()
            }

            override fun onResponse(call: Call<List<Group>>, response: Response<List<Group>>) {
                logger.d("Groups list fetching completed")
                onSuccess(response.body())
            }
        })
    }

    override suspend fun fetchParodiesList(
        onSuccess: (List<Parody>?) -> Unit,
        onError: () -> Unit
    ) {
        masterDataApiService.getParodiesList().enqueue(object : Callback<List<Parody>> {
            override fun onFailure(call: Call<List<Parody>>, t: Throwable) {
                logger.e("Parodies list fetching failed with error=$t")
                onError()
            }

            override fun onResponse(call: Call<List<Parody>>, response: Response<List<Parody>>) {
                logger.d("Parodies list fetching completed")
                onSuccess(response.body())
            }
        })
    }

    override suspend fun fetchLanguagesList(
        onSuccess: (List<Language>?) -> Unit,
        onError: () -> Unit
    ) {
        masterDataApiService.getLanguagesList().enqueue(object : Callback<List<Language>> {
            override fun onFailure(call: Call<List<Language>>, t: Throwable) {
                logger.e("Languages list fetching failed with error=$t")
                onError()
            }

            override fun onResponse(
                call: Call<List<Language>>,
                response: Response<List<Language>>
            ) {
                logger.d("Languages list fetching completed")
                onSuccess(response.body())
            }
        })
    }

    override suspend fun fetchTagsList(onSuccess: (List<Tag>?) -> Unit, onError: () -> Unit) {
        masterDataApiService.getTagsList().enqueue(object : Callback<List<Tag>> {
            override fun onFailure(call: Call<List<Tag>>, t: Throwable) {
                logger.e("Tags list fetching failed with error=$t")
                onError()
            }

            override fun onResponse(call: Call<List<Tag>>, response: Response<List<Tag>>) {
                logger.d("Tags list fetching completed")
                onSuccess(response.body())
            }
        })
    }

    override suspend fun fetchUnknownTypesList(
        onSuccess: (List<UnknownTag>?) -> Unit,
        onError: () -> Unit
    ) {
        masterDataApiService.getUnknownTagsList().enqueue(object : Callback<List<UnknownTag>> {
            override fun onFailure(call: Call<List<UnknownTag>>, t: Throwable) {
                logger.e("UnknownTag list fetching failed with error=$t")
                onError()
            }

            override fun onResponse(
                call: Call<List<UnknownTag>>,
                response: Response<List<UnknownTag>>
            ) {
                logger.d("UnknownTag list fetching completed")
                onSuccess(response.body())
            }
        })
    }

    override suspend fun fetchTagDataVersion(onSuccess: (Long) -> Unit, onError: () -> Unit) {
        masterDataApiService.getTagDataVersion().enqueue(object : Callback<Long> {
            override fun onFailure(call: Call<Long>, t: Throwable) {
                logger.e("Current version fetching failed with error=$t")
                onError()
            }

            override fun onResponse(call: Call<Long>, response: Response<Long>) {
                response.body()?.run(onSuccess)
            }
        })
    }

    override suspend fun fetchAppVersion(
        onSuccess: (LatestAppVersion) -> Unit,
        onError: (error: Throwable) -> Unit
    ) {
        masterDataApiService.getLatestAppVersion().enqueue(object : Callback<LatestAppVersion> {
            override fun onResponse(
                call: Call<LatestAppVersion>,
                response: Response<LatestAppVersion>
            ) {
                response.body()?.run(onSuccess)
            }

            override fun onFailure(call: Call<LatestAppVersion>, t: Throwable) {
                onError.invoke(t)
            }
        })
    }

    override fun getVersionHistory(): Single<List<AppVersionInfo>> {
        return Single.create {
            masterDataApiService.getVersionHistory()
                .enqueue(object : Callback<List<AppVersionInfo>> {
                    override fun onResponse(
                        call: Call<List<AppVersionInfo>>,
                        response: Response<List<AppVersionInfo>>
                    ) {
                        response.body()?.run(it::onSuccess)
                    }

                    override fun onFailure(call: Call<List<AppVersionInfo>>, t: Throwable) {
                        it.onError(t)
                    }
                })
        }
    }

    override fun fetchAlternativeDomains(): Single<AlternativeDomainGroup> {
        return Single.create {
            masterDataApiService.getAlternativeDomains()
                .enqueue(object : Callback<AlternativeDomainGroup> {
                    override fun onResponse(
                        call: Call<AlternativeDomainGroup>,
                        response: Response<AlternativeDomainGroup>
                    ) {
                        if (response.isSuccessful) {
                            response.body()?.let(it::onSuccess)
                        } else {
                            it.onError(IllegalStateException("Not successful in onResponse"))
                        }
                    }

                    override fun onFailure(call: Call<AlternativeDomainGroup>, t: Throwable) {
                        it.onError(t)
                    }
                })
        }
    }

    override fun fetchFeedbackFormUrl(): Single<String> {
        return Single.create {
            masterDataApiService.getFeedbackFormUrl().enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    response.body()?.takeIf(String::isNotBlank)?.let(it::onSuccess)
                        ?: it.onError(IllegalStateException("Null body response"))
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    it.onError(t)
                }
            })
        }
    }
}
