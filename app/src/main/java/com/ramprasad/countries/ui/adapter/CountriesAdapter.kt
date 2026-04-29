package com.ramprasad.countries.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ramprasad.countries.databinding.CountriesHeaderListItemBinding
import com.ramprasad.countries.databinding.CountriesListItemBinding
import com.ramprasad.countries.domain.model.Countries

/**
 * Created by Ramprasad on 7/6/25.
 */
class CountriesAdapter : ListAdapter<Countries, RecyclerView.ViewHolder>(CountriesDiffCallback) {
    override fun getItemViewType(position: Int): Int = if (getItemFromList(position).header != null) 0 else 1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 ->
                HeaderViewHolder(
                    CountriesHeaderListItemBinding.inflate(inflater, parent, false),
                )
            else ->
                CountriesViewHolder(
                    CountriesListItemBinding.inflate(inflater, parent, false),
                )
        }
    }

    fun getItemFromList(position: Int): Countries = getItem(position)

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        val item = getItemFromList(position)
        when (getItemViewType(position)) {
            0 -> (holder as HeaderViewHolder).bind(item)
            1 -> (holder as CountriesViewHolder).bind(item)
            else -> error("Unknown view type: ${getItemViewType(position)}")
        }
    }

    // --- ViewHolders ---
    class HeaderViewHolder(
        private val binding: CountriesHeaderListItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(countries: Countries) {
            binding.countryHeader.text = countries.header ?: ""
        }
    }

    class CountriesViewHolder(
        val binding: CountriesListItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(countries: Countries) {
            binding.countryCode.text = countries.code
            binding.countryName.text = countries.name
            binding.region.text = countries.region
            binding.countryCapital.text = countries.capital
        }
    }
}

object CountriesDiffCallback : DiffUtil.ItemCallback<Countries>() {
    override fun areItemsTheSame(
        oldItem: Countries,
        newItem: Countries,
    ): Boolean =
        if (oldItem.header != null && newItem.header != null) {
            oldItem.header == newItem.header
        } else if (oldItem.header == null && newItem.header == null) {
            oldItem.code == newItem.code
        } else {
            false
        }

    override fun areContentsTheSame(
        oldItem: Countries,
        newItem: Countries,
    ): Boolean = oldItem == newItem
}
