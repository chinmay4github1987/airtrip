package com.airtrip

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.*
import com.airtrip.domain.Slot
import com.airtrip.ui.history.HistoryScreen
import com.airtrip.ui.map.*
import com.airtrip.ui.nickname.NicknameScreen
import com.airtrip.ui.picker.PlacePickerScreen
import com.airtrip.ui.result.ResultScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val nav = rememberNavController()
                val vm: MapViewModel = hiltViewModel(this) // Shared Activity-scoped ViewModel

                NavHost(nav, startDestination = "map") {
                    // Screen 1: Map + AQI + A/B Slots + V Action Button
                    composable("map") {
                        MapScreen(
                            vm = vm,
                            onEditNickname = { slot -> nav.navigate("nickname/${slot.name}") },
                            onPickFromCache = { slot -> nav.navigate("picker/${slot.name}") },
                            onBooked = { nav.navigate("result") }
                        )
                    }

                    // Screen 2: Set Location Nickname
                    composable("nickname/{slot}") { backStackEntry ->
                        val slotName = backStackEntry.arguments?.getString("slot") ?: "A"
                        val slot = Slot.valueOf(slotName)
                        NicknameScreen(
                            vm = vm,
                            slot = slot,
                            onDone = { nav.popBackStack() }
                        )
                    }

                    // Screen 5: Pick from Cached Locations
                    composable("picker/{slot}") { backStackEntry ->
                        val slotName = backStackEntry.arguments?.getString("slot") ?: "A"
                        val slot = Slot.valueOf(slotName)
                        PlacePickerScreen(
                            vm = vm,
                            slot = slot,
                            onSelectPlace = { place ->
                                vm.setPlace(slot, place)
                                nav.popBackStack()
                            },
                            onBack = { nav.popBackStack() }
                        )
                    }

                    // Screen 3: Booking Result & Confirmation
                    composable("result") {
                        ResultScreen(
                            vm = vm,
                            onBack = { nav.popBackStack() },
                            onHistory = { nav.navigate("history") { popUpTo("map") } }
                        )
                    }

                    // Screen 4: Monthly Booking History & Rebooking
                    composable("history") {
                        HistoryScreen(
                            onSelectRecord = { book ->
                                vm.setFromHistory(book)
                                nav.popBackStack()
                            },
                            onBack = { nav.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
