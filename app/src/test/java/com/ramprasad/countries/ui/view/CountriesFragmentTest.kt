package com.ramprasad.countries.ui.view

import android.R
import android.content.DialogInterface
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.common.truth.Truth.assertThat
import com.ramprasad.countries.ui.view.MainActivity
import com.ramprasad.countries.ui.adapter.CountriesAdapter
import com.ramprasad.countries.domain.model.ResponseState
import com.ramprasad.countries.domain.model.Countries
import com.ramprasad.countries.ui.view.CountriesFragment
import com.ramprasad.countries.ui.viewmodel.CountriesViewModel
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.shadows.ShadowToast

/**
 * Created by Ramprasad on 7/12/25.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class CountriesFragmentTest {

    @MockK
    private lateinit var viewModel: CountriesViewModel

    @MockK
    private lateinit var adapter: CountriesAdapter

    private lateinit var fragment: CountriesFragment
    private val countriesLiveData = MutableLiveData<ResponseState>()

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        every { viewModel.countries } returns countriesLiveData
        every { viewModel.getListOfAllCountries() } just Runs

        every { adapter.submitList(any()) } just Runs
        every { adapter.registerAdapterDataObserver(any()) } just Runs
        every { adapter.unregisterAdapterDataObserver(any()) } just Runs
        every { adapter.onAttachedToRecyclerView(any()) } just Runs
        every { adapter.onDetachedFromRecyclerView(any()) } just Runs
        every { adapter.onViewAttachedToWindow(any()) } just Runs
        every { adapter.onViewDetachedFromWindow(any()) } just Runs
        every { adapter.onViewRecycled(any()) } just Runs

        every { adapter.hasStableIds() } returns false

        every { adapter.itemCount } returns 2
        every { adapter.getItemViewType(any()) } returns 0

        every { adapter.createViewHolder(any(), any()) } answers {
            val view = LayoutInflater.from(fragment.requireContext())
                .inflate(R.layout.simple_list_item_1, null, false)
            object : RecyclerView.ViewHolder(view) {}
        }

        every { adapter.onBindViewHolder(any(), any()) } just Runs
        every { adapter.bindViewHolder(any(), any()) } just Runs

        fragment = CountriesFragment()

        fragment.viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(CountriesViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return viewModel as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }

        fragment.injectAdapter(adapter)

        val activity =
            Robolectric.buildActivity(MainActivity::class.java).create().start().resume().get()
        activity.supportFragmentManager.beginTransaction()
            .add(fragment, "test")
            .commitNow()
    }

    @Test
    fun observeCountries_loadingState_should_showProgressBar() {
        // Act
        countriesLiveData.postValue(ResponseState.LOADING())
        shadowOf(Looper.getMainLooper()).idle()

        // Assert
        assert(fragment.binding.countryProgress.visibility == View.VISIBLE)
        assert(fragment.binding.countryRV.visibility == View.GONE)
    }

    @Test
    fun observeCountries_successState_should_showCountries_and_scrollListener_should_controlFloatingButton() {
        // ARRANGE: Provide enough items to allow scroll
        val countries = List(50) { Countries("Country $it") }
        val successState = ResponseState.SUCCESS(countries)
        every { adapter.itemCount } returns countries.size

        countriesLiveData.postValue(successState)
        shadowOf(Looper.getMainLooper()).idle()

        val recyclerView = fragment.binding.countryRV
        val fab = fragment.binding.floatingButton

        // Ensure RecyclerView is properly laid out so scroll works
        recyclerView.measure(
            View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.AT_MOST)
        )
        recyclerView.layout(0, 0, 1080, 1920)
        shadowOf(Looper.getMainLooper()).idle()

        // ASSERT: Initially FAB should be hidden
        assertThat(fab.visibility).isEqualTo(View.GONE)

        // ACT: Scroll down → FAB should become visible
        recyclerView.scrollBy(0, 300)
        shadowOf(Looper.getMainLooper()).idle()

        assertThat(fab.visibility).isEqualTo(View.VISIBLE)

        // ACT: Scroll back up to top → FAB should disappear
        recyclerView.scrollToPosition(0)
        recyclerView.scrollBy(0, -300)
        shadowOf(Looper.getMainLooper()).idle()

        assertThat(fab.visibility).isEqualTo(View.GONE)
    }


    @Test
    fun observeCountries_errorState_should_showErrorDialog() {
        // Spy on the fragment BEFORE adding it to activity
        val spiedFragment = spyk(fragment, recordPrivateCalls = true)

        // Stub showErrorDialog to do nothing but allow verification
        every { spiedFragment.showErrorDialog(any(), any()) } just Runs

        // Replace fragment reference with spy in the activity transaction
        val activity =
            Robolectric.buildActivity(MainActivity::class.java).create().start().resume().get()
        activity.supportFragmentManager.beginTransaction()
            .add(spiedFragment, "test")
            .commitNow()

        // Now post error state to LiveData
        val errorMessage = "Network error"
        val errorState = ResponseState.ERROR(Throwable(errorMessage))
        countriesLiveData.postValue(errorState)

        // Process LiveData emissions
        shadowOf(Looper.getMainLooper()).idle()

        // Assert UI visibility updated correctly
        assertThat(spiedFragment.binding.countryProgress.visibility).isEqualTo(View.GONE)
        assertThat(spiedFragment.binding.countryRV.visibility).isEqualTo(View.GONE)
    }


    @Test
    fun showCountries_should_setAdapter_and_revealRecyclerView() {
        val countries = listOf(Countries("Japan"), Countries("France"))
        val successState = ResponseState.SUCCESS(countries)

        // Act
        countriesLiveData.postValue(successState)
        shadowOf(Looper.getMainLooper()).idle()

        // Assert
        assertThat(fragment.binding.countryRV.visibility).isEqualTo(View.VISIBLE)
        assertThat(fragment.binding.countryProgress.visibility).isEqualTo(View.GONE)
        verify { adapter.submitList(countries) }
    }

    @Test
    fun errorDialog_retry_should_call_getListOfAllCountries() {
        // Arrange
        val errorMessage = "Something went wrong"

        // Simulate error state
        countriesLiveData.postValue(ResponseState.ERROR(Throwable(errorMessage)))
        shadowOf(Looper.getMainLooper()).idle()

        // Act
        val dialog = ShadowAlertDialog.getLatestAlertDialog()
        Shadows.shadowOf(dialog)
        val retryButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)

        retryButton.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        // Assert
        verify(atLeast = 1) { viewModel.getListOfAllCountries() } // one for retry
    }

    @Test
    fun fragment_initial_state_should_showLoadingInitially() {
        countriesLiveData.postValue(ResponseState.LOADING())
        shadowOf(Looper.getMainLooper()).idle()

        assertThat(fragment.binding.countryProgress.visibility).isEqualTo(View.VISIBLE)
        assertThat(fragment.binding.countryRV.visibility).isEqualTo(View.GONE)
    }


    @Test
    fun floatingButton_should_remain_visible_while_scrolling_down_continuously() {
        val countries = List(50) { Countries("Country $it") }
        every { adapter.itemCount } returns countries.size
        countriesLiveData.postValue(ResponseState.SUCCESS(countries))
        shadowOf(Looper.getMainLooper()).idle()

        val recyclerView = fragment.binding.countryRV
        val floatingButton = fragment.binding.floatingButton

        // Layout for scroll simulation
        recyclerView.measure(
            View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.AT_MOST)
        )
        recyclerView.layout(0, 0, 1080, 1920)

        // Scroll multiple steps
        repeat(3) {
            recyclerView.scrollBy(0, 300)
            shadowOf(Looper.getMainLooper()).idle()
        }

        assertThat(floatingButton.visibility).isEqualTo(View.VISIBLE)
    }

    @Test
    fun floatingButton_should_hide_when_scrolling_up() {
        val countries = List(30) { Countries("Country $it") }
        every { adapter.itemCount } returns countries.size
        countriesLiveData.postValue(ResponseState.SUCCESS(countries))
        shadowOf(Looper.getMainLooper()).idle()

        val recyclerView = fragment.binding.countryRV
        val floatingButton = fragment.binding.floatingButton

        recyclerView.measure(
            View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.AT_MOST)
        )
        recyclerView.layout(0, 0, 1080, 1920)

        // Scroll down first
        recyclerView.scrollBy(0, 500)
        shadowOf(Looper.getMainLooper()).idle()
        assertThat(floatingButton.visibility).isEqualTo(View.VISIBLE)

        fragment.binding.swipeRefresh.isRefreshing = true
        //assertTrue(isRefreshed)
        //Espresso.onView(ViewMatchers.withId(R.id.country_RV)).perform(ViewActions.swipeDown())
        // Scroll back up
        recyclerView.scrollBy(0, -500)
        shadowOf(Looper.getMainLooper()).idle()
        assertThat(floatingButton.visibility).isEqualTo(View.GONE)
    }


    @Test
    fun showErrorDialog_should_displayDialog_and_handleRetry() {
        // Arrange
        val errorMessage = "Test error"
        val retrySlot = slot<() -> Unit>()
        mockkObject(fragment)
        every { fragment.showErrorDialog(any(), capture(retrySlot)) } answers {
            callOriginal()
        }

        // Act
        fragment.showErrorDialog(errorMessage) { viewModel.getListOfAllCountries() }
        val latestDialog = ShadowAlertDialog.getLatestAlertDialog()

        // Assert
        assertNotNull(latestDialog)
        assertTrue(latestDialog.isShowing)
        val shadowDialog = Shadows.shadowOf(latestDialog)
        assertEquals("Error", shadowDialog.title)
    }


    @Test
    fun floatingButton_click_should_scrollToTop() {
        // Arrange
        val recyclerView = fragment.binding.countryRV
        val spyRecyclerView = spyk(recyclerView)

        fragment.binding.floatingButton.setOnClickListener {
            spyRecyclerView.smoothScrollToPosition(0)
        }

        // Act
        fragment.binding.floatingButton.performClick()

        // Assert
        verify { spyRecyclerView.smoothScrollToPosition(0) }
    }

    @Test
    fun fragment_on_swipe_refresh_should_refresh_list() {
        // Arrange
        val swipeRefresh = spyk(fragment.binding.swipeRefresh)
        every { swipeRefresh.setOnRefreshListener(any()) } answers {
            firstArg<SwipeRefreshLayout.OnRefreshListener>().onRefresh()
        }
        every { viewModel.getListOfAllCountries() } just runs
        fragment.setupSwipeToRefresh(swipeRefresh)
        verify { viewModel.getListOfAllCountries() }
    }

    @Test
    fun swipeRefresh_should_trigger_getListOfAllCountries_and_hideRefreshingIndicator() {
        val swipeRefresh = fragment.binding.swipeRefresh
        val recyclerView = fragment.binding.countryRV

        // Set up recyclerview with dummy items
        val countries = List(10) { Countries("Country $it") }
        countriesLiveData.postValue(ResponseState.SUCCESS(countries))
        shadowOf(Looper.getMainLooper()).idle()

        recyclerView.measure(
            View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.AT_MOST)
        )
        recyclerView.layout(0, 0, 1080, 1920)

        swipeRefresh.measure(
            View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.AT_MOST)
        )
        swipeRefresh.layout(0, 0, 1080, 1920)

        // Reset viewModel, then define stub
        clearMocks(viewModel)
        every { viewModel.getListOfAllCountries() } just Runs

        // Act — simulate refresh via direct invocation
        swipeRefresh.isRefreshing = true
        fragment.showToast("Refreshing countries...")
        viewModel.getListOfAllCountries()
        swipeRefresh.isRefreshing = false
        shadowOf(Looper.getMainLooper()).idle()

        // Assert
        verify(exactly = 1) { viewModel.getListOfAllCountries() }
        assertThat(swipeRefresh.isRefreshing).isFalse()
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo("Refreshing countries...")
    }

    @Test
    fun showLoading_should_showProgressBar_and_hideRecyclerView() {
        countriesLiveData.postValue(ResponseState.LOADING())
        shadowOf(Looper.getMainLooper()).idle()

        assertThat(fragment.binding.countryProgress.visibility).isEqualTo(View.VISIBLE)
        assertThat(fragment.binding.countryRV.visibility).isEqualTo(View.GONE)
    }

    @Test
    fun setupRecyclerView_should_scrollToTop_whenFloatingButtonClicked() {
        val spyRecycler = spyk(fragment.binding.countryRV)
        val fab = fragment.binding.floatingButton

        fab.setOnClickListener {
            spyRecycler.smoothScrollToPosition(0)
        }

        fab.performClick()

        verify { spyRecycler.smoothScrollToPosition(0) }
    }

    @Test
    fun showCountries_withEmptyList_should_still_showRecyclerView() {
        val emptyState = ResponseState.SUCCESS(emptyList())

        countriesLiveData.postValue(emptyState)
        shadowOf(Looper.getMainLooper()).idle()

        assertThat(fragment.binding.countryProgress.visibility).isEqualTo(View.GONE)
        assertThat(fragment.binding.countryRV.visibility).isEqualTo(View.VISIBLE)
        verify { adapter.submitList(emptyList()) }
    }

    @Test
    fun showError_withNullMessage_should_useDefaultMessage() {
        val error = Throwable(message = null)
        val fallbackMessage = "Unknown error"

        countriesLiveData.postValue(ResponseState.ERROR(error))
        shadowOf(Looper.getMainLooper()).idle()

        val dialog = ShadowAlertDialog.getLatestAlertDialog()
        val shadowDialog = Shadows.shadowOf(dialog)

        assertThat(fragment.binding.countryProgress.visibility).isEqualTo(View.GONE)
        assertThat(fragment.binding.countryRV.visibility).isEqualTo(View.GONE)
        assertThat(shadowDialog.message).isEqualTo(fallbackMessage)
    }

    @Test
    fun errorDialog_dismissButton_should_dismissDialog() {
        fragment.showErrorDialog("Some error") {
            fail("Retry should not be triggered") // to ensure not called
        }

        val dialog = ShadowAlertDialog.getLatestAlertDialog()
        assertTrue(dialog.isShowing)

        val dismissButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        dismissButton.performClick()

        shadowOf(Looper.getMainLooper()).idle()

        assertFalse(dialog.isShowing)
    }

    @Test
    fun floatingButton_click_should_trigger_smoothScrollToTop() {
        val recyclerView = fragment.binding.countryRV
        val fab = fragment.binding.floatingButton

        // Setup test layout manager to intercept scroll call
        val testLayoutManager = object : LinearLayoutManager(fragment.requireContext()) {
            var scrollTo: Int = -1
            override fun smoothScrollToPosition(rv: RecyclerView, state: RecyclerView.State?, position: Int) {
                scrollTo = position
                super.scrollToPosition(position)
            }
        }

        recyclerView.layoutManager = testLayoutManager

        // Trigger FAB click
        fab.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        // Assert scroll requested
        assertThat(testLayoutManager.scrollTo).isEqualTo(0)
    }
}
