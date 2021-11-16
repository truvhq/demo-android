package com.citadelapi.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.citadelapi.product.MainViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@Composable
fun AdditionalSettings(viewModel: MainViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val state = viewModel.productUIState.collectAsState()

    val icon = if (expanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown
    Column() {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .clickable { expanded = !expanded }
                .fillMaxWidth()
                .padding(Dp(8F))
        ) {
            Text(text = "Show additional settings")
            Icon(icon, "")
        }

        if (expanded) {
            OutlinedTextField(
                label = { Text(text = "Company Mapping ID") },
                value = state.value.companyMapping,
                onValueChange = { viewModel.changeCompanyMapping(it) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                label = { Text(text = "Provider ID") },
                value = state.value.provider,
                onValueChange = { viewModel.changeProvider(it) },
                modifier = Modifier.fillMaxWidth()
            )
            if (state.value.productType == "deposit_switch") {
                OutlinedTextField(
                    label = { Text(text = "Routing number") },
                    value = state.value.routingNumber,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}