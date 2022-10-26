package com.haidoan.android.ceedee.ui.disk_screen.disk_titles

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.liveData
import com.haidoan.android.ceedee.ui.disk_screen.disks.DisksRepository
import com.haidoan.android.ceedee.utils.TypeUtils
import kotlinx.coroutines.Dispatchers

class DiskTitlesViewModel(application: Application) : AndroidViewModel(application) {
    private val diskTitlesRepository : DiskTitlesRepository
    private val disksRepository: DisksRepository
    private val genreRepository: GenreRepository

    init {
        diskTitlesRepository = DiskTitlesRepository(application)
        disksRepository = DisksRepository(application)
        genreRepository = GenreRepository(application)
    }

    fun getDiskAmountInDiskTitles(diskTitleId : String) = liveData(Dispatchers.IO) {
        disksRepository.getDiskAmountInDiskTitlesFromFireStore(diskTitleId).collect { response ->
            emit(response)
        }
    }

    fun getDiskTitlesSortByName(type: TypeUtils.SORT_BY_NAME) = liveData(Dispatchers.IO) {
        diskTitlesRepository.getDiskTitlesSortByNameFromFireStore(type).collect { response ->
            emit(response)
        }
    }

    fun getGenreById(id: String) = liveData(Dispatchers.IO) {
        genreRepository.getGenreByIdFireStore(id).collect { response ->
            emit(response)
        }
    }

    fun getGenres() = liveData(Dispatchers.IO) {
        genreRepository.getGenresFromFireStore().collect { response ->
            emit(response)
        }
    }

    fun getDiskTitles() = liveData(Dispatchers.IO) {
        diskTitlesRepository.getDiskTitlesFromFireStore().collect { response ->
            emit(response)
        }
    }
}