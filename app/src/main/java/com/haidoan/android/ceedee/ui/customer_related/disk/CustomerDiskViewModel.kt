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

    val diskTitles = _currentFilteringGenreId.switchMap { currentFilteringGenreId ->
        liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            diskTitlesRepository.getDiskTitlesAvailableInStore()
                .collect { emit(it.filterByGenreId(currentFilteringGenreId)) }
        }
    }


    private fun List<DiskTitle>.filterByGenreId(genreId: String = "") =
        if (genreId.isEmpty()) this
        else this.filter { it.genreId == genreId }

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