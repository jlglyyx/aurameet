package com.chat.lib_common.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import com.chat.lib_common.constant.AppConstant
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okio.IOException
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.first
import kotlin.collections.isNullOrEmpty
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.let
import kotlin.run

class LocationUtil(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
        10000
    ).build()

    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    var onSuccess: (Double, Double, Address) -> Unit = { _, _, _ -> }

    var onError: (Exception) -> Unit = { it.printStackTrace() }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                stopLocationUpdates()
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        val address = getAddressFromLocation(location)
                        withContext(Dispatchers.Main) {
                            onSuccess(location.latitude, location.longitude, address)
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            onError(e)
                        }
                    }
                }
            } ?: run {
                onError(kotlin.Exception())
            }
        }
    }


    fun fetchLocationInfo() {

        if (!AppConstant.ClientInfo.OPEN_GOOGLE) return

        coroutineScope.launch {
            try {
                val lastLocation = getLastKnownLocation()
                if (lastLocation != null) {
                    val address = withContext(Dispatchers.IO) {
                        getAddressFromLocation(lastLocation)
                    }
                    onSuccess(lastLocation.latitude, lastLocation.longitude, address)
                } else {
                    val newLocation = requestNewLocation()
                    val address = withContext(Dispatchers.IO) {
                        getAddressFromLocation(newLocation)
                    }
                    onSuccess(newLocation.latitude, newLocation.longitude, address)
                }
            } catch (e: Exception) {
                onError(e)
            }
        }
    }


    private suspend fun getLastKnownLocation(): Location? =
        suspendCancellableCoroutine { continuation ->
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        continuation.resume(location)
                    }
                    .addOnFailureListener { e ->
                        continuation.resumeWithException(e)
                    }
            } catch (e: SecurityException) {
                continuation.resumeWithException(e)
            }
        }


    private suspend fun requestNewLocation(): Location =
        suspendCancellableCoroutine { continuation ->

            val isResumed = AtomicBoolean(false)

            try {

                val callback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        if (isResumed.compareAndSet(false, true)) {
                            locationResult.lastLocation?.let { location ->
                                fusedLocationClient.removeLocationUpdates(this)
                                continuation.resume(location)
                            } ?: run {
                                fusedLocationClient.removeLocationUpdates(this)
                                continuation.resumeWithException(kotlin.Exception())
                            }
                        }
                    }
                }

                continuation.invokeOnCancellation {
                    if (isResumed.compareAndSet(false, true)) {
                        fusedLocationClient.removeLocationUpdates(callback)
                    }
                }

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    callback,
                    Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                if (isResumed.compareAndSet(false, true)) {
                    continuation.resumeWithException(e)
                }
            }
        }


    private fun getAddressFromLocation(location: Location): Address {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                1
            )
            if (addresses.isNullOrEmpty()) {
                throw kotlin.Exception()
            } else {
                addresses.first()
            }
        } catch (e: IOException) {
            throw kotlin.Exception(" ${e.message}", e)
        }
    }


    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


    fun cancelAllTasks() {
        coroutineScope.coroutineContext.cancel()
    }
}