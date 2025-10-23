package com.example.task.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.AutocompletePrediction

class PlacesAdapter(
    private val onPlaceSelected: (AutocompletePrediction) -> Unit
) : RecyclerView.Adapter<PlacesAdapter.PlaceViewHolder>() {

    private var places = listOf<AutocompletePrediction>()

    fun updatePlaces(newPlaces: List<AutocompletePrediction>) {
        places = newPlaces
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.bind(places[position])
    }

    override fun getItemCount() = places.size

    inner class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val primaryText = itemView.findViewById<TextView>(android.R.id.text1)
        private val secondaryText = itemView.findViewById<TextView>(android.R.id.text2)

        fun bind(place: AutocompletePrediction) {
            primaryText.text = place.getPrimaryText(null).toString()
            secondaryText.text = place.getSecondaryText(null).toString()

            itemView.setOnClickListener {
                onPlaceSelected(place)
            }
        }
    }
}
