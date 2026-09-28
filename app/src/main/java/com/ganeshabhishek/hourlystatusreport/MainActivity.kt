package com.ganeshabhishek.hourlystatusreport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ganeshabhishek.hourlystatusreport.data.HourlyReportRepository
import com.ganeshabhishek.hourlystatusreport.data.ReportDatabase
import com.ganeshabhishek.hourlystatusreport.ui.HourlyReportViewModel
import com.ganeshabhishek.hourlystatusreport.ui.HourlyStatusReportScreen
import com.ganeshabhishek.hourlystatusreport.ui.theme.HourlyStatusReportTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = ReportDatabase.getInstance(applicationContext)
        val repository = HourlyReportRepository(database.hourlyReportDao())

        setContent {
            HourlyStatusReportTheme {
                val reportViewModel: HourlyReportViewModel = viewModel(
                    factory = HourlyReportViewModel.provideFactory(repository)
                )
                HourlyStatusReportScreen(viewModel = reportViewModel)
            }
        }
    }
}
