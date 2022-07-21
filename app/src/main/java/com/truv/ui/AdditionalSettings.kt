package com.truv.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truv.R
import com.truv.MainViewModel
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
        val style = TextStyle(fontSize = 12.sp, color = colorResource(R.color.grey50))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .clickable { expanded = !expanded }
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(text = "Show additional settings")
            Icon(icon, "")
        }

        if (expanded) {
            OutlinedTextField(
                label = { Text(text = "Company Mapping ID") },
                value = state.value.companyMapping ?: "",
                onValueChange = { viewModel.changeCompanyMapping(if (it == "") null else it) },
                modifier = Modifier.fillMaxWidth()
            )
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Use the company mapping ID to skip the employer search step. For example, use IDs below:",
                    style = style
                )
                Row {
                    Text("Facebook ", style = style)
                    Text("539aad839b51435aa8e525fed95f1688", modifier = Modifier.clickable {
                        viewModel.changeCompanyMapping("539aad839b51435aa8e525fed95f1688")
                    }, color = colorResource(R.color.accent), style = style)
                }
                Row {
                    Text("Kroger ", style = style)
                    Text("3f45aed287064cbc91d28eff0424a72a", modifier = Modifier.clickable {
                        viewModel.changeCompanyMapping("3f45aed287064cbc91d28eff0424a72a")
                    }, color = colorResource(R.color.accent), style = style)
                }
                Row {
                    Text("Fannie Mae ", style = style)
                    Text("4af9336b89294bc98879b1e38e6c72df", modifier = Modifier.clickable {
                        viewModel.changeCompanyMapping("4af9336b89294bc98879b1e38e6c72df")
                    }, color = colorResource(R.color.accent), style = style)
                }
            }
            OutlinedTextField(
                label = { Text(text = "Provider ID") },
                value = state.value.provider ?: "",
                onValueChange = { viewModel.changeProvider(if (it == "") null else it) },
                modifier = Modifier.fillMaxWidth()
            )
            Column(Modifier.padding(16.dp)) {
                ClickableText(
                    buildAnnotatedString {
                        append("Use the provider ID to skip selecting a payroll provider. For example, use ")
                        pushStringAnnotation(
                            tag = "provider",
                            annotation = "adp"
                        )
                        withStyle(style = SpanStyle(color = colorResource(R.color.accent))) {
                            append("adp")
                        }
                        pop()
                        append(".")
                    },
                    style = style,
                    onClick = {
                        viewModel.changeProvider("adp")
                    }
                )
            }
            if (setOf("deposit_switch", "pll").contains(state.value.productType)) {
                OutlinedTextField(
                    label = { Text(text = "Routing number") },
                    value = state.value.accountState.routingNumber,
                    onValueChange = {
                        viewModel.changeAccountState(
                            state.value.accountState.copy(
                                routingNumber = it
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    label = { Text(text = "Account number") },
                    value = state.value.accountState.accountNumber,
                    onValueChange = {
                        viewModel.changeAccountState(
                            state.value.accountState.copy(
                                accountNumber = it
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    label = { Text(text = "Bank name") },
                    value = state.value.accountState.bankName,
                    onValueChange = {
                        viewModel.changeAccountState(
                            state.value.accountState.copy(
                                bankName = it
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Dropdown(
                    value = state.value.accountState.accountType,
                    onChange = { viewModel.changeAccountState(
                        state.value.accountState.copy(
                            accountType = it
                        )
                    ) },
                    options = arrayOf(
                        DropdownData("checking", "Checking"),
                        DropdownData("savings", "Savings"),
                    ),
                    label = "Account type"
                )

            }
        }
    }
}