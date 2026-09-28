package com.ganeshabhishek.hourlystatusreport.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ganeshabhishek.hourlystatusreport.R
import com.ganeshabhishek.hourlystatusreport.data.DailyHourlyReport
import com.ganeshabhishek.hourlystatusreport.data.HourlySlot
import com.ganeshabhishek.hourlystatusreport.data.ReportFormatUtils
import com.ganeshabhishek.hourlystatusreport.data.SlotStatus
import com.ganeshabhishek.hourlystatusreport.ui.components.AIChatBottomSheet
import com.ganeshabhishek.hourlystatusreport.ui.theme.Amber200
import com.ganeshabhishek.hourlystatusreport.ui.theme.Amber300
import com.ganeshabhishek.hourlystatusreport.ui.theme.Amber50
import com.ganeshabhishek.hourlystatusreport.ui.theme.Amber700
import com.ganeshabhishek.hourlystatusreport.ui.theme.Amber800
import com.ganeshabhishek.hourlystatusreport.ui.theme.Emerald200
import com.ganeshabhishek.hourlystatusreport.ui.theme.Emerald400
import com.ganeshabhishek.hourlystatusreport.ui.theme.Emerald50
import com.ganeshabhishek.hourlystatusreport.ui.theme.Emerald500
import com.ganeshabhishek.hourlystatusreport.ui.theme.Emerald600
import com.ganeshabhishek.hourlystatusreport.ui.theme.Emerald700
import com.ganeshabhishek.hourlystatusreport.ui.theme.Emerald800
import com.ganeshabhishek.hourlystatusreport.ui.theme.Indigo100
import com.ganeshabhishek.hourlystatusreport.ui.theme.Indigo200
import com.ganeshabhishek.hourlystatusreport.ui.theme.Indigo50
import com.ganeshabhishek.hourlystatusreport.ui.theme.Indigo600
import com.ganeshabhishek.hourlystatusreport.ui.theme.Indigo700
import com.ganeshabhishek.hourlystatusreport.ui.theme.Indigo800
import com.ganeshabhishek.hourlystatusreport.ui.theme.Indigo950
import com.ganeshabhishek.hourlystatusreport.ui.theme.Rose200
import com.ganeshabhishek.hourlystatusreport.ui.theme.Rose50
import com.ganeshabhishek.hourlystatusreport.ui.theme.Rose800
import com.ganeshabhishek.hourlystatusreport.ui.theme.Slate100
import com.ganeshabhishek.hourlystatusreport.ui.theme.Slate200
import com.ganeshabhishek.hourlystatusreport.ui.theme.Slate300
import com.ganeshabhishek.hourlystatusreport.ui.theme.Slate400
import com.ganeshabhishek.hourlystatusreport.ui.theme.Slate50
import com.ganeshabhishek.hourlystatusreport.ui.theme.Slate500
import com.ganeshabhishek.hourlystatusreport.ui.theme.Slate600
import com.ganeshabhishek.hourlystatusreport.ui.theme.Slate700
import com.ganeshabhishek.hourlystatusreport.ui.theme.Slate900
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HourlyStatusReportScreen(
    viewModel: HourlyReportViewModel
) {
    val context = LocalContext.current
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val report by viewModel.currentReport.collectAsStateWithLifecycle()
    val currentTimeText by viewModel.currentTimeText.collectAsStateWithLifecycle()
    val currentSlotId by viewModel.currentSlotId.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val copiedRecently by viewModel.copiedRecently.collectAsStateWithLifecycle()
    val isAiChatOpen by viewModel.isAiChatOpen.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()

    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    var showUploadDialog by rememberSaveable { mutableStateOf(false) }
    var showDatePickerDialog by rememberSaveable { mutableStateOf(false) }

    // CSV Export Launcher
    val createCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                val csv = ReportFormatUtils.formatAsCsv(report)
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(csv.toByteArray(Charsets.UTF_8))
                }
                viewModel.showToast("CSV exported for $selectedDate.")
            }.onFailure {
                viewModel.showToast("Failed to save CSV file.")
            }
        }
    }

    // File Upload Launcher (.csv, .json, .txt)
    val openReportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                val content = context.contentResolver.openInputStream(uri)
                    ?.bufferedReader(Charsets.UTF_8)
                    ?.use { it.readText() }
                    .orEmpty()
                val pathName = uri.lastPathSegment.orEmpty()
                viewModel.importReportText(content, pathName)
                showUploadDialog = false
            }.onFailure {
                viewModel.showToast("Could not read selected file.")
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopNavBar(
                onOpenAiChat = { viewModel.setAiChatOpen(true) }
            )
        },
        floatingActionButton = {
            if (!isAiChatOpen) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.setAiChatOpen(true) },
                    containerColor = Indigo600,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("fab_write_it_down"),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Amber300,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.write_it_down),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
                    .testTag("hourly_report_list"),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Toast Banner
                item {
                    AnimatedVisibility(
                        visible = toastMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        toastMessage?.let { msg ->
                            ToastBanner(
                                message = msg,
                                onDismiss = { viewModel.dismissToast() }
                            )
                        }
                    }
                }

                // Main Header Card: Hourly Status Report (X/12 hours)
                item {
                    MainHeaderCard(
                        report = report,
                        selectedDate = selectedDate,
                        currentTimeText = currentTimeText,
                        copiedRecently = copiedRecently,
                        onOpenAiChat = { viewModel.setAiChatOpen(true) },
                        onCopyReport = {
                            copyReportToClipboard(context, report)
                            viewModel.markCopied()
                        },
                        onShareReport = {
                            shareReportText(context, report)
                        },
                        onExportCsv = {
                            createCsvLauncher.launch("hourly_status_report_$selectedDate.csv")
                        },
                        onOpenUpload = { showUploadDialog = true },
                        onApplySample = { viewModel.applySampleTemplate() },
                        onResetReport = { showResetDialog = true },
                        onPrevDay = { viewModel.prevDay() },
                        onNextDay = { viewModel.nextDay() },
                        onOpenDatePicker = { showDatePickerDialog = true },
                        onToday = { viewModel.goToToday() }
                    )
                }

                // Table Section Header
                item {
                    TableSectionHeader(slotCount = report.slots.size)
                }

                // 13 Hourly Slots
                items(
                    items = report.slots,
                    key = { slot -> "${report.date}_${slot.id}" }
                ) { slot ->
                    HourlySlotRowCard(
                        slot = slot,
                        isCurrent = currentSlotId == slot.id,
                        onActivityChange = { newText ->
                            viewModel.updateSlotActivity(slot.id, newText)
                        },
                        onToggleStatus = {
                            viewModel.toggleSlotStatus(slot)
                        }
                    )
                }

                // Summary Footer Card
                item {
                    SummaryFooterCard(
                        report = report,
                        onOpenAiChat = { viewModel.setAiChatOpen(true) }
                    )
                }
            }
        }
    }

    // AI Chat Bottom Sheet
    AIChatBottomSheet(
        isOpen = isAiChatOpen,
        onClose = { viewModel.setAiChatOpen(false) },
        currentReport = report,
        currentSlotId = currentSlotId,
        messages = chatMessages,
        isLoading = isAiLoading,
        onSendMessage = { viewModel.sendAiMessage(it) },
        onClearChat = { viewModel.clearAiChat() }
    )

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Hourly Report?") },
            text = {
                Text("Reset all logged activities for $selectedDate? The 1:00 – 1:30 PM Lunch break will remain scheduled.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetCurrentReport()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose800),
                    modifier = Modifier.testTag("confirm_reset_button")
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Upload / Import Report Dialog (File Picker + Paste CSV/JSON/Text)
    if (showUploadDialog) {
        UploadReportDialog(
            onDismiss = { showUploadDialog = false },
            onPickFile = {
                openReportFileLauncher.launch(
                    arrayOf("text/*", "application/json", "text/csv", "text/comma-separated-values")
                )
            },
            onImportPastedText = { pasted ->
                viewModel.importReportText(pasted, "pasted_report.txt")
                showUploadDialog = false
            }
        )
    }

    // DatePicker Dialog
    if (showDatePickerDialog) {
        val initialMillis = remember(selectedDate) {
            runCatching {
                LocalDate.parse(selectedDate, DateTimeFormatter.ISO_LOCAL_DATE)
                    .atStartOfDay(ZoneOffset.UTC)
                    .toInstant()
                    .toEpochMilli()
            }.getOrElse { System.currentTimeMillis() }
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val picked = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                                .format(DateTimeFormatter.ISO_LOCAL_DATE)
                            viewModel.selectDate(picked)
                        }
                        showDatePickerDialog = false
                    }
                ) {
                    Text("Select")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun TopNavBar(
    onOpenAiChat: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = Slate200)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Slate900),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "12 Hours Workday · 9:00 AM – 9:00 PM",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Indigo50,
                modifier = Modifier
                    .border(1.dp, Indigo200, RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onOpenAiChat)
                    .testTag("top_bar_write_it_down_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Indigo600,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(R.string.write_it_down),
                        style = MaterialTheme.typography.labelLarge,
                        color = Indigo700
                    )
                }
            }
        }
    }
}

@Composable
private fun ToastBanner(
    message: String,
    onDismiss: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Slate900,
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("toast_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Emerald400,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss notification",
                    tint = Slate400,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun MainHeaderCard(
    report: DailyHourlyReport,
    selectedDate: String,
    currentTimeText: String,
    copiedRecently: Boolean,
    onOpenAiChat: () -> Unit,
    onCopyReport: () -> Unit,
    onShareReport: () -> Unit,
    onExportCsv: () -> Unit,
    onOpenUpload: () -> Unit,
    onApplySample: () -> Unit,
    onResetReport: () -> Unit,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onOpenDatePicker: () -> Unit,
    onToday: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate200, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Visual Hero Banner with Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_banner),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Indigo950.copy(alpha = 0.92f),
                                    Slate900.copy(alpha = 0.78f),
                                    Indigo950.copy(alpha = 0.55f)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Indigo200,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = stringResource(R.string.header_badge).uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 0.8.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Indigo100
                        )
                    }

                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                                append("Hourly Status Report ")
                            }
                            withStyle(
                                SpanStyle(
                                    color = Emerald400,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("(${report.formattedLoggedHours}/12 hours)")
                            }
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.testTag("report_header_title")
                    )

                    Text(
                        text = stringResource(R.string.operational_schedule),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate300
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Progress Bar
                val progress = (report.totalLoggedHours / 12.0).toFloat().coerceIn(0f, 1f)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Shift Completion",
                            style = MaterialTheme.typography.labelLarge,
                            color = Slate600
                        )
                        Text(
                            text = "${(progress * 100).toInt()}% (${report.formattedLoggedHours} / 12.0 hrs)",
                            style = MaterialTheme.typography.labelMedium,
                            color = Indigo600
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        color = Indigo600,
                        trackColor = Slate100,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }

                // Action Toolbar (scrollable row of action buttons)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // AI Chat Help
                    ActionToolbarButton(
                        text = stringResource(R.string.ai_chat_help),
                        icon = Icons.Default.AutoAwesome,
                        containerColor = Indigo600,
                        contentColor = Color.White,
                        borderColor = Indigo600,
                        testTag = "toolbar_ai_chat_button",
                        onClick = onOpenAiChat
                    )

                    // Copy Report
                    ActionToolbarButton(
                        text = if (copiedRecently) stringResource(R.string.copied) else stringResource(R.string.copy_report),
                        icon = if (copiedRecently) Icons.Default.Check else Icons.Default.ContentCopy,
                        containerColor = if (copiedRecently) Emerald50 else Color.White,
                        contentColor = if (copiedRecently) Emerald700 else Slate700,
                        borderColor = if (copiedRecently) Emerald200 else Slate300,
                        testTag = "toolbar_copy_button",
                        onClick = onCopyReport
                    )

                    // Export CSV
                    ActionToolbarButton(
                        text = stringResource(R.string.export_csv),
                        icon = Icons.Default.Download,
                        containerColor = Color.White,
                        contentColor = Slate700,
                        borderColor = Slate300,
                        testTag = "toolbar_export_csv_button",
                        onClick = onExportCsv
                    )

                    // Upload Report
                    ActionToolbarButton(
                        text = stringResource(R.string.upload_report),
                        icon = Icons.Default.FileUpload,
                        containerColor = Indigo50,
                        contentColor = Indigo700,
                        borderColor = Indigo200,
                        testTag = "toolbar_upload_button",
                        onClick = onOpenUpload
                    )

                    // Share Report
                    ActionToolbarButton(
                        text = stringResource(R.string.share_report),
                        icon = Icons.Default.Share,
                        containerColor = Color.White,
                        contentColor = Slate700,
                        borderColor = Slate300,
                        testTag = "toolbar_share_button",
                        onClick = onShareReport
                    )

                    // Sample Log Template
                    ActionToolbarButton(
                        text = stringResource(R.string.apply_template),
                        icon = Icons.AutoMirrored.Filled.PlaylistAddCheck,
                        containerColor = Color.White,
                        contentColor = Slate700,
                        borderColor = Slate300,
                        testTag = "toolbar_sample_button",
                        onClick = onApplySample
                    )

                    // Reset Button
                    ActionToolbarButton(
                        text = stringResource(R.string.reset_report),
                        icon = Icons.Default.Refresh,
                        containerColor = Color.White,
                        contentColor = Slate600,
                        borderColor = Slate200,
                        testTag = "toolbar_reset_button",
                        onClick = onResetReport
                    )
                }

                // Live Current Time Show & Shift Info
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Slate50,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Slate200, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Indigo50)
                                    .border(1.dp, Indigo200, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Indigo600,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = stringResource(R.string.current_time_label).uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Slate500
                                )
                                Text(
                                    text = currentTimeText,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Slate900,
                                    modifier = Modifier.testTag("live_clock_text")
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Emerald500)
                                )
                                Text(
                                    text = "Shift: 9:00 AM – 9:00 PM",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = Slate900
                                )
                            }
                            Text(
                                text = "12 Hours Workday Schedule",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate500
                            )
                        }
                    }
                }

                HorizontalDivider(color = Slate100)

                // Date Selector Sub-bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = onPrevDay,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                                .testTag("prev_day_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = stringResource(R.string.previous_day),
                                tint = Slate700
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Slate50,
                            modifier = Modifier
                                .border(1.dp, Slate300, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onOpenDatePicker)
                                .testTag("date_selector_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = Slate500,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = selectedDate,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Slate900
                                )
                            }
                        }

                        IconButton(
                            onClick = onNextDay,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                                .testTag("next_day_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = stringResource(R.string.next_day),
                                tint = Slate700
                            )
                        }

                        TextButton(
                            onClick = onToday,
                            modifier = Modifier.testTag("today_button")
                        ) {
                            Text(
                                text = stringResource(R.string.today_button),
                                style = MaterialTheme.typography.labelLarge,
                                color = Indigo600
                            )
                        }
                    }

                    Text(
                        text = "${report.slots.size} Hourly Slots",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionToolbarButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        modifier = Modifier
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor
            )
        }
    }
}

@Composable
private fun TableSectionHeader(slotCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "TIME & ACTIVITY LOG",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            ),
            color = Slate500
        )
        Text(
            text = "TAP STATUS TO TOGGLE ($slotCount SLOTS)",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            ),
            color = Slate400
        )
    }
}

@Composable
private fun HourlySlotRowCard(
    slot: HourlySlot,
    isCurrent: Boolean,
    onActivityChange: (String) -> Unit,
    onToggleStatus: () -> Unit
) {
    val hasContent = slot.activity.trim().isNotEmpty()
    val cardBg = when {
        isCurrent -> Indigo50.copy(alpha = 0.65f)
        slot.isBreak -> Amber50.copy(alpha = 0.55f)
        else -> Color.White
    }
    val borderColor = when {
        isCurrent -> Indigo600
        slot.isBreak -> Amber200
        hasContent -> Slate300
        else -> Slate200
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent) 2.dp else 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isCurrent) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .testTag("slot_card_${slot.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Time Range + Now Badge + Duration + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = slot.timeRange,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Slate900
                    )

                    if (isCurrent) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Indigo100
                        ) {
                            Text(
                                text = "NOW",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Indigo700,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = if (slot.durationHours == 0.5) "· 0.5 hr" else "· 1.0 hr",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400
                    )
                }

                // Status Pill
                if (slot.isBreak) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Amber50,
                        modifier = Modifier.border(1.dp, Amber200, RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = "Break",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                            color = Amber700,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                } else {
                    StatusBadgeButton(
                        status = slot.status,
                        slotId = slot.id,
                        onClick = onToggleStatus
                    )
                }
            }

            // Activity Input or Break Info
            if (slot.isBreak) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.lunch_break_title),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Amber800
                    )
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate400
                    )
                    Text(
                        text = stringResource(R.string.lunch_break_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate600
                    )
                }
            } else {
                OutlinedTextField(
                    value = slot.activity,
                    onValueChange = onActivityChange,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.activity_placeholder),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate400,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = if (hasContent) Color.White else Slate50,
                        focusedBorderColor = Slate900,
                        unfocusedBorderColor = if (hasContent) Slate300 else Slate200,
                        focusedTextColor = Slate900,
                        unfocusedTextColor = Slate900
                    ),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("slot_input_${slot.id}")
                )
            }
        }
    }
}

@Composable
private fun StatusBadgeButton(
    status: SlotStatus,
    slotId: String,
    onClick: () -> Unit
) {
    val (bgColor, textColor, borderColor) = when (status) {
        SlotStatus.COMPLETED -> Triple(Emerald50, Emerald800, Emerald200)
        SlotStatus.IN_PROGRESS -> Triple(Indigo50, Indigo800, Indigo200)
        SlotStatus.BLOCKED -> Triple(Rose50, Rose800, Rose200)
        SlotStatus.PENDING -> Triple(Slate100, Slate600, Slate200)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = Modifier
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .testTag("slot_status_$slotId")
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun SummaryFooterCard(
    report: DailyHourlyReport,
    onOpenAiChat: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Slate50,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate200, RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Slate500,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "12-Hour Daily Work Schedule (9:00 AM – 9:00 PM). Use \"Write It Down\" or \"Copy Report\" to log and export.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate600,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(color = Slate200)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onOpenAiChat)
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Indigo600,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "AI Chat: Write It Down",
                        style = MaterialTheme.typography.labelLarge,
                        color = Indigo600
                    )
                }

                Text(
                    text = "${report.formattedLoggedHours}/12 Hours Tracked · Target: 12.0 hours",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500
                )
            }
        }
    }
}

@Composable
private fun UploadReportDialog(
    onDismiss: () -> Unit,
    onPickFile: () -> Unit,
    onImportPastedText: (String) -> Unit
) {
    var pastedContent by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Upload / Import Hourly Status Report",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Select a CSV, JSON, or TXT report file from your device, or paste formatted report lines below.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate600
                )

                OutlinedButton(
                    onClick = onPickFile,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pick_report_file_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = null,
                        tint = Indigo600,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choose File (.csv, .json, .txt)")
                }

                OutlinedTextField(
                    value = pastedContent,
                    onValueChange = { pastedContent = it },
                    placeholder = {
                        Text(
                            text = "Or paste CSV / JSON / copied report text here...\ne.g. 9:00 – 10:00 AM Morning standup",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate400
                        )
                    },
                    minLines = 4,
                    maxLines = 8,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("paste_report_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onImportPastedText(pastedContent) },
                enabled = pastedContent.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                modifier = Modifier.testTag("confirm_import_text_button")
            ) {
                Text("Import Text")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun copyReportToClipboard(context: Context, report: DailyHourlyReport) {
    val formatted = ReportFormatUtils.formatForCopy(report)
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText("Hourly Status Report", formatted))
}

private fun shareReportText(context: Context, report: DailyHourlyReport) {
    val formatted = ReportFormatUtils.formatForCopy(report)
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Hourly Status Report (${report.formattedLoggedHours}/12 hours) - ${report.date}")
        putExtra(Intent.EXTRA_TEXT, formatted)
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share Hourly Status Report"))
}
