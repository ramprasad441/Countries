package com.ramprasad.countries.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.ramprasad.countries.commons.CoroutineDispatcherProvider
import com.ramprasad.countries.domain.model.Countries
import com.ramprasad.countries.domain.model.ResponseState
import com.ramprasad.countries.domain.usecase.CountriesUseCase
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Created by Ramprasad on 7/6/25.
 */
@ExperimentalCoroutinesApi
class CountriesViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var useCase: CountriesUseCase
    private lateinit var targetTest: CountriesViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        useCase = mockk()
        targetTest =
            CountriesViewModel(
                useCase,
                CoroutineDispatcherProvider(
                    main = testDispatcher,
                    io = testDispatcher,
                    default = testDispatcher,
                ),
            )
    }

    @After
    fun teardown() {
        clearAllMocks()
        Dispatchers.resetMain()
    }

    @Test
    fun `get countries returns error`() {
        every { useCase.getAllCountries() } returns
            flowOf(
                ResponseState.ERROR(Throwable("Error")),
            )

        val stateList = mutableListOf<ResponseState>()
        targetTest.countries.observeForever { stateList.add(it) }

        targetTest.getListOfAllCountries()
        testScope.advanceUntilIdle()

        assertThat(stateList).hasSize(3)
        assertThat(stateList[0]).isInstanceOf(ResponseState.LOADING::class.java)
        assertThat(stateList[1]).isInstanceOf(ResponseState.ERROR::class.java)
        assertThat((stateList[1] as ResponseState.ERROR).error.message).isEqualTo("Error")
    }

    @Test
    fun `get countries returns loading`() {
        every { useCase.getAllCountries() } returns
            flowOf(
                ResponseState.LOADING(),
            )

        val stateList = mutableListOf<ResponseState>()
        targetTest.countries.observeForever { stateList.add(it) }

        targetTest.getListOfAllCountries()
        testScope.advanceUntilIdle()

        assertThat(stateList).hasSize(3)
        assertThat(stateList[0]).isInstanceOf(ResponseState.LOADING::class.java)
        assertThat(stateList[1]).isInstanceOf(ResponseState.LOADING::class.java)
        assertThat((stateList[1] as ResponseState.LOADING).isLoading).isTrue()
    }

    @Test
    fun `get countries returns success`() {
        val mockCountry = Countries(name = "TestLand", capital = "TestCity", code = "TL")
        every { useCase.getAllCountries() } returns
            flowOf(
                ResponseState.SUCCESS(listOf(mockCountry)),
            )

        val stateList = mutableListOf<ResponseState>()
        targetTest.countries.observeForever { stateList.add(it) }

        targetTest.getListOfAllCountries()
        testScope.advanceUntilIdle()

        assertThat(stateList).hasSize(3)
        assertThat(stateList[0]).isInstanceOf(ResponseState.LOADING::class.java)
        assertThat(stateList[1]).isInstanceOf(ResponseState.SUCCESS::class.java)
        assertThat((stateList[1] as ResponseState.SUCCESS).countries).containsExactly(mockCountry)
    }
}
