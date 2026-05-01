package com.ramprasad.countries.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.google.common.truth.Truth.assertThat
import com.ramprasad.countries.commons.CoroutineDispatcherProvider
import com.ramprasad.countries.domain.usecase.CountriesUseCase
import io.mockk.mockk
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Created by Ramprasad on 7/8/25.
 */
class CountriesViewModelFactoryTest {
    private lateinit var useCase: CountriesUseCase
    private lateinit var dispatcher: CoroutineDispatcherProvider
    private lateinit var factory: CountriesViewModelFactory

    @Before
    fun setUp() {
        useCase = mockk()
        dispatcher = CoroutineDispatcherProvider()
                factory = CountriesViewModelFactory(useCase, dispatcher)
    }

    @Test
    fun `create returns CountriesViewModel when correct class is passed`() {
        val viewModel = factory.create(CountriesViewModel::class.java)

        // This covers both null-check and type-check in one go
        assertThat(viewModel).isInstanceOf(CountriesViewModel::class.java)
    }

    @Test
    fun `created CountriesViewModel is not null`() {
        val viewModel = factory.create(CountriesViewModel::class.java)
        assertNotNull(viewModel)
    }

    @Test
    fun `create throws IllegalArgumentException when unknown ViewModel class is passed`() {
        class WrongViewModel : ViewModel()
        val unknownClass = WrongViewModel::class.java

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                factory.create(unknownClass)
            }

        assertEquals("Unknown ViewModel class: ${unknownClass.name}", exception.message)
    }
}
