package com.haidoan.android.ceedee.ui.customer_related.disk

import android.util.Log
import androidx.lifecycle.*
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.data.Genre
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskTitlesRepository
import com.haidoan.android.ceedee.ui.disk_screen.repository.GenreRepository
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import kotlinx.coroutines.Dispatchers

private const val TAG = "CustomerDiskViewModel"

class CustomerDiskViewModel(
    private val diskTitlesRepository: DiskTitlesRepository,
    private val genreRepository: GenreRepository
) : ViewModel() {

    private val _genres = liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
        genreRepository.getGenresFromFireStore().collect {
            if (it is Response.Success) emit(it.data)
            if (it is Response.Failure) {
                Log.e(
                    TAG,
                    " genreRepository.getGenresFromFireStore() - An error has occurred: ${it.errorMessage}"
                )
            }
        }
    }
    val genres: LiveData<List<Genre>>
        get() = _genres


    private val _currentFilteringGenreId = MutableLiveData("")
    fun setFilteringGenreId(genreId: String = "") {
        _currentFilteringGenreId.value = genreId
        Log.d(
            TAG,
            "setFilteringGenreId() - _currentFilteringGenreId.value: ${_currentFilteringGenreId.value}"
        )
    }


    private val _searchQuery = MutableLiveData("")
    fun searchDiskTitle(query: String?) {
        _searchQuery.value = query
    }

    private val _popularDiskTitles = MutableLiveData(listOf<DiskTitle>())
    val popularDiskTitles: LiveData<List<DiskTitle>>
        get() = _popularDiskTitles

    private val diskTitleModifications =
        MediatorLiveData<Pair<String?, String?>>().apply {
            addSource(_currentFilteringGenreId) { value = Pair(it, _searchQuery.value) }
            addSource(_searchQuery) { value = Pair(_currentFilteringGenreId.value, it) }
        }

    val diskTitles = diskTitleModifications.switchMap { diskTitleModifications ->
        liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            diskTitlesRepository.getDiskTitlesAvailableInStore()
                .collect { diskTitles ->
                    _popularDiskTitles.postValue(diskTitles.sortedByDescending { it.totalRentalAmount })
                    Log.d(
                        TAG,
                        "diskTitlesRepository.getDiskTitlesAvailableInStore() - diskTitles.sortedByDescending { it.totalRentalAmount }: ${diskTitles.sortedByDescending { it.totalRentalAmount }}"
                    )
                    emit(
                        diskTitles.filterByGenreId(diskTitleModifications.first ?: "")
                            .searchByDiskTitle(diskTitleModifications.second ?: "")
                    )
                }
        }
    }


    private fun List<DiskTitle>.filterByGenreId(genreId: String = "") =
        if (genreId.isEmpty()) this
        else this.filter { it.genreId == genreId }

    private fun List<DiskTitle>.searchByDiskTitle(query: String) =
        this.filter { diskTitle ->
            diskTitle.name.lowercase().contains(query.lowercase())
        }

    class Factory(
        private val diskTitlesRepository: DiskTitlesRepository,
        private val genreRepository: GenreRepository
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CustomerDiskViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CustomerDiskViewModel(diskTitlesRepository, genreRepository) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}