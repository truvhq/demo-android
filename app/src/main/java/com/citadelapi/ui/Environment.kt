package com.citadelapi.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun Environment(env: String, onEnvChange: (env: String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val icon = if (expanded)
        Icons.Filled.ArrowDropUp
    else
        Icons.Filled.ArrowDropDown

    Column() {
        OutlinedTextField(
            value = when (env) {
                "dev" -> "Development"
                "prod" -> "Production"
                else -> "Sandbox"
            },
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    expanded = !expanded
                },
            label = { Text("Environment") },
            trailingIcon = {
                Icon(icon, "contentDescription",
                    Modifier.clickable { expanded = !expanded })
            }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(onClick = { onEnvChange("sandbox"); expanded = false; }) {
                Text("Sandbox")
            }
            DropdownMenuItem(onClick = { onEnvChange("dev"); expanded = false; }) {
                Text("Development")
            }
            DropdownMenuItem(onClick = { onEnvChange("prod"); expanded = false; }) {
                Text("Production")
            }
        }
    }

}