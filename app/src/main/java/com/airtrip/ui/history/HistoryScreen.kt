package com.airtrip.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.*
import com.airtrip.data.repo.BookRepository
import com.airtrip.domain.Book
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class HistoryState(
    val items: List<Book> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
) {
    val count get() = items.size
    val total get() = items.sumOf { it.price }
}

@HiltViewModel
class HistoryViewModel @Inject constructor(private val repo: BookRepository) : ViewModel() {
    private val _s = MutableStateFlow(HistoryState(loading = true))
    val state = _s.asStateFlow()

    init {
        loadCurrentMonth()
    }

    fun loadCurrentMonth() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH) + 1
        viewModelScope.launch {
            _s.update { it.copy(loading = true, error = null) }
            try {
                val books = repo.history(year, month)
                _s.update { it.copy(items = books, loading = false) }
            } catch (e: Exception) {
                _s.update { it.copy(error = e.message ?: "Failed to load history", loading = false) }
            }
        }
    }
}

/**
 * Screen 4: Monthly History matching Figma Screen 4.
 * Shows Total Count and Total Price at the top, followed by list of trips.
 * Tapping any item reselects A and B and returns to Screen 1 (optional enhancement).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onSelectRecord: (Book) -> Unit = {},
    onBack: () -> Unit = {},
    vm: HistoryViewModel = hiltViewModel()
) {
    val s by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trip History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header stats: Total Count & Total Price
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp, horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total Count",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = s.count.toString(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total Price",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val totalDisplay = if (s.total % 1.0 == 0.0) s.total.toLong().toString() else s.total.toString()
                    Text(
                        text = totalDisplay,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

            if (s.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.Black)
                }
            } else if (s.items.isEmpty()) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No bookings found for this month.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(s.items) { book ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectRecord(book) }
                                .padding(horizontal = 24.dp, vertical = 18.dp)
                        ) {
                            // Slot A Row
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "A",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                    modifier = Modifier.width(36.dp)
                                )
                                Text(
                                    text = book.a.displayName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Slot B Row
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "B",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                    modifier = Modifier.width(36.dp)
                                )
                                Text(
                                    text = book.b.displayName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Price: ${book.price.toLong()} • Tap to re-book",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(start = 36.dp)
                            )
                        }

                        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                    }
                }
            }
        }
    }
}
