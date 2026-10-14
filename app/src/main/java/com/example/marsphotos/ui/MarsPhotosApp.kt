package com.example.marsphotos.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marsphotos.ui.screens.*

@Composable
fun MarsPhotosApp() {
    var selectedTab by rememberSaveable { mutableIntStateOf(1) }
    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Marte") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Rick and Morty") })
            }
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                if (selectedTab == 0) {
                    val model: MarsViewModel = viewModel()
                    HomeScreen(model.marsUiState, model::getMarsPhotos)
                } else {
                    val model: ExplorerViewModel = viewModel()
                    ExplorerScreen(model)
                }
            }
        }
    }
}
