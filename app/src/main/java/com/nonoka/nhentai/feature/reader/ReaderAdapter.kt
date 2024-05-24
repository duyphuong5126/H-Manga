package com.nonoka.nhentai.feature.reader

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.recyclerview.widget.RecyclerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nonoka.nhentai.R
import com.nonoka.nhentai.databinding.ReaderPageBinding
import com.nonoka.nhentai.ui.theme.Grey77
import com.nonoka.nhentai.ui.theme.MainColor
import com.nonoka.nhentai.ui.theme.headlineLargeStyle
import com.nonoka.nhentai.feature.reader.ReaderPageModel.RemotePage
import com.nonoka.nhentai.feature.reader.ReaderPageModel.LocalPage

/*
 * Created by nhdphuong on 5/5/18.
 */
class ReaderAdapter(
    private val pageList: List<ReaderPageModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PageViewHolder(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PageViewHolder -> holder.bindTo(pageList[position], position + 1)
        }
    }

    override fun getItemCount(): Int {
        return pageList.size
    }

    private class PageViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.reader_page, parent, false)
    ) {
        private val viewBinding = ReaderPageBinding.bind(itemView)

        fun bindTo(page: ReaderPageModel, pageIndex: Int) {
            viewBinding.root.setContent {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(page.width.toFloat() / page.height.toFloat())
                ) {
                    var isLoading by remember {
                        mutableStateOf(true)
                    }
                    val model: Any = when (page) {
                        is RemotePage -> page.url

                        is LocalPage -> ImageRequest.Builder(LocalContext.current)
                            .data(page.file)
                            .build()
                    }
                    AsyncImage(
                        modifier = Modifier.fillMaxSize(),
                        model = model,
                        contentDescription = "Page $pageIndex",
                        onSuccess = {
                            isLoading = false
                        }
                    )

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = Grey77),
                        ) {
                            Text(
                                modifier = Modifier
                                    .align(Alignment.Center),
                                text = "$pageIndex",
                                style = MaterialTheme.typography.headlineLargeStyle.copy(color = MainColor),
                            )
                        }
                    }
                }
            }
        }
    }
}
