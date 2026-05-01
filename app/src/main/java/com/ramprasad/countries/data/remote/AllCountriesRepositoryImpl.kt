package com.ramprasad.countries.data.remote

import com.ramprasad.countries.commons.FailureResponse
import com.ramprasad.countries.commons.NullResponseMessage
import com.ramprasad.countries.data.remote.network.RetrofitClient
import com.ramprasad.countries.domain.model.Countries
import com.ramprasad.countries.domain.model.ResponseState
import com.ramprasad.countries.domain.repository.AllCountriesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Created by Ramprasad on 7/6/25.
 */
class AllCountriesRepositoryImpl(
    private val retrofitClient: RetrofitClient,
) : AllCountriesRepository {
    override fun getAllCountries(): Flow<ResponseState> =
        flow {
            emit(ResponseState.LOADING())

            try {
                val response = retrofitClient.getAllCountries()
                if (response.isSuccessful) {
                    response.body()?.let { countriesList ->
                        val mapOfCharAndData =
                            countriesList.groupBy {
                                if (it.name.isNotEmpty()) it.name[0].uppercaseChar() else null
                            }
                        val headerList =
                            mapOfCharAndData.keys
                                .filterNotNull()
                                .map { Countries(header = it.toString()) }

                        val resultList = headerList + countriesList

                        val sortedList =
                            resultList.sortedBy {
                                it.header ?: it.name
                            }

                        emit(ResponseState.SUCCESS(sortedList))
                    } ?: throw NullResponseMessage()
                } else {
                    throw FailureResponse()
                }
            } catch (e: Exception) {
                emit(ResponseState.ERROR(e))
            }
        }
}
