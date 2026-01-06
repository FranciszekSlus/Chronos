
package com.tasker.chronos.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.navigation.NavController
import com.tasker.chronos.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChronosTopAppBar(title: String, navController: NavController) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontFamily = FontFamily(Font(R.font.greekfreak)),
                color = Color.White
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.navigate("menu") }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Wróć do menu",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFFFFD700),
        )
    )
}