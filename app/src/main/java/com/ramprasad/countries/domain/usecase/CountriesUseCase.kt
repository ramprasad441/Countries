package com.ramprasad.countries.domain.usecase

import com.ramprasad.countries.domain.model.ResponseState
import com.ramprasad.countries.domain.repository.AllCountriesRepository
import kotlinx.coroutines.flow.Flow

/**
 * Created by Ramprasad on 7/27/25.
 */
class CountriesUseCase(val remoteRepo: AllCountriesRepository) {
    fun getAllCountries(): Flow<ResponseState> = remoteRepo.getAllCountries()
}