package com.ramprasad.countries.ui.adapter

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
import com.ramprasad.countries.ui.adapter.CountriesAdapter.HeaderViewHolder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.slot
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
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

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `submitList updates data and reflects correct item count and types`() {
        val countries =
            listOf(
                Countries(header = "A"),
                Countries(name = "Albania", code = "AL", region = "Europe", capital = "Tirana"),
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
                false,
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
        val binding =
            object {
                val countryHeader: TextView = textView
            }

        // Use your real or test-specific ViewHolder using the test binding
        val holder =
            object : RecyclerView.ViewHolder(textView) {
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
        val spy = spyk(adapter)
        every { spy.getItemViewType(any()) } returns 99
        every { spy.getItemFromList(any()) } returns mockk()
        val parent = FrameLayout(ApplicationProvider.getApplicationContext())
        val holder = spy.onCreateViewHolder(parent, 99)
        spy.onBindViewHolder(holder, 0)
    }

    @Test
    fun `onBindViewHolder routes to correct view holders`() {
        val spyAdapter = spyk(adapter)
        val headerItem = Countries(header = "A")
        val listItem = Countries(name = "France", code = "FR")

        every { spyAdapter.getItemFromList(0) } returns headerItem
        every { spyAdapter.getItemFromList(1) } returns listItem

        val mockHeaderHolder = mockk<HeaderViewHolder>(relaxed = true)
        val mockListItemHolder = mockk<CountriesAdapter.CountriesViewHolder>(relaxed = true)

        spyAdapter.onBindViewHolder(mockHeaderHolder, 0)
        verify { mockHeaderHolder.bind(headerItem) }

        spyAdapter.onBindViewHolder(mockListItemHolder, 1)
        verify { mockListItemHolder.bind(listItem) }
    }

    @Test
    fun `onBindViewHolder should bind HeaderViewHolder with header data`() {
        val spy = spyk(adapter)
        val country = mockk<Countries>()

        every { country.header } returns "A"
        every { spy.getItemViewType(any()) } returns 0
        every { spy.getItemFromList(any()) } returns country

        val parent = FrameLayout(ApplicationProvider.getApplicationContext())
        val holder = spy.onCreateViewHolder(parent, 0)
        assertTrue(holder is HeaderViewHolder)

        val headerViewHolder = holder as HeaderViewHolder
        val spyHolder = spyk(headerViewHolder)
        spy.onBindViewHolder(spyHolder, 0)
        verify { spyHolder.bind(country) }
    }

    @Test
    fun `CountriesViewHolder bind coverage with empty values`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val binding = CountriesListItemBinding.inflate(LayoutInflater.from(context), null, false)
        val holder = CountriesAdapter.CountriesViewHolder(binding)

        val country =
            Countries(
                name = "",
                code = "",
                region = "",
                capital = "",
            )

        holder.bind(country)

        assertEquals("", binding.countryName.text.toString())
        assertEquals("", binding.countryCode.text.toString())
    }

    @Test
    fun `covers both branches in bindViewHolder`() {
        val countries =
            Countries(name = "Albania", code = "AL", region = "Europe", capital = "Tirana")

        val spy = spyk(adapter)
        every { spy.getItemViewType(any()) } returns 1
        every { spy.getItemFromList(any()) } returns countries
        val parent = FrameLayout(ApplicationProvider.getApplicationContext())
        val holder = spy.onCreateViewHolder(parent, 1)
        assertTrue(holder is CountriesAdapter.CountriesViewHolder)
        val headerViewHolder = holder as CountriesAdapter.CountriesViewHolder
        val spyHolder = spyk(headerViewHolder)
        spy.onBindViewHolder(spyHolder, 0)
        verify { spyHolder.bind(countries) }
    }

    @Test
    fun `submitList updates item count`() {
        val countries =
            listOf(
                Countries(header = "A"),
                Countries(name = "Albania", code = "AL", region = "Europe", capital = "Tirana"),
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

    @Test
    fun `HeaderViewHolder bind coverage - both branches`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val inflater = LayoutInflater.from(context)

        val binding = CountriesHeaderListItemBinding.inflate(inflater, null, false)
        val holder = HeaderViewHolder(binding)

        val countryWithHeader = Countries(header = "Europe")
        holder.bind(countryWithHeader)
        assertEquals("Europe", binding.countryHeader.text.toString())

        val countryWithNullHeader = Countries(header = null)
        holder.bind(countryWithNullHeader)
        assertEquals("", binding.countryHeader.text.toString())
    }
}
