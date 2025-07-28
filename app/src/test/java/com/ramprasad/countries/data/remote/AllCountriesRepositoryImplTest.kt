package com.ramprasad.countries.data.remote

import com.ramprasad.countries.commons.FailureResponse
import com.ramprasad.countries.commons.NullResponseMessage
import com.ramprasad.countries.data.remote.network.RetrofitClient
import com.ramprasad.countries.domain.model.Countries
import com.ramprasad.countries.domain.model.ResponseState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import retrofit2.Response

/**
 * Created by Ramprasad on 7/6/25.
 */
@ExperimentalCoroutinesApi
class AllCountriesRepositoryImplTest {

    private val mockRetrofitClient = mockk<RetrofitClient>()

    private lateinit var repository: AllCountriesRepositoryImpl

    @Before
    fun setup() {
        repository = AllCountriesRepositoryImpl(mockRetrofitClient)
    }

    @Test
    fun `getAllCountries emits loading and success when retrofit returns successful response`() =
        runTest {
            // Arrange
            val countries = listOf(
                Countries(name = "Albania"),
                Countries(name = "Belgium")
            )
            val response = Response.success(countries)
            coEvery { mockRetrofitClient.getAllCountries() } returns response

            // Act
            val emissions = repository.getAllCountries().toList()

            // Assert
            assert(emissions[0] is ResponseState.LOADING)
            val success = emissions[1] as ResponseState.SUCCESS
            // Should contain headers plus original countries sorted
            assert(success.countries.isNotEmpty())
            // Header for 'A' and 'B' should exist
            assert(success.countries.any { it.header == "A" })
            assert(success.countries.any { it.header == "B" })
            // Countries count should be original + headers
            assert(success.countries.size == countries.size + 2)
        }

    @Test
    fun `getAllCountries emits loading and error when retrofit returns unsuccessful response`() =
        runTest {
            // Arrange
            val errorResponse: Response<List<Countries>> =
                Response.error(400, mockk(relaxed = true))
            coEvery { mockRetrofitClient.getAllCountries() } returns errorResponse

            // Act
            val emissions = repository.getAllCountries().toList()

            // Assert
            assert(emissions[0] is ResponseState.LOADING)
            assert(emissions[1] is ResponseState.ERROR)
            val errorState = emissions[1] as ResponseState.ERROR
            assert(errorState.error is FailureResponse)
        }

    @Test
    fun `getAllCountries emits loading and error when retrofit returns null body`() = runTest {
        // Arrange
        val nullBodyResponse: Response<List<Countries>> = Response.success(null)
        coEvery { mockRetrofitClient.getAllCountries() } returns nullBodyResponse

        // Act
        val emissions = repository.getAllCountries().toList()

        // Assert
        assert(emissions[0] is ResponseState.LOADING)
        assert(emissions[1] is ResponseState.ERROR)
        val errorState = emissions[1] as ResponseState.ERROR
        assert(errorState.error is NullResponseMessage)
    }

    @Test
    fun `getAllCountries emits loading and error when retrofit throws exception`() = runTest {
        // Arrange
        val exception = RuntimeException("Network error")
        coEvery { mockRetrofitClient.getAllCountries() } throws exception

        // Act
        val emissions = repository.getAllCountries().toList()

        // Assert
        assert(emissions[0] is ResponseState.LOADING)
        assert(emissions[1] is ResponseState.ERROR)
        val errorState = emissions[1] as ResponseState.ERROR
        assert(errorState.error.message == "Network error")
    }

    @Test
    fun `getAllCountries emits loading and success with empty list response`() = runTest {
        // Arrange
        val emptyCountries = emptyList<Countries>()
        val response = Response.success(emptyCountries)
        coEvery { mockRetrofitClient.getAllCountries() } returns response

        // Act
        val emissions = repository.getAllCountries().toList()

        // Assert
        assert(emissions[0] is ResponseState.LOADING)
        val success = emissions[1] as ResponseState.SUCCESS
        // Should contain no headers and no countries => empty list
        assert(success.countries.isEmpty())
    }

    @Test
    fun `getAllCountries emits loading and success with countries having empty names`() = runTest {
        // Arrange
        val countriesWithEmptyNames = listOf(
            Countries(name = ""),
            Countries(name = "Canada"),
            Countries(name = ""),
            Countries(name = "Cuba")
        )
        val response = Response.success(countriesWithEmptyNames)
        coEvery { mockRetrofitClient.getAllCountries() } returns response

        // Act
        val emissions = repository.getAllCountries().toList()

        // Assert
        assert(emissions[0] is ResponseState.LOADING)

        val secondEmission = emissions[1]
        assert(secondEmission is ResponseState.SUCCESS) {
            "Expected ResponseState.SUCCESS but received ${secondEmission::class.simpleName}" +
                    if (secondEmission is ResponseState.ERROR) ", Error: ${secondEmission.error}" else ""
        }
        val success = secondEmission as ResponseState.SUCCESS

        // Headers should only come from countries with non-empty names -> 'C'
        assert(success.countries.any { it.header == "C" })

        // Countries with empty names do not produce headers but should be present in the final list
        val emptyNameCountries = success.countries.filter { it.name.isEmpty() }
        assert(emptyNameCountries.isNotEmpty())

        // The total count is original countries plus headers (headers only for 'C')
        val expectedHeaders = 1 // for 'C'
        val expectedSize = countriesWithEmptyNames.size + expectedHeaders
        assert(success.countries.size == expectedSize)
    }



    @Test
    fun `getAllCountries emits loading and success with all same starting letter`() = runTest {
        // Arrange
        val countries = listOf(
            Countries(name = "Kenya"),
            Countries(name = "Kuwait"),
            Countries(name = "Kazakhstan")
        )
        val response = Response.success(countries)
        coEvery { mockRetrofitClient.getAllCountries() } returns response

        // Act
        val emissions = repository.getAllCountries().toList()

        // Assert
        assert(emissions[0] is ResponseState.LOADING)
        val success = emissions[1] as ResponseState.SUCCESS
        val headers = success.countries.filter { it.header != null }
        assert(headers.size == 1)
        assert(headers.first().header == "K")

        // Total size == original + one header
        assert(success.countries.size == countries.size + 1)

        // The list should be sorted by header/name properly:
        for (i in 0 until success.countries.size - 1) {
            val current = success.countries[i]
            val next = success.countries[i + 1]
            val currentKey = current.header ?: current.name
            val nextKey = next.header ?: next.name
            assert(currentKey <= nextKey)
        }
    }

    @Test
    fun `getAllCountries emits loading and success when countries have special characters in name`() =
        runTest {
            // Arrange
            val countries = listOf(
                Countries(name = "Åland"),
                Countries(name = "Éire"),
                Countries(name = "-Albania")
            )
            val response = Response.success(countries)
            coEvery { mockRetrofitClient.getAllCountries() } returns response

            // Act
            val emissions = repository.getAllCountries().toList()

            // Assert
            assert(emissions[0] is ResponseState.LOADING)
            val success = emissions[1] as ResponseState.SUCCESS

            // Headers should be created based on uppercase of first char if valid
            // For "-Albania" first char is '-', so header should be "-"
            // For "Åland" and "Éire", headers should be 'Å' and 'É' respectively
            val expectedHeaders = setOf("Å", "É", "-")

            val actualHeaders = success.countries.mapNotNull { it.header }.toSet()
            assert(actualHeaders.containsAll(expectedHeaders))

            // Total list size = countries + number of unique non-null headers
            assert(success.countries.size == countries.size + expectedHeaders.size)
        }

    @Test
    fun `getAllCountries emits loading and success when countries have names starting with lowercase letters`() =
        runTest {
            // Arrange
            val countries = listOf(
                Countries(name = "germany"),
                Countries(name = "greece"),
                Countries(name = "Guatemala")
            )
            val response = Response.success(countries)
            coEvery { mockRetrofitClient.getAllCountries() } returns response

            // Act
            val emissions = repository.getAllCountries().toList()

            // Assert headers should be uppercase first char 'G'
            assert(emissions[0] is ResponseState.LOADING)
            val success = emissions[1] as ResponseState.SUCCESS
            val headers = success.countries.filter { it.header != null }
            assert(headers.size == 1)
            assert(headers.first().header == "G")

            // Total list size = original + 1 header
            assert(success.countries.size == countries.size + 1)
        }

    @Test
    fun `getAllCountries emits loading and success when countries have duplicate starting letters`() =
        runTest {
            // Arrange
            val countries = listOf(
                Countries(name = "Denmark"),
                Countries(name = "Dominica"),
                Countries(name = "Djibouti")
            )
            val response = Response.success(countries)
            coEvery { mockRetrofitClient.getAllCountries() } returns response

            // Act
            val emissions = repository.getAllCountries().toList()

            // Assert only one header 'D' should be created
            assert(emissions[0] is ResponseState.LOADING)
            val success = emissions[1] as ResponseState.SUCCESS
            val headers = success.countries.filter { it.header != null }
            assert(headers.size == 1)
            assert(headers.first().header == "D")

            assert(success.countries.size == countries.size + 1)
        }

    @Test
    fun `getAllCountries emits loading and success when all countries have empty names`() =
        runTest {
            // Arrange
            val emptyNameCountries = listOf(
                Countries(name = ""),
                Countries(name = ""),
                Countries(name = "")
            )
            val response = Response.success(emptyNameCountries)
            coEvery { mockRetrofitClient.getAllCountries() } returns response

            // Act
            val emissions = repository.getAllCountries().toList()

            // Assert no headers, only original countries present
            assert(emissions[0] is ResponseState.LOADING)
            val success = emissions[1] as ResponseState.SUCCESS

            // No headers
            assert(success.countries.none { it.header != null })

            // List size unchanged
            assert(success.countries.size == emptyNameCountries.size)
        }

    @Test
    fun `getAllCountries emits loading and error when retrofit client throws custom exception`() =
        runTest {
            // Arrange
            val customException = IllegalArgumentException("Custom error")
            coEvery { mockRetrofitClient.getAllCountries() } throws customException

            // Act
            val emissions = repository.getAllCountries().toList()

            // Assert
            assert(emissions[0] is ResponseState.LOADING)
            val errorState = emissions[1] as ResponseState.ERROR
            assert(errorState.error is IllegalArgumentException)
            assert(errorState.error.message == "Custom error")
        }

    @Test
    fun `getAllCountries emits loading and success with mixed empty and valid names`() = runTest {
        // Arrange
        val countries = listOf(
            Countries(name = ""),
            Countries(name = "Norway"),
            Countries(name = ""),
            Countries(name = "Nepal")
        )
        val response = Response.success(countries)
        coEvery { mockRetrofitClient.getAllCountries() } returns response

        // Act
        val emissions = repository.getAllCountries().toList()

        // Assert
        assert(emissions[0] is ResponseState.LOADING)
        val success = emissions[1] as ResponseState.SUCCESS

        // Header for 'N' should exist
        assert(success.countries.any { it.header == "N" })

        // Countries with empty names present (exclude header entries)
        val emptyNameCountries = success.countries.filter { it.name.isEmpty() && it.header == null }
        assert(emptyNameCountries.size == 2)

        // Total is original countries + 1 header
        assert(success.countries.size == countries.size + 1)
    }




}