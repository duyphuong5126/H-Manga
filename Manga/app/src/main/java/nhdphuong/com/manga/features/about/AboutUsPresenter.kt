package nhdphuong.com.manga.features.about

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.usecase.GetVersionCodeUseCase
import javax.inject.Inject

class AboutUsPresenter @Inject constructor(
    private val view: AboutUsContract.View,
    private val getVersionCodeUseCase: GetVersionCodeUseCase
) : AboutUsContract.Presenter {
    private val compositeDisposable = CompositeDisposable()

    override fun setUp() {
        getVersionCodeUseCase.execute()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onSuccess = { versionCode ->
                view.showTagDataVersion(versionCode.toString())
            }, onError = { error ->
                Logger.e(TAG, "Failed to get version code with error: $error")
            }).addTo(compositeDisposable)
    }

    override fun clear() {
        compositeDisposable.clear()
    }

    companion object {
        private const val TAG = "AboutUsPresenter"
    }
}
