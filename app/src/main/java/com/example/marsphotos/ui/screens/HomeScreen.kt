package com.example.marsphotos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.marsphotos.R

@Composable
fun Photo(url: String, description: String) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(url).crossfade(true).build(),
        contentDescription = description,
        placeholder = painterResource(R.drawable.loading_img),
        error = painterResource(R.drawable.ic_broken_image),
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxWidth().height(280.dp)
    )
}

@Composable
fun HomeScreen(marsUiState: MarsUiState, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (marsUiState) {
            MarsUiState.Loading -> CircularProgressIndicator()
            MarsUiState.Error -> {
                Text("Não foi possível obter as fotografias. Verifica a internet.")
                Button(onClick = onRetry) { Text("Tentar novamente") }
            }
            is MarsUiState.Success -> {
                Text(marsUiState.photos)
                Photo(marsUiState.randomPhoto.imgSrc, "Fotografia da superfície de Marte")
                Button(onClick = onRetry) { Text("Outra fotografia") }
            }
        }
    }
}

@Composable
fun ExplorerScreen(viewModel: ExplorerViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Rick and Morty Explorer", style = MaterialTheme.typography.titleLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Category.entries.forEach { category ->
                FilterChip(selected = viewModel.category == category,
                    onClick = { viewModel.selectCategory(category) },
                    label = { Text(category.name) })
            }
        }
        OutlinedTextField(value = viewModel.name, onValueChange = viewModel::updateName,
            label = { Text("Filtrar por nome") },
            isError = viewModel.nameError != null,
            supportingText = { viewModel.nameError?.let { Text(it) } },
            singleLine = true, modifier = Modifier.fillMaxWidth())
        Button(onClick = viewModel::search) { Text("Pesquisar") }
        when (val state = viewModel.uiState) {
            ExplorerUiState.Loading -> CircularProgressIndicator()
            ExplorerUiState.Empty -> Text("Nenhum resultado. Experimenta outro nome ou limpa o filtro.")
            is ExplorerUiState.Error -> {
                Text(state.message)
                Button(onClick = viewModel::retry) { Text("Tentar novamente") }
            }
            is ExplorerUiState.Success -> {
                Text("Itens recebidos: ${state.received} / ${state.total} encontrados")
                Text("Imagens disponíveis nesta resposta: ${state.images}")
                Text("O sorteio usa a primeira página de resultados (até 20 itens).",
                    style = MaterialTheme.typography.bodySmall)
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(state.item.name, style = MaterialTheme.typography.titleLarge)
                        Text(state.item.field1)
                        Text(state.item.field2)
                        state.item.image?.let { Photo(it, state.item.caption.ifEmpty { state.item.name }) }
                            ?: Text("Sem imagem disponível para este item.")
                        if (state.item.caption.isNotEmpty()) Text(state.item.caption)
                    }
                }
                Button(onClick = viewModel::roll, enabled = state.received > 1) { Text("Roll") }
                if (state.received == 1) Text("Só existe um item nesta resposta.")
            }
        }
    }
}
