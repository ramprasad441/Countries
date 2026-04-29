package com.ramprasad.countries.ui.viewmodel

import com.ramprasad.countries.commons.CoroutineDispatcherProvider
import com.ramprasad.countries.domain.usecase.CountriesUseCase
import io.mockk.mockk
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

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
        assertTrue(viewModel is CountriesViewModel)
    }

    @Test
    fun `created CountriesViewModel is not null`() {
        val viewModel = factory.create(CountriesViewModel::class.java)
        assertNotNull(viewModel)
    }
}
