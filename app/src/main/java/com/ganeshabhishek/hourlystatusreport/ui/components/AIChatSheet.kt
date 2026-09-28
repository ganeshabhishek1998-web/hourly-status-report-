package com.ganeshabhishek.hourlystatusreport.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganeshabhishek.hourlystatusreport.R
import com.ganeshabhishek.hourlystatusreport.data.DailyHourlyReport
import com.ganeshabhishek.hourlystatusreport.ui.ChatMessageUi
import com.ganeshabhishek.hourlystatusreport.ui.theme.Emerald200
import com.ganeshabhishek.hourlystatusreport.ui.theme.Emerald400
import com.ganeshabhishek.hourlystatusreport.ui.theme.Emerald50
import com.ganeshabhishek.hourlystatusreport.ui.theme.Emerald600
import com.ganeshabhishek.hourlystatusreport.ui.theme.Emerald800
import com.ganeshabhishek.hourlystatusreport.ui.theme.Emerald900
import com.ganeshabhishek.hourlystatusreport.ui.theme.Indigo100
import com.ganeshabhishek.hourlystatusreport.ui.theme.Indigo200
import com.ganeshabhishek.hourlystatusreport.ui.theme.Indigo50
import com.ganeshabhishek.hourlystatusreport.ui.theme.Indigo600
import com.ganeshabhishek.hourlystatusreport.ui.theme.Indigo700
import com.ganeshabhishek.hourlystatusreport.ui.theme.Slate100
import com.ganeshabhishek.hourlystatusreport.ui.theme.Slate200
import com.ganeshabhishek.hourlystatusreport.ui.theme.Slate400
import com.ganeshabhishek.hourlystatusreport.ui.theme.Slate50
import com.ganeshabhishek.hourlystatusreport.ui.theme.Slate600
import com.ganeshabhishek.hourlystatusreport.ui.theme.Slate700
import com.ganeshabhishek.hourlystatusreport.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIChatBottomSheet(
    isOpen: Boolean,
    onClose: () -> Unit,
    currentReport: DailyHourlyReport,
    currentSlotId: String?,
    messages: List<ChatMessageUi>,
    isLoading: Boolean,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit
) {
    if (!isOpen) return

    BackHandler(onBack = onClose)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var inputText by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    val activeSlot = currentReport.slots.find { it.id == currentSlotId }
        ?: currentReport.slots.first()

    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = Color.White,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        modifier = Modifier.testTag("ai_chat_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.86f)
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Indigo600),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.ai_chat_title),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Emerald400)
                            )
                        }
                        Text(
                            text = "Active Slot: ${activeSlot.timeRange}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate400
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onClearChat,
                        modifier = Modifier.testTag("ai_chat_clear_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.clear_chat),
                            tint = Slate400
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("ai_chat_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close_chat),
                            tint = Slate400
                        )
                    }
                }
            }

            // Quick "Write It Down" Suggestions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate50)
                    .border(width = 1.dp, color = Slate200)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickSuggestionChip(
                    label = "Write down: \"I'm just saying, write it down.\"",
                    highlighted = true,
                    testTag = "chip_write_it_down",
                    onClick = { onSendMessage("Write it down: I'm just saying, write it down.") }
                )
                QuickSuggestionChip(
                    label = "Write down: \"It's a machine\"",
                    highlighted = false,
                    testTag = "chip_its_a_machine",
                    onClick = { onSendMessage("Write it down: It's a machine") }
                )
                QuickSuggestionChip(
                    label = "Morning standup",
                    highlighted = false,
                    testTag = "chip_morning_standup",
                    onClick = { onSendMessage("Write it down: Morning standup & sprint ticket triage") }
                )
            }

            // Message Feed
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.White),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatMessageBubble(msg = msg)
                }

                if (isLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Indigo100),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = Indigo600
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Slate100
                            ) {
                                Text(
                                    text = "Writing it down...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Slate600,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Input Form with direct "Write Down" button
            Surface(
                color = Color.White,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = Slate200)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.ai_input_placeholder),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Slate400,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (inputText.isNotBlank() && !isLoading) {
                                        onSendMessage(inputText)
                                        inputText = ""
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Slate50,
                                unfocusedContainerColor = Slate50,
                                focusedBorderColor = Slate900,
                                unfocusedBorderColor = Slate200,
                                focusedTextColor = Slate900,
                                unfocusedTextColor = Slate900
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_chat_input_field")
                        )

                        Button(
                            onClick = {
                                if (inputText.isNotBlank() && !isLoading) {
                                    onSendMessage(inputText)
                                    inputText = ""
                                }
                            },
                            enabled = inputText.isNotBlank() && !isLoading,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Indigo600,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                            modifier = Modifier.testTag("ai_chat_send_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Write Down",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Target Slot: ${activeSlot.timeRange}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate400
                        )
                        Text(
                            text = "12-Hour Schedule",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate400
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickSuggestionChip(
    label: String,
    highlighted: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val bgColor = if (highlighted) Indigo50 else Color.White
    val borderColor = if (highlighted) Indigo200 else Slate200
    val textColor = if (highlighted) Indigo700 else Slate700

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = null,
            tint = if (highlighted) Indigo600 else Slate600,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (highlighted) FontWeight.SemiBold else FontWeight.Medium,
                fontSize = 11.sp
            ),
            color = textColor
        )
    }
}

@Composable
private fun ChatMessageBubble(msg: ChatMessageUi) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!msg.isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Indigo100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = Indigo600,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        val bubbleShape = if (msg.isUser) {
            RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 2.dp)
        } else {
            RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 2.dp, bottomEnd = 14.dp)
        }

        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(bubbleShape)
                .background(if (msg.isUser) Slate900 else Slate100)
                .then(
                    if (!msg.isUser) Modifier.border(1.dp, Slate200, bubbleShape) else Modifier
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = msg.content,
                style = MaterialTheme.typography.bodyMedium,
                color = if (msg.isUser) Color.White else Slate900
            )

            AnimatedVisibility(visible = msg.writtenDown != null) {
                msg.writtenDown?.let { written ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Emerald50)
                            .border(1.dp, Emerald200, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Emerald600,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp)
                        )
                        Column {
                            Text(
                                text = "Written down to report:",
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                                color = Emerald900
                            )
                            Text(
                                text = written.slotTimeRange,
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate600
                            )
                            Text(
                                text = "\"${written.activity}\"",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                ),
                                color = Emerald800
                            )
                        }
                    }
                }
            }

            Text(
                text = msg.timestamp,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Slate400,
                modifier = Modifier.align(Alignment.End)
            )
        }

        if (msg.isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Slate900),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
