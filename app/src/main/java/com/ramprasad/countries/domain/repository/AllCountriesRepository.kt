package com.ramprasad.countries.domain.repository

import com.ramprasad.countries.domain.model.ResponseState
import kotlinx.coroutines.flow.Flow

interface AllCountriesRepository {
    fun getAllCountries(): Flow<ResponseState>
}