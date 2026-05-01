package com.ramprasad.countries.ui.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ramprasad.countries.R
import com.ramprasad.countries.databinding.ActivityMainBinding
import com.ramprasad.countries.ui.adapter.CountriesAdapter

class MainActivity : AppCompatActivity() {
    val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            val fragment =
                CountriesFragment().apply {
                    injectAdapter(CountriesAdapter())
                }

            supportFragmentManager
                .beginTransaction()
                .replace(R.id.CountryFragment, fragment)
                .commit()
        }
    }
}
