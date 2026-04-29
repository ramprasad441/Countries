package com.ramprasad.countries.ui.view

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ramprasad.countries.databinding.CountriesFragmentBinding
import com.ramprasad.countries.domain.model.ResponseState
import com.ramprasad.countries.providers.ModuleProvider
import com.ramprasad.countries.ui.adapter.CountriesAdapter
import com.ramprasad.countries.ui.viewmodel.CountriesViewModel
import com.ramprasad.countries.ui.viewmodel.CountriesViewModelFactory

/**
 * Created by Ramprasad on 7/5/25.
 */
class CountriesFragment : Fragment() {
    private var _binding: CountriesFragmentBinding? = null
    val binding get() = _binding!!

    // ViewModel safely initialized using a factory
    var viewModelFactory: ViewModelProvider.Factory? = null

    val countriesViewModel: CountriesViewModel by viewModels {
        viewModelFactory ?: CountriesViewModelFactory(
            ModuleProvider.provideCountriesUseCase(),
            ModuleProvider.providesDispatcher(),
        )
    }

    // Adapter can be injected in tests
    var countriesAdapter: CountriesAdapter = CountriesAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CountriesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeCountries()
        setupSwipeToRefresh(binding.swipeRefresh)
    }

    private fun setupRecyclerView() {
        binding.countryRV.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = countriesAdapter
            binding.floatingButton.setOnClickListener {
                smoothScrollToPosition(0)
            }
        }
    }

    fun setupSwipeToRefresh(swipeRefreshLayout: SwipeRefreshLayout) {
        swipeRefreshLayout.setOnRefreshListener {
            onSwipeRefresh()
        }
    }

    private fun onSwipeRefresh() {
        countriesViewModel.getListOfAllCountries()
        binding.swipeRefresh.isRefreshing = false
    }

    private fun observeCountries() {
        countriesViewModel.countries.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResponseState.LOADING -> showLoading()
                is ResponseState.SUCCESS -> showCountries(state)
                is ResponseState.ERROR -> showError(state.error.localizedMessage ?: "Unknown error")
            }
        }
    }

    private fun showLoading() {
        binding.countryProgress.visibility = View.VISIBLE
        binding.countryRV.visibility = View.GONE
    }

    private fun showCountries(state: ResponseState.SUCCESS) {
        binding.countryProgress.visibility = View.GONE
        binding.countryRV.visibility = View.VISIBLE
        countriesAdapter.submitList(state.countries)

        setupScrollToTopButton()
    }

    private fun showError(message: String) {
        binding.countryProgress.visibility = View.GONE
        binding.countryRV.visibility = View.GONE

        showErrorDialog(message) {
            countriesViewModel.getListOfAllCountries()
        }
    }

    private fun setupScrollToTopButton() {
        binding.countryRV.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int,
                ) {
                    val isAtTop = !recyclerView.canScrollVertically(-1)
                    if (isAtTop) {
                        binding.floatingButton.visibility = View.GONE
                    } else {
                        binding.floatingButton.visibility = View.VISIBLE
                    }
                }
            },
        )
    }

    // === Hooks for testing / overriding ===

    fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    fun showErrorDialog(
        message: String,
        retry: () -> Unit,
    ) {
        AlertDialog
            .Builder(requireContext())
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("Retry") { dialog, _ ->
                dialog.dismiss()
                retry()
            }.setNegativeButton("Dismiss") { dialog, _ ->
                dialog.dismiss() // This will now turn green
            }.create()
            .show()
    }

    // Allows test injection of adapter
    fun injectAdapter(adapter: CountriesAdapter) {
        countriesAdapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
