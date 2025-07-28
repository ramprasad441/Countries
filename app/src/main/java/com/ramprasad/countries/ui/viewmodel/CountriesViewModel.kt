package com.ramprasad.countries.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ramprasad.countries.commons.CoroutineDispatcherProvider
import com.ramprasad.countries.domain.model.ResponseState
import com.ramprasad.countries.domain.usecase.CountriesUseCase
import kotlinx.coroutines.launch

/**
 * Created by Ramprasad on 7/6/25.
 */
class CountriesViewModel(
    val useCase: CountriesUseCase,
    private val dispatcherProvider: CoroutineDispatcherProvider
) : ParentViewModel() {

    private val _countries: MutableLiveData<ResponseState> =
        MutableLiveData(ResponseState.LOADING())
    val countries: LiveData<ResponseState> get() = _countries

    init {
        getListOfAllCountries()
    }

    fun getListOfAllCountries() {
        viewModelSafeScope.launch(dispatcherProvider.io) {
            useCase.getAllCountries().collect {
                _countries.postValue(it)
            }
        }
    }
}