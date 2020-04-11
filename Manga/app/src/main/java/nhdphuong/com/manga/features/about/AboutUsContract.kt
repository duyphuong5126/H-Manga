package nhdphuong.com.manga.features.about

interface AboutUsContract {
    interface View {
        fun showTagDataVersion(versionCode: String)
    }

    interface Presenter {
        fun setUp()
        fun clear()
    }
}
