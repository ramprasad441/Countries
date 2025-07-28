package com.ramprasad.countries.providers

import com.ramprasad.countries.data.remote.AllCountriesRepositoryImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Created by Ramprasad on 7/8/25.
 */
class ModuleProviderTest {

    @Test
    fun `providesCountryRepository returns AllCountriesRepositoryImpl`() {
        // Act
        val repository = ModuleProvider.providesCountryRepository()

        // Assert
        assertTrue(repository is AllCountriesRepositoryImpl)
    }

//    @Test
//    fun `providesIODispatcher returns Dispatchers_IO`() {
//        // Act
//        val dispatcher: CoroutineDispatcher = ModuleProvider.providesIODispatcher()
//
//        // Assert
//        assertEquals(Dispatchers.IO, dispatcher)
//    }
}