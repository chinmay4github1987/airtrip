package com.airtrip.ui.map

import androidx.lifecycle.*
import com.airtrip.data.repo.*
import com.airtrip.domain.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val currentAqi: Int? = null,
    val a: Place? = null,
    val b: Place? = null,
    val loading: Boolean = false,
    val booked: Book? = null,
    val error: String? = null,
    val cachedPlaces: List<Place> = emptyList()
) {
    val buttonText get() = when {
        a == null -> "Set A"
        b == null -> "Set B"
        else -> "Book"
    }
}

@HiltViewModel
class MapViewModel @Inject constructor(
    private val places: PlaceRepository,
    private val books: BookRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MapUiState())
    val state = _state.asStateFlow()

    init {
        loadCachedPlaces()
    }

    fun loadCachedPlaces() = viewModelScope.launch {
        try {
            val list = places.getCachedPlaces()
            _state.update { it.copy(cachedPlaces = list) }
        } catch (_: Exception) {}
    }

    fun onCameraIdle(lat: Double, lng: Double) = launchSafe {
        val aqi = places.fetchAqi(lat, lng)
        _state.update { it.copy(currentAqi = aqi) }
    }

    fun onVTap(lat: Double, lng: Double) = launchSafe {
        val s = _state.value
        when {
            s.a == null -> {
                val p = places.resolve(lat, lng)
                _state.update { it.copy(a = p) }
                loadCachedPlaces()
            }
            s.b == null -> {
                val p = places.resolve(lat, lng)
                _state.update { it.copy(b = p) }
                loadCachedPlaces()
            }
            else -> {
                val book = books.book(s.a, s.b)
                _state.update { it.copy(booked = book) }
            }
        }
    }

    fun setPlace(slot: Slot, place: Place) = launchSafe {
        // Refresh AQI for the selected place
        val freshAqi = places.fetchAqi(place.latitude, place.longitude)
        val refreshed = place.copy(aqi = freshAqi)
        _state.update {
            if (slot == Slot.A) it.copy(a = refreshed) else it.copy(b = refreshed)
        }
    }

    fun setFromHistory(book: Book) = launchSafe {
        val aAqi = places.fetchAqi(book.a.latitude, book.a.longitude)
        val bAqi = places.fetchAqi(book.b.latitude, book.b.longitude)
        _state.update {
            it.copy(
                a = book.a.copy(aqi = aAqi),
                b = book.b.copy(aqi = bAqi),
                booked = null
            )
        }
    }

    fun setNickname(slot: Slot, nick: String) = viewModelScope.launch {
        val cleaned = nick.take(20).ifBlank { null }
        val targetPlace = if (slot == Slot.A) _state.value.a else _state.value.b
        targetPlace?.let { places.saveNickname(it, cleaned) }

        _state.update {
            if (slot == Slot.A) {
                it.copy(a = it.a?.copy(nickname = cleaned))
            } else {
                it.copy(b = it.b?.copy(nickname = cleaned))
            }
        }
        loadCachedPlaces()
    }

    fun reset() {
        _state.update { MapUiState(cachedPlaces = it.cachedPlaces) }
    }

    private fun launchSafe(block: suspend () -> Unit) = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        try {
            block()
        } catch (e: Exception) {
            _state.update { it.copy(error = e.message ?: "Operation failed") }
        } finally {
            _state.update { it.copy(loading = false) }
        }
    }
}
