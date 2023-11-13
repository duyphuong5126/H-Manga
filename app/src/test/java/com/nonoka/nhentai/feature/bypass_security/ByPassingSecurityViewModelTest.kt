package com.nonoka.nhentai.feature.bypass_security

import com.nonoka.nhentai.TestDataProvider
import com.nonoka.nhentai.mock.MockDispatcher
import org.junit.Assert.assertEquals
import org.junit.Test


class ByPassingSecurityViewModelTest {
    private val viewModel = ByPassingSecurityViewModel(MockDispatcher())

    @Test
    fun `validateData, json string is valid`() {
        val json = TestDataProvider.providesGalleryPageJson()
        viewModel.validateData(json)

        assertEquals(viewModel.byPassingResult.value, ByPassingResult.Success)
    }

    @Test
    fun `validateData, json string is empty`() {
        viewModel.validateData("")

        assertEquals(viewModel.byPassingResult.value, ByPassingResult.Failure)
    }

    @Test
    fun `validateData, json string is invalid`() {
        viewModel.validateData("invalid json")

        assertEquals(viewModel.byPassingResult.value, ByPassingResult.Failure)
    }

    @Test
    fun onRetry() {
        viewModel.onRetry()

        assertEquals(viewModel.byPassingResult.value, ByPassingResult.Loading)
    }

    @Test
    fun onError() {
        viewModel.onError("No internet connection")

        assertEquals(viewModel.byPassingResult.value, ByPassingResult.Failure)
    }
}