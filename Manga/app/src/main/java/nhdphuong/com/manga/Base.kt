package nhdphuong.com.manga

/*
 * Created by nhdphuong on 3/18/18.
 */
interface Base {
    interface View<in T : Presenter> {
        fun setPresenter(presenter: T)
        fun showLoading()
        fun hideLoading()
        fun isActive(): Boolean
    }

    interface Presenter {
        fun start()
        fun stop()
    }
}