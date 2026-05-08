package com.numisproerp.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.numisproerp.NumisProERPApplication
import com.numisproerp.data.repository.Repository
import com.numisproerp.ui.theme.AccentBlue
import com.numisproerp.ui.theme.AccentOrange
import com.numisproerp.ui.theme.AccentRed
import com.numisproerp.ui.theme.IOSDesign
import com.numisproerp.ui.theme.IOSIconChip
import com.numisproerp.ui.viewmodel.ExpensesViewModel
import com.numisproerp.ui.viewmodel.ExpensesViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(navController: NavHostController) {
    val viewModel: ExpensesViewModel = viewModel(
        factory = ExpensesViewModelFactory(
            Repository(NumisProERPApplication.getInstance().database)
        )
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadExpenses()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Назад",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        FloatingActionButton(
            onClick = { viewModel.toggleAddDialog(true) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = AccentBlue
        ) {
            Icon(Icons.Default.Add, contentDescription = "Додати витрату")
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 72.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            val totalExpenses = uiState.expenses.sumOf { it.amount }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = AccentRed.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(IOSDesign.CardCornerRadius)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Загальні витрати",
                        fontSize = 16.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    )
                    Text(
                        text = "${String.format("%,.2f", totalExpenses)} ₴",
                        fontSize = 20.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = AccentRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.expenses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Немає витрат.\nНатисніть + щоб додати",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.expenses) { expense ->
                        ExpenseCard(
                            expense = expense,
                            formatDate = { viewModel.formatDate(it) }
                        )
                    }
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleAddDialog(false) },
            title = { Text("Додати витрату") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var category by remember { mutableStateOf("") }
                    var amount by remember { mutableStateOf("") }
                    var comment by remember { mutableStateOf("") }

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Категорія *") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Оренда, Реклама, Доставка...") },
                        shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                    )

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Сума *") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("₴") },
                        shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                    )

                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Коментар") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Опис витрати") },
                        shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                    )

                    TextButton(
                        onClick = {
                            if (category.isNotBlank() && amount.isNotBlank()) {
                                viewModel.addExpense(category, amount, comment)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Зберегти")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.toggleAddDialog(false) }) {
                    Text("Скасувати")
                }
            }
        )
    }
}

@Composable
fun ExpenseCard(
    expense: com.numisproerp.data.entities.OtherExpense,
    formatDate: (Long) -> String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            IOSIconChip(
                icon = Icons.Outlined.Category,
                tint = AccentOrange
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = expense.category,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    fontSize = 14.sp
                )
                Text(
                    text = formatDate(expense.date),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                if (expense.comment.isNotEmpty()) {
                    Text(
                        text = expense.comment,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            Text(
                text = "-${String.format("%,.2f", expense.amount)} ₴",
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = AccentRed
            )
        }
    }
}
