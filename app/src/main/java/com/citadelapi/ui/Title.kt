package com.citadelapi.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp

@Composable
fun Title(title: String) {
    Text(
        title,
        modifier = Modifier.padding(vertical = 12.dp),
        fontSize = 22.sp,
        fontWeight = FontWeight.Medium
    )
}