package com.citadelapi.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize

data class DropdownData(val value: String, val label: String) {}

@Composable
fun Dropdown(
    value: String,
    onChange: (value: String) -> Unit,
    options: Array<DropdownData>,
    label: String
) {
    var expanded by remember { mutableStateOf(false) }
    var textfieldSize by remember { mutableStateOf(Size.Zero) }
    val icon = if (expanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown
    var selectedLabel by remember {
        mutableStateOf(options.find { it.value == value }?.label)
    }

    Column() {
        Box() {
            OutlinedTextField(
                value = selectedLabel ?: "",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        textfieldSize = coordinates.size.toSize()
                    },
                label = { Text(label) },
                trailingIcon = {
                    Icon(icon, "",
                        Modifier.clickable { expanded = !expanded })
                }
            )
            Box(modifier = Modifier
                .matchParentSize()
                .clickable {
                    expanded = !expanded
                })
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(with(LocalDensity.current) { textfieldSize.width.toDp() }),
        ) {
            options.map {
                DropdownMenuItem(onClick = { selectedLabel = it.label; onChange(it.value); expanded = false; }) {
                    Text(it.label)
                }
            }
        }
    }

}