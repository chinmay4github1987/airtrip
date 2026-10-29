package com.airtrip.ui.picker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airtrip.domain.Place
import com.airtrip.domain.Slot
import com.airtrip.ui.map.MapViewModel

/**
 * Screen 5: Saved Locations Picker.
 * Shown when tapping an unset A or B slot on Screen 1.
 * Allows selecting from previously searched/cached locations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacePickerScreen(
    vm: MapViewModel,
    slot: Slot,
    onSelectPlace: (Place) -> Unit,
    onBack: () -> Unit
) {
    val state by vm.state.collectAsState()
    val places = state.cachedPlaces

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Location for $slot", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { padding ->
        if (places.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No cached locations yet.\nMove the map on Screen 1 and tap 'Set A' to search and cache coordinates.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                itemsIndexed(places) { index, place ->
                    val letter = ('A'.code + (index % 26)).toChar().toString()
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectPlace(place)
                            }
                            .padding(horizontal = 24.dp, vertical = 18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = letter,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(36.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = place.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "AQI: ${place.aqi} • (${String.format(java.util.Locale.US, "%.3f", place.latitude)}, ${String.format(java.util.Locale.US, "%.3f", place.longitude)})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                }
            }
        }
    }
}
