package com.airtrip.ui.result

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airtrip.domain.Place
import com.airtrip.ui.map.BrandYellow
import com.airtrip.ui.map.MapViewModel

/**
 * Screen 3: Booking confirmation matching Figma Screen 3.
 * Shows A and B locations, AQI, nicknames, and final price.
 * Bottom "V" button navigates to Screen 4 (History).
 * Back press resets and returns to Screen 1.
 */
@Composable
fun ResultScreen(
    vm: MapViewModel,
    onBack: () -> Unit,
    onHistory: () -> Unit
) {
    val book = remember { vm.state.value.booked }

    BackHandler {
        vm.reset()
        onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(24.dp)
    ) {
        book?.let { b ->
            // Slot A Section
            LocationSection(slotLabel = "A", place = b.a)

            Spacer(modifier = Modifier.height(28.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(28.dp))

            // Slot B Section
            LocationSection(slotLabel = "B", place = b.b)

            Spacer(modifier = Modifier.weight(1f))

            // Price Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "price",
                    fontSize = 18.sp,
                    color = Color.Gray
                )
                Text(
                    text = if (b.price % 1.0 == 0.0) b.price.toLong().toString() else b.price.toString(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            // Bottom Yellow Action Button V
            Button(
                onClick = {
                    vm.reset()
                    onHistory()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandYellow,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text("V", fontSize = 22.sp, fontWeight = FontWeight.Black)
            }
        } ?: run {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No booking data available.")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun LocationSection(slotLabel: String, place: Place) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = slotLabel,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.width(36.dp)
            )
            Text(
                text = place.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.padding(start = 36.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "aqi",
                fontSize = 15.sp,
                color = Color.Gray,
                modifier = Modifier.width(90.dp)
            )
            Text(
                text = place.aqi.toString(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        if (!place.nickname.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.padding(start = 36.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "nickname",
                    fontSize = 15.sp,
                    color = Color.Gray,
                    modifier = Modifier.width(90.dp)
                )
                Text(
                    text = place.nickname,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}
