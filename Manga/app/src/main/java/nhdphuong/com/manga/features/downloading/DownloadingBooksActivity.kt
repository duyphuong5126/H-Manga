package nhdphuong.com.manga.features.downloading

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.broadcastreceiver.BroadCastReceiverHelper
import nhdphuong.com.manga.databinding.ActivityDownloadingBooksBinding
import nhdphuong.com.manga.features.downloaded.DownloadedBooksActivity
import nhdphuong.com.manga.features.downloading.uimodel.PendingDownloadItemUiModel
import nhdphuong.com.manga.features.downloading.view.PendingListAdapter
import nhdphuong.com.manga.supports.SpaceItemDecoration
import nhdphuong.com.manga.views.becomeVisible
import nhdphuong.com.manga.views.createLoadingDialog
import nhdphuong.com.manga.views.gone
import nhdphuong.com.manga.views.showCancelDownloadingBookConfirmationDialog
import javax.inject.Inject

class DownloadingBooksActivity : AppCompatActivity(), DownloadingBooksContract.View,
    View.OnClickListener, PendingListAdapter.PendingListActionListener {
    private lateinit var viewBinding: ActivityDownloadingBooksBinding
    private val ibBack: ImageButton get() = viewBinding.ibBack
    private val pendingList: RecyclerView get() = viewBinding.pendingList
    private val clNothing: ConstraintLayout get() = viewBinding.clNothing
    private val buttonGoToDownloadedList: ImageButton get() = viewBinding.buttonGoToDownloadedList
    private lateinit var loadingDialog: Dialog

    private var pendingListAdapter: PendingListAdapter? = null

    private val logger = Logger("DownloadingBooksActivity")

    private val bookDownloadingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Constants.ACTION_DOWNLOADING_STARTED -> {
                    val bookId = intent.extras?.getString(Constants.BOOK_ID).orEmpty()
                    presenter.updateDownloadingStarted(bookId)
                }
                Constants.ACTION_DOWNLOADING_PROGRESS -> {
                    val bookId = intent.extras?.getString(Constants.BOOK_ID).orEmpty()
                    val totalBookPages = intent.extras?.getInt(Constants.TOTAL) ?: 0
                    val progress = intent.extras?.getInt(Constants.PROGRESS) ?: 0
                    presenter.updateDownloadingProgress(bookId, progress, totalBookPages)
                }
                Constants.ACTION_DOWNLOADING_COMPLETED -> {
                    val bookId = intent.extras?.getString(Constants.BOOK_ID).orEmpty()
                    presenter.updateDownloadingCompleted(bookId)
                }
                Constants.ACTION_DOWNLOADING_FAILED -> {
                    val bookId = intent.extras?.getString(Constants.BOOK_ID).orEmpty()
                    val totalBookPages = intent.extras?.getInt(Constants.TOTAL) ?: 0
                    val downloadingFailedCount =
                        intent.extras?.getInt(Constants.DOWNLOADING_FAILED_COUNT) ?: 0
                    presenter.updateDownloadFailure(bookId, downloadingFailedCount, totalBookPages)
                }
                else -> Unit
            }
        }
    }

    @Inject
    lateinit var presenter: DownloadingBooksContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NHentaiApp.instance.applicationComponent.plus(DownloadingBooksModule(this))
            .inject(this)

        viewBinding = ActivityDownloadingBooksBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        ibBack.setOnClickListener(this)
        buttonGoToDownloadedList.setOnClickListener(this)

        loadingDialog = createLoadingDialog()

        presenter.start()
    }

    override fun onResume() {
        super.onResume()
        BroadCastReceiverHelper.registerBroadcastReceiver(
            this,
            bookDownloadingReceiver,
            Constants.ACTION_DOWNLOADING_STARTED,
            Constants.ACTION_DOWNLOADING_PROGRESS,
            Constants.ACTION_DOWNLOADING_COMPLETED,
            Constants.ACTION_DOWNLOADING_FAILED
        )
    }

    override fun onPause() {
        super.onPause()
        BroadCastReceiverHelper.unRegisterBroadcastReceiver(this, bookDownloadingReceiver)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ibBack -> onBackPressed()
            R.id.buttonGoToDownloadedList -> {
                DownloadedBooksActivity.start(this)
            }
        }
    }

    override fun setUpPendingDownloadList(pendingDownloadList: List<PendingDownloadItemUiModel>) {
        pendingListAdapter = PendingListAdapter(this, pendingDownloadList, this)
        pendingList.adapter = pendingListAdapter
        pendingList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        pendingList.addItemDecoration(
            SpaceItemDecoration(
                this,
                R.dimen.space_medium,
                showFirstDivider = false,
                showLastDivider = false
            )
        )
    }

    override fun updatePendingList(newPendingDownloadList: List<PendingDownloadItemUiModel>) {
        pendingListAdapter?.updateList(newPendingDownloadList)
    }

    override fun updateDownloadingStarted(position: Int) {
        pendingListAdapter?.updateStartedStatus(position)
    }

    override fun updateProgress(position: Int, progress: Int, total: Int) {
        pendingListAdapter?.updateProgress(position, progress, total)
    }

    override fun updateCompletion(position: Int) {
        pendingListAdapter?.updateCompletion(position)
    }

    override fun updateFailure(position: Int, failureCount: Int, total: Int) {
        pendingListAdapter?.updateFailureMessage(position, failureCount, total)
    }

    override fun hideNothingView() {
        clNothing.gone()
    }

    override fun showNothingView() {
        clNothing.becomeVisible()
    }

    override fun showLoading() {
        if (isActive()) {
            loadingDialog.show()
        }
    }

    override fun hideLoading() {
        if (isActive()) {
            loadingDialog.dismiss()
        }
    }

    override fun removeBook(bookId: String) {
        logger.d("Request removal of book $bookId")
        showCancelDownloadingBookConfirmationDialog(bookId, onOk = {
            presenter.removePendingItem(bookId)
        })
    }

    override fun isActive(): Boolean {
        return lifecycle.currentState != Lifecycle.State.DESTROYED
    }

    companion object {
        @JvmStatic
        fun start(fromContext: Context) {
            fromContext.startActivity(Intent(fromContext, DownloadingBooksActivity::class.java))
        }
    }
}