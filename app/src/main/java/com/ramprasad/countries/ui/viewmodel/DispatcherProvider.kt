package com.ramprasad.countries.ui.viewmodel

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Created by Ramprasad on 7/22/25.
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}