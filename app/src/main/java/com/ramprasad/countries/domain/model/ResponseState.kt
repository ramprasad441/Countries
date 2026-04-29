package com.ramprasad.countries.domain.model

/**
 * Created by Ramprasad on 7/6/25.
 */
sealed interface ResponseState {
    class ERROR(
        val error: Throwable,
    ) : ResponseState

    class LOADING(
        val isLoading: Boolean = true,
    ) : ResponseState

    class SUCCESS(
        val countries: List<Countries>,
    ) : ResponseState
}
