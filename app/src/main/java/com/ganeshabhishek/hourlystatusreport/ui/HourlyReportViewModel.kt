package com.ganeshabhishek.hourlystatusreport.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ganeshabhishek.hourlystatusreport.data.DailyHourlyReport
import com.ganeshabhishek.hourlystatusreport.data.DefaultHourlySlots
import com.ganeshabhishek.hourlystatusreport.data.HourlyReportRepository
import com.ganeshabhishek.hourlystatusreport.data.HourlySlot
import com.ganeshabhishek.hourlystatusreport.data.ReportFormatUtils
import com.ganeshabhishek.hourlystatusreport.data.SlotStatus
import com.ganeshabhishek.hourlystatusreport.network.AiWriteDownAction
import com.ganeshabhishek.hourlystatusreport.network.GeminiAiAssistant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ChatMessageUi(
    val id: String,
    val isUser: Boolean,
    val content: String,
    val timestamp: String,
    val writtenDown: AiWriteDownAction? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class HourlyReportViewModel(
    private val repository: HourlyReportRepository
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val clockFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.US)
    private val chatTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)

    private val _selectedDate = MutableStateFlow(LocalDate.now().format(dateFormatter))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    val currentReport: StateFlow<DailyHourlyReport> = _selectedDate
        .flatMapLatest { date ->
            repository.ensureDateInitialized(date)
            repository.observeReport(date)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DailyHourlyReport(
                id = "rep_${_selectedDate.value}",
                date = _selectedDate.value,
                slots = DefaultHourlySlots.SLOTS,
                totalLoggedHours = 0.5,
                targetHours = 12.0,
                updatedAt = ""
            )
        )

    private val _currentTimeText = MutableStateFlow(LocalTime.now().format(clockFormatter))
    val currentTimeText: StateFlow<String> = _currentTimeText.asStateFlow()

    private val _currentSlotId = MutableStateFlow<String?>(null)
    val currentSlotId: StateFlow<String?> = _currentSlotId.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _copiedRecently = MutableStateFlow(false)
    val copiedRecently: StateFlow<Boolean> = _copiedRecently.asStateFlow()

    private val _isAiChatOpen = MutableStateFlow(false)
    val isAiChatOpen: StateFlow<Boolean> = _isAiChatOpen.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessageUi>>(
        listOf(initialGreetingMessage())
    )
    val chatMessages: StateFlow<List<ChatMessageUi>> = _chatMessages.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                val nowTime = LocalTime.now()
                _currentTimeText.value = nowTime.format(clockFormatter)
                updateActiveSlotId(nowTime)
                delay(1000L)
            }
        }
    }

    private fun initialGreetingMessage(): ChatMessageUi {
        return ChatMessageUi(
            id = "init_1",
            isUser = false,
            content = "Hello! I am your AI assistant. Tell me what you did or what you're doing, and say \"I'm just saying, write it down.\"—I will immediately record it into your 12-hour status report.",
            timestamp = LocalTime.now().format(chatTimeFormatter)
        )
    }

    private fun updateActiveSlotId(nowTime: LocalTime) {
        val todayStr = LocalDate.now().format(dateFormatter)
        if (_selectedDate.value != todayStr) {
            _currentSlotId.value = null
            return
        }
        val nowMinutes = nowTime.hour * 60 + nowTime.minute
        val matched = DefaultHourlySlots.SLOTS.find { s ->
            val startTotal = s.startHour * 60 + s.startMinute
            val endTotal = s.endHour * 60 + s.endMinute
            nowMinutes in startTotal until endTotal
        }
        _currentSlotId.value = matched?.id
    }

    fun selectDate(dateStr: String) {
        _selectedDate.value = dateStr
        updateActiveSlotId(LocalTime.now())
    }

    fun prevDay() {
        val current = runCatching { LocalDate.parse(_selectedDate.value, dateFormatter) }
            .getOrElse { LocalDate.now() }
        selectDate(current.minusDays(1).format(dateFormatter))
    }

    fun nextDay() {
        val current = runCatching { LocalDate.parse(_selectedDate.value, dateFormatter) }
            .getOrElse { LocalDate.now() }
        selectDate(current.plusDays(1).format(dateFormatter))
    }

    fun goToToday() {
        selectDate(LocalDate.now().format(dateFormatter))
    }

    fun updateSlotActivity(slotId: String, activity: String) {
        val date = _selectedDate.value
        viewModelScope.launch {
            repository.updateSlot(date = date, slotId = slotId, activity = activity)
        }
    }

    fun toggleSlotStatus(slot: HourlySlot) {
        if (slot.isBreak) return
        val date = _selectedDate.value
        val nextStatus = slot.status.nextToggleStatus()
        viewModelScope.launch {
            repository.updateSlot(date = date, slotId = slot.id, status = nextStatus)
        }
    }

    fun applySampleTemplate() {
        val date = _selectedDate.value
        viewModelScope.launch {
            repository.applyTemplate(date)
            showToast("Sample 12-hour log applied.")
        }
    }

    fun resetCurrentReport() {
        val date = _selectedDate.value
        viewModelScope.launch {
            repository.resetReport(date)
            showToast("Hourly report reset to empty.")
        }
    }

    fun importReportText(rawText: String, fileName: String = "") {
        val date = _selectedDate.value
        if (rawText.isBlank()) {
            showToast("Uploaded file or text is empty.")
            return
        }
        val parsed = ReportFormatUtils.parseImportedText(rawText, fileName)
        if (parsed.isEmpty()) {
            showToast("Could not find hourly status activities in the uploaded content.")
            return
        }
        viewModelScope.launch {
            val count = repository.importReport(date, parsed)
            showToast("Hourly status report uploaded successfully ($count activities updated).")
        }
    }

    fun markCopied() {
        _copiedRecently.value = true
        showToast("Report copied to clipboard.")
        viewModelScope.launch {
            delay(2500L)
            _copiedRecently.value = false
        }
    }

    fun showToast(message: String) {
        _toastMessage.value = message
        viewModelScope.launch {
            delay(3200L)
            if (_toastMessage.value == message) {
                _toastMessage.value = null
            }
        }
    }

    fun dismissToast() {
        _toastMessage.value = null
    }

    fun setAiChatOpen(open: Boolean) {
        _isAiChatOpen.value = open
    }

    fun clearAiChat() {
        _chatMessages.value = listOf(
            ChatMessageUi(
                id = "init_reset_${System.currentTimeMillis()}",
                isUser = false,
                content = "Chat cleared. What should I write down into your 12-hour report?",
                timestamp = LocalTime.now().format(chatTimeFormatter)
            )
        )
    }

    fun sendAiMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _isAiLoading.value) return

        val userMsg = ChatMessageUi(
            id = "user_${System.currentTimeMillis()}",
            isUser = true,
            content = trimmed,
            timestamp = LocalTime.now().format(chatTimeFormatter)
        )
        _chatMessages.value = _chatMessages.value + userMsg
        _isAiLoading.value = true

        val reportSnapshot = currentReport.value
        val activeSlotId = _currentSlotId.value ?: reportSnapshot.slots.firstOrNull()?.id
        val date = _selectedDate.value

        viewModelScope.launch {
            val result = GeminiAiAssistant.processChatMessage(
                message = trimmed,
                currentSlotId = activeSlotId,
                currentReport = reportSnapshot
            )

            result.writtenDown?.let { action ->
                repository.updateSlot(
                    date = date,
                    slotId = action.slotId,
                    activity = action.activity,
                    status = SlotStatus.COMPLETED
                )
            }

            val botMsg = ChatMessageUi(
                id = "bot_${System.currentTimeMillis()}",
                isUser = false,
                content = result.reply,
                timestamp = LocalTime.now().format(chatTimeFormatter),
                writtenDown = result.writtenDown
            )
            _chatMessages.value = _chatMessages.value + botMsg
            _isAiLoading.value = false
        }
    }

    companion object {
        fun provideFactory(repository: HourlyReportRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HourlyReportViewModel(repository) as T
                }
            }
        }
    }
}
