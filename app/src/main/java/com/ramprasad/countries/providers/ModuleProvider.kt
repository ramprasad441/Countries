package com.ramprasad.countries.providers

/**
 * Created by Ramprasad on 7/7/25.
 */
import com.ramprasad.countries.commons.CoroutineDispatcherProvider
import com.ramprasad.countries.data.remote.network.RetrofitClient
import com.ramprasad.countries.data.remote.AllCountriesRepositoryImpl
import com.ramprasad.countries.domain.repository.AllCountriesRepository
import com.ramprasad.countries.domain.usecase.CountriesUseCase
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


/**
 * Created by Ramprasad on 7/6/25.
 */
object ModuleProvider {
    private fun providesOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()


    private fun providesNetworkService(okHttpClient: OkHttpClient): RetrofitClient =
        Retrofit.Builder()
            .baseUrl(RetrofitClient.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(RetrofitClient::class.java)


    fun providesCountryRepository(): AllCountriesRepository =
        AllCountriesRepositoryImpl(providesNetworkService(providesOkHttpClient()))

    fun provideCountriesUseCase() = CountriesUseCase(providesCountryRepository())

    fun providesDispatcher(): CoroutineDispatcherProvider = CoroutineDispatcherProvider()
}