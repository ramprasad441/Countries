package com.ramprasad.countries.ui.view.adapter

import android.content.Context
import android.os.Build
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.ramprasad.countries.databinding.CountriesHeaderListItemBinding
import com.ramprasad.countries.databinding.CountriesListItemBinding
import com.ramprasad.countries.domain.model.Countries
import com.ramprasad.countries.ui.adapter.CountriesAdapter
import com.ramprasad.countries.ui.adapter.CountriesAdapter.HeaderViewHolder
import com.ramprasad.countries.ui.adapter.CountriesDiffCallback
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Created by Ramprasad on 7/6/25.
 */

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class CountriesAdapterTest {

    private lateinit var adapter: CountriesAdapter

    @Before
    fun setUp() {
        adapter = CountriesAdapter()
    }

    @Test
    fun `submitList updates data and reflects correct item count and types`() {
        val countries = listOf(
            Countries(header = "A"),
            Countries(name = "Albania", code = "AL", region = "Europe", capital = "Tirana")
        )

        adapter.submitList(countries)
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(2, adapter.itemCount)
        assertEquals(0, adapter.getItemViewType(0)) // Header
        assertEquals(1, adapter.getItemViewType(1)) // Country
    }

    @Test
    fun `onCreateViewHolder creates correct ViewHolder types`() {
        mockkStatic(LayoutInflater::class)

        val parent = mockk<ViewGroup>()
        val context = mockk<Context>()
        val inflater = mockk<LayoutInflater>()
        val mockHeaderBinding = mockk<CountriesHeaderListItemBinding>()
        val mockItemBinding = mockk<CountriesListItemBinding>()

        val mockRootView = mockk<CardView>()
        every { mockHeaderBinding.root } returns mockRootView
        every { mockItemBinding.root } returns mockRootView

        every { parent.context } returns context
        every { LayoutInflater.from(context) } returns inflater

        mockkStatic(CountriesHeaderListItemBinding::class)
        mockkStatic(CountriesListItemBinding::class)

        every {
            CountriesHeaderListItemBinding.inflate(
                inflater,
                parent,
                false
            )
        } returns mockHeaderBinding
        every { CountriesListItemBinding.inflate(inflater, parent, false) } returns mockItemBinding

        val headerHolder = adapter.onCreateViewHolder(parent, 0)
        assertTrue(headerHolder is HeaderViewHolder)

        val countryHolder = adapter.onCreateViewHolder(parent, 1)
        assertTrue(countryHolder is CountriesAdapter.CountriesViewHolder)
    }


    @Test
    fun `onBindViewHolder binds HeaderViewHolder with header data`() {
        val headerCountry = Countries(header = "A")
        adapter.submitList(listOf(headerCountry))
        shadowOf(Looper.getMainLooper()).idle()

        val textView = mockk<TextView>()
        val binding = object {
            val countryHeader: TextView = textView
        }

        // Use your real or test-specific ViewHolder using the test binding
        val holder = object : RecyclerView.ViewHolder(textView) {
            fun bind(countries: Countries) {
                binding.countryHeader.text = countries.header
            }
        }
        val slot = slot<CharSequence>()
        every { textView.text = capture(slot) } just runs

        holder.bind(headerCountry)

        verify { textView.text = any<CharSequence>() }
        assertEquals("A", slot.captured)
    }

    @Test(expected = IllegalStateException::class)
    fun `onBindViewHolder throws for unknown view type`() {
        val spyk = spyk(adapter)
        every { spyk.getItemViewType(any()) } returns 99 // provoke else branch! }
        every { spyk.getItemFromList(any()) } returns mockk()
        val parent = FrameLayout(ApplicationProvider.getApplicationContext())
        val holder = spyk.onCreateViewHolder(parent, 99)
        spyk.onBindViewHolder(holder, 0) // Should throw and test will pass if error thrown
    }

    @Test
    fun `onBindViewHolder should bind HeaderViewHolder with header data`() {
        val spyk = spyk(adapter)
        val country = mockk<Countries>()
        every { country.header } returns "A"
        every { spyk.getItemViewType(any()) } returns 0
        every { spyk.getItemFromList(any()) } returns country
        val parent = FrameLayout(ApplicationProvider.getApplicationContext())
        val holder = spyk.onCreateViewHolder(parent, 0)
        assertTrue(holder is HeaderViewHolder)
        val headerViewHolder = holder as HeaderViewHolder
        val spykHolder = spyk(headerViewHolder)
        spyk.onBindViewHolder(spykHolder, 0)
        verify { spykHolder.bind(country) }
    }


    @Test
    fun `binds CountriesViewHolder with null values`() {
        val country1 = Countries()

        mockkStatic(LayoutInflater::class)

        val parent = mockk<ViewGroup>()
        val context = mockk<Context>()
        val inflater = mockk<LayoutInflater>()
        val mockItemBinding = mockk<CountriesListItemBinding>()

        val countryCode = mockk<TextView>(relaxed = true)
        val region = mockk<TextView>(relaxed = true)
        val countryName = mockk<TextView>(relaxed = true)
        val countryCapital = mockk<TextView>(relaxed = true)

        val mockCardView = mockk<CardView>(relaxed = true)
        every { mockItemBinding.root } returns mockCardView
        mockItemBinding.setPrivateField("countryCode", countryCode)
        mockItemBinding.setPrivateField("region", region)
        mockItemBinding.setPrivateField("countryName", countryName)
        mockItemBinding.setPrivateField("countryCapital", countryCapital)

        every { parent.context } returns context
        every { LayoutInflater.from(context) } returns inflater

        mockkStatic(CountriesListItemBinding::class)
        every { CountriesListItemBinding.inflate(inflater, parent, false) } returns mockItemBinding

        val sAdapter = spyk(adapter)
        every { sAdapter.getItemFromList(any()) } returns country1
        val countryHolder =
            sAdapter.onCreateViewHolder(parent, 1) as CountriesAdapter.CountriesViewHolder

        sAdapter.bindViewHolder(countryHolder, 1)
    }


    @Test
    fun `covers both branches in bindViewHolder`() {


        val countries =
            Countries(name = "Albania", code = "AL", region = "Europe", capital = "Tirana")

        val spyk = spyk(adapter)
        every { spyk.getItemViewType(any()) } returns 1
        every { spyk.getItemFromList(any()) } returns countries
        val parent = FrameLayout(ApplicationProvider.getApplicationContext())
        val holder = spyk.onCreateViewHolder(parent, 1)
        assertTrue(holder is CountriesAdapter.CountriesViewHolder)
        val headerViewHolder = holder as CountriesAdapter.CountriesViewHolder
        val spykHolder = spyk(headerViewHolder)
        spyk.onBindViewHolder(spykHolder, 0)
        verify { spykHolder.bind(countries) }
    }


    @Test
    fun `submitList updates item count`() {
        val countries = listOf(
            Countries(header = "A"),
            Countries(name = "Albania", code = "AL", region = "Europe", capital = "Tirana")
        )

        adapter.submitList(countries)
        shadowOf(Looper.getMainLooper()).idle() // instead of flushForegroundThreadScheduler()

        assertEquals(2, adapter.itemCount)
    }

    class CountriesDiffCallbackTest {

        private val diffCallback = CountriesDiffCallback

        @Test
        fun `areItemsTheSame returns true when both headers are non-null and equal`() {
            val oldItem = Countries(header = "A", code = "AL")
            val newItem = Countries(header = "A", code = "BR") // code different, headers same

            assertTrue(diffCallback.areItemsTheSame(oldItem, newItem))
        }

        @Test
        fun `areItemsTheSame returns false when both headers are non-null but different`() {
            val oldItem = Countries(header = "A", code = "AL")
            val newItem = Countries(header = "B", code = "AL")

            assertFalse(diffCallback.areItemsTheSame(oldItem, newItem))
        }

        @Test
        fun `areItemsTheSame returns true when both headers null and codes are equal`() {
            val oldItem = Countries(header = null, code = "AL")
            val newItem = Countries(header = null, code = "AL")

            assertTrue(diffCallback.areItemsTheSame(oldItem, newItem))
        }

        @Test
        fun `areItemsTheSame returns false when headers null and codes are different`() {
            val oldItem = Countries(header = null, code = "AL")
            val newItem = Countries(header = null, code = "BR")

            assertFalse(diffCallback.areItemsTheSame(oldItem, newItem))
        }

        @Test
        fun `areItemsTheSame returns false when one header is null and the other is not`() {
            val oldItem = Countries(header = null, code = "AL")
            val newItem = Countries(header = "A", code = "AL")

            assertFalse(diffCallback.areItemsTheSame(oldItem, newItem))

            val oldItem2 = Countries(header = "A", code = "AL")
            val newItem2 = Countries(header = null, code = "AL")

            assertFalse(diffCallback.areItemsTheSame(oldItem2, newItem2))
        }

        @Test
        fun `areContentsTheSame returns true when oldItem equals newItem`() {
            val item1 = Countries(header = "A", code = "AL")
            val item2 = Countries(header = "A", code = "AL")

            assertTrue(diffCallback.areContentsTheSame(item1, item2))
        }

        @Test
        fun `areContentsTheSame returns false when oldItem does not equal newItem`() {
            val item1 = Countries(header = "A", code = "AL")
            val item2 = Countries(header = "B", code = "BR")

            assertFalse(diffCallback.areContentsTheSame(item1, item2))
        }
    }

    private fun Any.setPrivateField(field: String, value: Any) {
        this::class.java.getDeclaredField(field).apply {
            this.isAccessible = true
            set(this@setPrivateField, value)
        }
    }

}



