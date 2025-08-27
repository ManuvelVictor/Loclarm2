package com.victor.loclarm2.presentation.home.screens

import android.location.Address
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun Geocoder.getFromLocationNameAsync(
    locationName: String,
    maxResults: Int
): List<Address> = suspendCancellableCoroutine { cont ->
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getFromLocationName(locationName, maxResults, object : Geocoder.GeocodeListener {
            override fun onGeocode(addresses: MutableList<Address>) {
                cont.resume(addresses)
            }

            override fun onError(errorMessage: String?) {
                cont.resumeWithException(Exception(errorMessage ?: "Geocoding failed"))
            }
        })
    } else {
        try {
            @Suppress("DEPRECATION")
            val addresses = getFromLocationName(locationName, maxResults)
            cont.resume(addresses ?: emptyList())
        } catch (e: Exception) {
            cont.resumeWithException(e)
        }
    }
}