package com.example.task.model

import com.google.gson.annotations.SerializedName

data class DirectionsResponse(
    @SerializedName("routes") val routes: List<Route>,
    @SerializedName("status") val status: String
)

data class Route(
    @SerializedName("overview_polyline") val overviewPolyline: OverviewPolyline,
    @SerializedName("legs") val legs: List<Leg>
)

data class OverviewPolyline(
    @SerializedName("points") val points: String
)

data class Leg(
    @SerializedName("distance") val distance: Distance,
    @SerializedName("duration") val duration: Duration,
    @SerializedName("start_location") val startLocation: LatLng,
    @SerializedName("end_location") val endLocation: LatLng
)

data class Distance(
    @SerializedName("text") val text: String,
    @SerializedName("value") val value: Int
)

data class Duration(
    @SerializedName("text") val text: String,
    @SerializedName("value") val value: Int
)

data class LatLng(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double
)
