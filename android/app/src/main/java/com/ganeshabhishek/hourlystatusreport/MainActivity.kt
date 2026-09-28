package com.ganeshabhishek.hourlystatusreport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class StatusEntry(
    val time: String,
    val name: String,
    val status: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                HourlyStatusReportApp()
            }
        }
    }
}

@Composable
fun HourlyStatusReportApp() {
    var entries by remember {
        mutableStateOf(
            listOf(
                StatusEntry("10:00 AM", "Team", "Present"),
                StatusEntry("11:00 AM", "Team", "Working"),
                StatusEntry("12:00 PM", "Team", "Updated")
            )
        )
    }

    fun addEntry() {
        val now = LocalDateTime.now()
        val time = now.format(DateTimeFormatter.ofPattern("hh:mm a"))
        entries = entries + StatusEntry(time, "Team", "New update")
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Hourly Status Report") }) }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Today's hourly updates",
                    style = MaterialTheme.typography.headlineSmall
                )

                Button(
                    onClick = ::addEntry,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Current Hour")
                }

                entries.asReversed().forEach { entry ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(entry.time, style = MaterialTheme.typography.titleMedium)
                                Text(entry.name)
                            }
                            Text(entry.status)
                        }
                    }
                }
            }
        }
    }
}
