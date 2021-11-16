package com.citadelapi.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun Product(product: String, onChange: (env: String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val icon = if (expanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown

    Column() {
        OutlinedTextField(
            value = when (product) {
                "income" -> "Income"
                "employment" -> "Employment"
                "dds" -> "Employment"
                else -> "income"
            },
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    expanded = !expanded
                },
            label = { Text("Product type") },
            trailingIcon = {
                Icon(icon, "contentDescription",
                    Modifier.clickable { expanded = !expanded })
            }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(onClick = { onChange("income"); expanded = false; }) {
                Text("Income")
            }
            DropdownMenuItem(onClick = { onChange("employment"); expanded = false; }) {
                Text("Employment")
            }
            DropdownMenuItem(onClick = { onChange("admin"); expanded = false; }) {
                Text("Admin")
            }
        }
    }

}