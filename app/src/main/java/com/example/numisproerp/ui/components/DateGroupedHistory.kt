package com.numisproerp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
 * Діалог вибору дати з трьома випадаючими списками (Рік / Місяць / День).
 *
 * Дозволяє вільно вибрати будь-який день в діапазоні 1900..2100, включно з
 * майбутніми роками. На відміну від Material3 `DatePicker`, цей підхід не
 * страждає від проблем з перехопленням кліків у scrollable-контейнерах і від native
 * Android `DatePickerDialog`, який на деяких прошивках обмежує вибір року.
 *
 * Повертає мс локального часового поясу (00:00 вибраного дня). Нормалізація
 * `startOfDay`/`endOfDay` робиться у викликаючому коді.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateChooserDialog(
    initialMillis: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val today = remember { Calendar.getInstance() }
    val initial = remember(initialMillis) {
        Calendar.getInstance().apply {
            if (initialMillis != null) timeInMillis = initialMillis
        }
    }
    var year by remember { mutableStateOf(initial.get(Calendar.YEAR)) }
    var month by remember { mutableStateOf(initial.get(Calendar.MONTH)) } // 0..11
    var day by remember { mutableStateOf(initial.get(Calendar.DAY_OF_MONTH)) }

    // Максимальний день в місяці — залежить від вибраного року/місяця.
    val maxDay = remember(year, month) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    // Якщо день виходить за межі після зміни місяця — обрізаємо.
    if (day > maxDay) day = maxDay

    val monthNames = listOf(
        tr("Січень", "January"), tr("Лютий", "February"), tr("Березень", "March"),
        tr("Квітень", "April"), tr("Травень", "May"), tr("Червень", "June"),
        tr("Липень", "July"), tr("Серпень", "August"), tr("Вересень", "September"),
        tr("Жовтень", "October"), tr("Листопад", "November"), tr("Грудень", "December")
    )
    val years = remember { (1900..2100).toList() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("Виберіть дату", "Pick a date")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Рік
                    PickerDropdown(
                        modifier = Modifier.weight(1f),
                        label = tr("Рік", "Year"),
                        value = year.toString(),
                        items = years.map { it.toString() to it },
                        onSelect = { year = it }
                    )
                    // Місяць
                    PickerDropdown(
                        modifier = Modifier.weight(1.2f),
                        label = tr("Місяць", "Month"),
                        value = monthNames[month],
                        items = monthNames.mapIndexed { idx, n -> n to idx },
                        onSelect = { month = it }
                    )
                    // День
                    PickerDropdown(
                        modifier = Modifier.weight(0.8f),
                        label = tr("День", "Day"),
                        value = day.toString(),
                        items = (1..maxDay).map { it.toString() to it },
                        onSelect = { day = it }
                    )
                }
                // Кнопка «Сьогодні» для швидкого вибору
                TextButton(onClick = {
                    year = today.get(Calendar.YEAR)
                    month = today.get(Calendar.MONTH)
                    day = today.get(Calendar.DAY_OF_MONTH)
                }) {
                    Text(tr("Сьогодні", "Today"))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, day)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                onConfirm(cal.timeInMillis)
            }) { Text(tr("Готово", "OK")) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(tr("Скасувати", "Cancel")) }
        }
    )
}

/**
 * Випадаючий список-пікер для одного поля дати (рік / місяць / день).
 * Проста OutlinedButton + DropdownMenu — працює на всіх пристроях без проблем
 * з перехопленням жестів у батьківських scrollable-контейнерах.
 */
@Composable
private fun <T> PickerDropdown(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    items: List<Pair<String, T>>,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1
                )
                Text(
                    text = value,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.height(280.dp)
        ) {
            items.forEach { (text, payload) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onSelect(payload)
                        expanded = false
                    }
                )
            }
        }
    }
}

