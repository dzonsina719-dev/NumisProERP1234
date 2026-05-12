package com.numisproerp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.numisproerp.ui.i18n.tr
import com.numisproerp.ui.theme.AccentBlue
import com.numisproerp.ui.theme.IOSDesign
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Запис історії, незалежний від типу: дата, кількість, сума, ім'я товару.
 * Підходить і для закупівель (постачальник), і для продажів (клієнт), і для
 * будь-яких інших агрегацій по дню.
 */
data class HistoryRecord(
    val date: Long,
    val productName: String,
    val quantity: Int,
    val totalAmount: Double
)

/**
 * Згруповує записи історії за добою (00:00 локального часу) і повертає мапу
 * `дата (мс на початок дня) → список записів`. Дні відсортовано за спаданням
 * (найновіші зверху).
 */
fun groupHistoryByDay(records: List<HistoryRecord>): List<Pair<Long, List<HistoryRecord>>> {
    val byDay = records.groupBy { startOfDay(it.date) }
    return byDay
        .toSortedMap(compareByDescending { it })
        .map { (day, items) -> day to items.sortedByDescending { it.date } }
}

/** Повертає `timestamp` для 00:00 того ж дня у локальному часовому поясі. */
fun startOfDay(timestamp: Long): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

/** Повертає `timestamp` для 23:59:59.999 того ж дня. */
fun endOfDay(timestamp: Long): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    return cal.timeInMillis
}

/**
 * Рядок-фільтр часового діапазону з двома кнопками (Від / До) та хрестиком
 * скидання. Передає батьківському компоненту обрані `startMillis` та `endMillis`
 * (включно). `null` означає «без обмеження» по відповідному боці.
 *
 * Використовує Material3 Compose `DatePicker` з явним перемикачем року і місяців,
 * щоб давав обирати будь-який рік (в тому числі майбутній) — не native
 * спіннер-варіант Android DatePickerDialog, в якому на деяких прошивках важко
 * вибрати дистантний день.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeFilterRow(
    startMillis: Long?,
    endMillis: Long?,
    onChange: (Long?, Long?) -> Unit
) {
    val df = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    if (showFromPicker) {
        DateChooserDialog(
            initialMillis = startMillis,
            onDismiss = { showFromPicker = false },
            onConfirm = { picked ->
                onChange(startOfDay(picked), endMillis)
                showFromPicker = false
            }
        )
    }
    if (showToPicker) {
        DateChooserDialog(
            initialMillis = endMillis,
            onDismiss = { showToPicker = false },
            onConfirm = { picked ->
                onChange(startMillis, endOfDay(picked))
                showToPicker = false
            }
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = { showFromPicker = true },
            shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius),
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = startMillis?.let { tr("Від ", "From ") + df.format(Date(it)) }
                    ?: tr("Від", "From"),
                fontSize = 12.sp,
                maxLines = 1
            )
        }
        OutlinedButton(
            onClick = { showToPicker = true },
            shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius),
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = endMillis?.let { tr("До ", "To ") + df.format(Date(it)) }
                    ?: tr("До", "To"),
                fontSize = 12.sp,
                maxLines = 1
            )
        }
        if (startMillis != null || endMillis != null) {
            IconButton(
                onClick = { onChange(null, null) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = tr("Скинути", "Clear"),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Заголовок дня з агрегованими сумами: дата + загальна кількість + загальна
 * сума. Під ним — деталі записів за цей день.
 *
 * @param currencyFormat функція форматування грошової суми (для локалізації).
 */
@Composable
fun DayGroupCard(
    day: Long,
    records: List<HistoryRecord>,
    summaryColor: androidx.compose.ui.graphics.Color = AccentBlue,
    currencyFormat: (Double) -> String = { String.format("%,.2f \u20b4", it) }
) {
    val df = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    val totalQty = records.sumOf { it.quantity }
    val totalAmount = records.sumOf { it.totalAmount }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(IOSDesign.CardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = summaryColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = df.format(Date(day)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${tr("Шт.", "Pcs")}: $totalQty",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = currencyFormat(totalAmount),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = summaryColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            records.forEach { rec ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = rec.productName,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    Text(
                        text = "${rec.quantity} ${tr("шт.", "pcs")}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currencyFormat(rec.totalAmount),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = summaryColor
                    )
                }
            }
        }
    }
}

/**
 * Material3 Compose DatePicker у власному діалозі. Дозволяє вибрати **будь-яку**
 * дату (минулу або майбутню) — діапазон років `1900..2100`. На відміну від
 * native `android.app.DatePickerDialog`, що на деяких прошивках падає у
 * спіннер-режим з обмеженим вибором.
 *
 * Час повертається у мс **локального часового поясу** (00:00 цього дня).
 * Подальша нормалізація (startOfDay/endOfDay) робиться у викликаючому коді.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateChooserDialog(
    initialMillis: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis ?: System.currentTimeMillis(),
        yearRange = 1900..2100,
        initialDisplayMode = DisplayMode.Picker
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val utc = state.selectedDateMillis
                if (utc != null) {
                    // Material3 повертає UTC-мс ОБРАНОЇ КАЛЕНДАРНОЇ ДАТИ (00:00 UTC).
                    // Конвертуємо у мс локального часового поясу того ж дня,
                    // щоб `startOfDay`/`endOfDay` коректно його зрізали.
                    val tz = TimeZone.getDefault()
                    val local = utc - tz.getOffset(utc)
                    onConfirm(local)
                } else {
                    onDismiss()
                }
            }) { Text(tr("Готово", "OK")) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(tr("Скасувати", "Cancel")) }
        }
    ) {
        DatePicker(state = state)
    }
}

