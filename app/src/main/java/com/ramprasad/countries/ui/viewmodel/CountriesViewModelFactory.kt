package com.ramprasad.countries.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ramprasad.countries.commons.CoroutineDispatcherProvider
import com.ramprasad.countries.domain.usecase.CountriesUseCase

/**
 * Created by Ramprasad on 7/7/25.
 */
class CountriesViewModelFactory(
    private val useCase: CountriesUseCase,
    private val dispatcher: CoroutineDispatcherProvider
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CountriesViewModel(useCase, dispatcher) as T
    }
}