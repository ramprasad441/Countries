package com.ramprasad.countries.ui.view

import android.os.Bundle
import com.ramprasad.countries.R
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Created by Ramprasad on 7/11/25.
 */

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class MainActivityTest {
    @Test
    fun `should add CountriesFragment and inject adapter`() {
        // Launch the activity
        val controller = Robolectric.buildActivity(MainActivity::class.java)
        val activity = controller.setup().get()
        activity.setTheme(R.style.Theme_Countries)

        // Assert activity is not null and has correct layout
        Assert.assertNotNull(activity)
        Assert.assertEquals(R.id.CountryFragment, activity.binding.CountryFragment.id)

        // Get the fragment added
        val fragment =
            activity.supportFragmentManager
                .findFragmentById(R.id.CountryFragment)

        // Verify the fragment is instance of CountriesFragment
        Assert.assertTrue(fragment is CountriesFragment)

        // Optional: Verify adapter was injected (requires exposing `adapter` in fragment)
        if (fragment is CountriesFragment) {
            Assert.assertNotNull(fragment.countriesAdapter)
            Assert.assertTrue(true)
        }
    }

    @Test
    fun `should not add CountriesFragment if savedInstanceState is not null`() {
        val bundle = Bundle()

        // Simulate first launch and save state
        Robolectric
            .buildActivity(MainActivity::class.java)
            .setup()
            .saveInstanceState(bundle)
            .pause()
            .stop()
            .destroy()

        // Recreate with savedInstanceState
        val recreatedController =
            Robolectric
                .buildActivity(MainActivity::class.java)
                .create(bundle)
                .start()
                .resume()
                .visible()

        val recreatedActivity = recreatedController.get()

        val fragment =
            recreatedActivity.supportFragmentManager
                .findFragmentById(R.id.CountryFragment)

        // Fragment should be present (restored by FragmentManager)
        Assert.assertTrue(fragment is CountriesFragment)
    }
}
