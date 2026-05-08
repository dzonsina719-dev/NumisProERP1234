package com.numisproerp.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.numisproerp.data.entities.CatalogItem
import com.numisproerp.ui.theme.AccentBlue
import com.numisproerp.ui.theme.AccentGreen
import com.numisproerp.ui.theme.AccentOrange
import com.numisproerp.ui.theme.IOSDesign
import com.numisproerp.ui.viewmodel.CatalogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    navController: NavHostController,
    viewModel: CatalogViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<CatalogItem?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadItems()
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.importExcelFile(context, it) { success, message ->
                android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Верхній рядок
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "Каталог НБУ",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Кнопки сортування та фільтру тільки якщо є дані
            if (uiState.isDataLoaded) {
                Row {
                    IconButton(onClick = { viewModel.toggleSortDialog(true) }) {
                        Icon(Icons.Default.Sort, contentDescription = "Сортувати", tint = AccentBlue)
                    }
                    IconButton(onClick = { viewModel.toggleFilterDialog(true) }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Фільтрувати", tint = AccentBlue)
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 72.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            // Якщо дані не завантажені - показуємо дві кнопки
            if (!uiState.isDataLoaded && !uiState.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { filePickerLauncher.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) },
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null)
                        Text("Каталог НБУ (завантажити Excel)", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
            // Якщо завантажується - показуємо прогрес
            else if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            // Якщо дані є - показуємо список товарів
            else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.items) { item ->
                        CatalogItemCard(
                            item = item,
                            onClick = {
                                selectedItem = item
                                showDetailDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Діалог сортування
    if (uiState.showSortDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleSortDialog(false) },
            title = { Text("Сортувати") },
            text = {
                Column {
                    listOf("name" to "За назвою", "date" to "За датою", "denomination" to "За номіналом").forEach { (value, label) ->
                        TextButton(
                            onClick = {
                                viewModel.setSortBy(value)
                                viewModel.toggleSortDialog(false)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.toggleSortDialog(false) }) {
                    Text("Скасувати")
                }
            }
        )
    }

    // Діалог фільтру
    if (uiState.showFilterDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleFilterDialog(false) },
            title = { Text("Фільтрувати за категорією") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    TextButton(
                        onClick = {
                            viewModel.clearCategory()
                            viewModel.toggleFilterDialog(false)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Всі категорії", fontWeight = FontWeight.Bold)
                    }
                    uiState.categories.forEach { category ->
                        TextButton(
                            onClick = {
                                viewModel.selectCategory(category)
                                viewModel.toggleFilterDialog(false)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(category)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.toggleFilterDialog(false) }) {
                    Text("Скасувати")
                }
            }
        )
    }

    // Детальна карточка товару
    if (showDetailDialog && selectedItem != null) {
        AlertDialog(
            onDismissRequest = {
                showDetailDialog = false
                selectedItem = null
            },
            title = { Text(selectedItem!!.name) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Фото
                    if (selectedItem!!.imageUrlFront.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(selectedItem!!.imageUrlFront)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Фото монети",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(IOSDesign.CardCornerRadius)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (selectedItem!!.series.isNotEmpty()) {
                                Row {
                                    Text("Серія: ", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                    Text(selectedItem!!.series, fontSize = 13.sp)
                                }
                            }
                            if (selectedItem!!.dateIntroduced.isNotEmpty()) {
                                Row {
                                    Text("Дата введення: ", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                    Text(selectedItem!!.dateIntroduced, fontSize = 13.sp)
                                }
                            }
                            if (selectedItem!!.material.isNotEmpty()) {
                                Row {
                                    Text("Матеріал: ", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                    Text(selectedItem!!.material, fontSize = 13.sp)
                                }
                            }
                            if (selectedItem!!.denomination.isNotEmpty()) {
                                Row {
                                    Text("Номінал: ", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                    Text("${selectedItem!!.denomination} ₴", fontSize = 13.sp)
                                }
                            }
                            if (selectedItem!!.diameter.isNotEmpty()) {
                                Row {
                                    Text("Діаметр: ", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                    Text("${selectedItem!!.diameter} мм", fontSize = 13.sp)
                                }
                            }
                            if (selectedItem!!.weight.isNotEmpty()) {
                                Row {
                                    Text("Маса: ", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                    Text("${selectedItem!!.weight} г", fontSize = 13.sp)
                                }
                            }
                            if (selectedItem!!.mintage.isNotEmpty()) {
                                Row {
                                    Text("Тираж: ", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                    Text(selectedItem!!.mintage, fontSize = 13.sp)
                                }
                            }
                            if (selectedItem!!.category.isNotEmpty()) {
                                Row {
                                    Text("Категорія: ", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                    Text(selectedItem!!.category, fontSize = 13.sp)
                                }
                            }
                            if (selectedItem!!.quality.isNotEmpty()) {
                                Row {
                                    Text("Якість карбування: ", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                    Text(selectedItem!!.quality, fontSize = 13.sp)
                                }
                            }
                            if (selectedItem!!.artist.isNotEmpty()) {
                                Row {
                                    Text("Художник: ", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                    Text(selectedItem!!.artist, fontSize = 13.sp)
                                }
                            }
                            if (selectedItem!!.sculptor.isNotEmpty()) {
                                Row {
                                    Text("Скульптор: ", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                    Text(selectedItem!!.sculptor, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    if (selectedItem!!.websiteUrl.isNotEmpty()) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(selectedItem!!.websiteUrl))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                        ) {
                            Icon(Icons.Outlined.Info, contentDescription = null)
                            Text("Переглянути на сайті НБУ", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = {
                    showDetailDialog = false
                    selectedItem = null
                }) {
                    Text("Закрити")
                }
            }
        )
    }
}

@Composable
fun CatalogItemCard(
    item: CatalogItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(IOSDesign.CardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = IOSDesign.CardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.imageUrlFront)
                    .crossfade(true)
                    .build(),
                contentDescription = "Фото",
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(IOSDesign.ChipCornerRadius))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )
                if (item.series.isNotEmpty()) {
                    Text(
                        text = item.series,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Row {
                    if (item.denomination.isNotEmpty()) {
                        Text(
                            text = "${item.denomination} ₴",
                            fontSize = 12.sp,
                            color = AccentOrange
                        )
                    }
                    if (item.dateIntroduced.isNotEmpty()) {
                        Text(
                            text = " • ${item.dateIntroduced}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
