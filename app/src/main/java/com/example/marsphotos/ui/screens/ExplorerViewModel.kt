package com.example.marsphotos.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marsphotos.network.RickApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.HttpException

enum class Category { Character, Episode, Location }

data class ExplorerItem(val id: Int, val name: String, val field1: String, val field2: String,
    val image: String? = null, val characterUrls: List<String> = emptyList(), val caption: String = "")
sealed interface ExplorerUiState {
    data object Loading : ExplorerUiState
    data object Empty : ExplorerUiState
    data class Error(val message: String) : ExplorerUiState
    data class Success(val item: ExplorerItem, val received: Int, val total: Int, val images: Int) : ExplorerUiState
}
class ExplorerViewModel : ViewModel() {
    var category by mutableStateOf(Category.Character)
        private set
    var name by mutableStateOf("")
        private set
    var nameError: String? by mutableStateOf(null)
        private set
    var uiState: ExplorerUiState by mutableStateOf(ExplorerUiState.Loading)
        private set
    private var items = emptyList<ExplorerItem>()
    private var total = 0
    private var job: Job? = null
    init { loadItems() }
    fun updateName(value: String) {
        name = value
        nameError = null
    }
    fun selectCategory(value: Category) {
        category = value
        nameError = null
        loadItems()
    }
    fun search() {
        if (name.isBlank()) {
            nameError = "Introduz um nome para pesquisar."
            return
        }
        nameError = null
        loadItems()
    }
    fun retry() { loadItems() }
    private fun loadItems() {
        job?.cancel()
        val selectedCategory = category
        val query = name.trim()
        uiState = ExplorerUiState.Loading
        job = viewModelScope.launch {
            try {
                val api = RickApi.service
                when (selectedCategory) {
                    Category.Character -> {
                        val page = api.characters(query)
                        total = page.info.count
                        items = page.results.map { ExplorerItem(it.id, it.name, "Estado: ${it.status}", "Espécie: ${it.species}", image = it.image) }
                    }
                    Category.Episode -> {
                        val page = api.episodes(query)
                        total = page.info.count
                        items = page.results.map { ExplorerItem(it.id, it.name, "Episódio: ${it.episode}", "Emissão: ${it.airDate}", characterUrls = it.characters) }
                    }
                    Category.Location -> {
                        val page = api.locations(query)
                        total = page.info.count
                        items = page.results.map { ExplorerItem(it.id, it.name, "Tipo: ${it.type}", "Dimensão: ${it.dimension}", characterUrls = it.residents) }
                    }
                }
                showRandom()
            } catch (e: CancellationException) { throw e
            } catch (e: HttpException) {
                uiState = if (e.code() == 404) ExplorerUiState.Empty else ExplorerUiState.Error("Erro do servidor. Tenta novamente.")
            } catch (e: Exception) { uiState = ExplorerUiState.Error("Não foi possível carregar. Verifica a internet.") }
        }
    }
    fun roll() {
        val previous = (uiState as? ExplorerUiState.Success)?.item?.id ?: return
        uiState = ExplorerUiState.Loading
        job = viewModelScope.launch { showRandom(previous) }
    }
    private suspend fun showRandom(previous: Int? = null) {
        if (items.isEmpty()) { uiState = ExplorerUiState.Empty; return }
        val alternatives = items.filter { it.id != previous }
        var item = alternatives.ifEmpty { items }.random()

        if (item.image == null && item.characterUrls.isNotEmpty()) {
            try {
                val character = RickApi.service.character(item.characterUrls.random())
                item = item.copy(image = character.image, caption = "Personagem associada: ${character.name}")
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) { item = item.copy(caption = "Não foi possível carregar a imagem associada.") }
        }
        uiState = ExplorerUiState.Success(item, items.size, total,
            if (category == Category.Character) items.count { it.image != null } else if (item.image != null) 1 else 0)
    }
}
