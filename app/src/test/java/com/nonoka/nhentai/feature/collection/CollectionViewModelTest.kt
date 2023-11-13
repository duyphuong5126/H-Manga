package com.nonoka.nhentai.feature.collection

import com.nonoka.nhentai.domain.DoujinshiRepository
import com.nonoka.nhentai.mock.MockDispatcher
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when` as whenever
import com.nonoka.nhentai.TestDataProvider
import com.nonoka.nhentai.domain.entity.GalleryPageNotExistException
import com.nonoka.nhentai.domain.entity.doujinshi.DoujinshisResult
import com.nonoka.nhentai.ui.shared.model.GalleryUiState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

class CollectionViewModelTest {

    private val doujinshiRepository: DoujinshiRepository = mock()

    private val viewModel = CollectionViewModel(doujinshiRepository, MockDispatcher())

    private suspend fun setUpGalleryResult(
        pageIndex: Int,
        exist: Boolean = true,
        emptyGallery: Boolean = false,
        fails: Boolean = false,
    ) {
        if (exist) {
            if (fails) {
                whenever(doujinshiRepository.getCollectionPage(pageIndex)).then {
                    throw NullPointerException()
                }
            } else {
                whenever(doujinshiRepository.getCollectionPage(pageIndex)).thenReturn(
                    DoujinshisResult(
                        if (emptyGallery) emptyList() else TestDataProvider.providesDoujinshiList(25),
                        numOfPages = if (emptyGallery) 0 else 10,
                        numOfBooksPerPage = if (emptyGallery) 0 else 25
                    )
                )
            }
        } else {
            whenever(doujinshiRepository.getCollectionPage(pageIndex)).then {
                throw GalleryPageNotExistException()
            }
        }

        if (!emptyGallery) {
            whenever(doujinshiRepository.getCollectionSize()).thenReturn(250)
        }
    }

    @Test
    fun `loadPage, empty gallery`() {
        runBlocking {
            setUpGalleryResult(pageIndex = 0, emptyGallery = true)

            val result = viewModel.loadPage(0)
            assertTrue(result.isEmpty())

            assertEquals(viewModel.collectionCountLabel.value, "")

            verify(doujinshiRepository).getCollectionPage(0)
            verify(doujinshiRepository, never()).getCollectionSize()
        }
    }

    @Test
    fun `loadPage, not empty gallery`() {
        runBlocking {
            setUpGalleryResult(pageIndex = 0)

            val result = viewModel.loadPage(0)
            assertTrue(result.isNotEmpty())

            val firstItem = result[0]
            assertTrue(firstItem is GalleryUiState.Title)
            assertEquals((firstItem as GalleryUiState.Title).title, "Page 1")

            verify(doujinshiRepository).getCollectionPage(0)
            verify(doujinshiRepository).getCollectionSize()
        }
    }

    @Test
    fun `loadPage, page does not exist`() {
        runBlocking {
            setUpGalleryResult(pageIndex = 1000, exist = false, emptyGallery = true)

            try {
                viewModel.loadPage(1000)
            } catch (error: Throwable) {
                assertTrue(error is GalleryPageNotExistException)
            }

            verify(doujinshiRepository).getCollectionPage(1000)
            verify(doujinshiRepository, never()).getCollectionSize()
        }
    }

    @Test
    fun `loadPage, fails with an exception not GalleryPageNotExistException`() {
        runBlocking {
            setUpGalleryResult(pageIndex = 0, exist = true, fails = true)

            val result = viewModel.loadPage(0)
            assertTrue(result.isEmpty())

            assertEquals(viewModel.collectionCountLabel.value, "")

            verify(doujinshiRepository).getCollectionPage(0)
            verify(doujinshiRepository, never()).getCollectionSize()
        }
    }
}