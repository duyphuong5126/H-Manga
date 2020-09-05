package nhdphuong.com.manga.data.remote

import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.api.MasterDataApiService
import nhdphuong.com.manga.data.MasterDataSource
import nhdphuong.com.manga.data.entity.LatestAppVersion
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
    companion object {
        private const val TAG = "TagRemoteDataSource"
    }

    override suspend fun fetchArtistsList(onSuccess: (List<Artist>?) -> Unit, onError: () -> Unit) {
        masterDataApiService.getArtistsList().enqueue(object : Callback<List<Artist>> {
            override fun onFailure(call: Call<List<Artist>>, t: Throwable) {
                Logger.d(TAG, "Artists list fetching failed with error=$t")
                onError()
            }

            override fun onResponse(call: Call<List<Artist>>, response: Response<List<Artist>>) {
                Logger.d(TAG, "Artists list fetching completed")
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
                Logger.d(TAG, "Characters list fetching failed with error=$t")
                onError()
            }

            override fun onResponse(
                call: Call<List<Character>>,
                response: Response<List<Character>>
            ) {
                Logger.d(TAG, "Characters list fetching completed")
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
                Logger.d(TAG, "Categories list fetching failed with error=$t")
                onError()
            }

            override fun onResponse(
                all: Call<List<Category>>,
                response: Response<List<Category>>
            ) {
                Logger.d(TAG, "Categories list fetching completed")
                onSuccess(response.body())
            }
        })
    }

    override suspend fun fetchGroupsList(onSuccess: (List<Group>?) -> Unit, onError: () -> Unit) {
        masterDataApiService.getGroupsList().enqueue(object : Callback<List<Group>> {
            override fun onFailure(call: Call<List<Group>>, t: Throwable) {
                Logger.d(TAG, "Groups list fetching failed with error=$t")
                onError()
            }

            override fun onResponse(call: Call<List<Group>>, response: Response<List<Group>>) {
                Logger.d(TAG, "Groups list fetching completed")
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
                Logger.d(TAG, "Parodies list fetching failed with error=$t")
                onError()
            }

            override fun onResponse(call: Call<List<Parody>>, response: Response<List<Parody>>) {
                Logger.d(TAG, "Parodies list fetching completed")
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
                Logger.d(TAG, "Languages list fetching failed with error=$t")
                onError()
            }

            override fun onResponse(
                call: Call<List<Language>>,
                response: Response<List<Language>>
            ) {
                Logger.d(TAG, "Languages list fetching completed")
                onSuccess(response.body())
            }
        })
    }

    override suspend fun fetchTagsList(onSuccess: (List<Tag>?) -> Unit, onError: () -> Unit) {
        masterDataApiService.getTagsList().enqueue(object : Callback<List<Tag>> {
            override fun onFailure(call: Call<List<Tag>>, t: Throwable) {
                Logger.d(TAG, "Tags list fetching failed with error=$t")
                onError()
            }

            override fun onResponse(call: Call<List<Tag>>, response: Response<List<Tag>>) {
                Logger.d(TAG, "Tags list fetching completed")
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
                Logger.d(TAG, "UnknownTag list fetching failed with error=$t")
                onError()
            }

            override fun onResponse(
                call: Call<List<UnknownTag>>,
                response: Response<List<UnknownTag>>
            ) {
                Logger.d(TAG, "UnknownTag list fetching completed")
                onSuccess(response.body())
            }
        })
    }

    override suspend fun fetchTagDataVersion(onSuccess: (Long) -> Unit, onError: () -> Unit) {
        masterDataApiService.getTagDataVersion().enqueue(object : Callback<Long> {
            override fun onFailure(call: Call<Long>, t: Throwable) {
                Logger.d(TAG, "Current version fetching failed with error=$t")
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
}
