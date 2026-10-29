package com.airtrip.ui.nickname

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
import com.airtrip.domain.Slot
import com.airtrip.ui.map.BrandYellow
import com.airtrip.ui.map.MapViewModel

/**
 * Screen 2: Set nickname for selected location (max 20 characters, optional).
 * Matching Figma Screen 2 layout.
 */
@Composable
fun NicknameScreen(
    vm: MapViewModel,
    slot: Slot,
    onDone: () -> Unit
) {
    val s by vm.state.collectAsState()
    val place = if (slot == Slot.A) s.a else s.b
    var nick by remember { mutableStateOf(place?.nickname.orEmpty()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(24.dp)
    ) {
        // Top section: "A  location name" & "aqi  0"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = slot.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.width(36.dp)
            )
            Text(
                text = place?.name ?: "Unknown location",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.padding(start = 36.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "aqi",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(end = 16.dp)
            )
            Text(
                text = place?.aqi?.toString() ?: "0",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom section: Nickname input & Yellow V Button
        OutlinedTextField(
            value = nick,
            onValueChange = { if (it.length <= 20) nick = it },
            placeholder = { Text("nickname") },
            supportingText = { Text("${nick.length}/20", color = Color.Gray) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color(0xFFD1D5DB)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                vm.setNickname(slot, nick)
                onDone()
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

        Spacer(modifier = Modifier.height(16.dp))
    }
}
