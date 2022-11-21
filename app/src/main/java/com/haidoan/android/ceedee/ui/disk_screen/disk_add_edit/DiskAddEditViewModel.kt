package com.haidoan.android.ceedee.ui.disk_screen.disk_add_edit

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.liveData
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskTitlesRepository
import com.haidoan.android.ceedee.ui.disk_screen.repository.GenreRepository
import kotlinx.coroutines.Dispatchers

class DiskAddEditViewModel(application: Application) : AndroidViewModel(application) {
    private val genreRepository: GenreRepository
    private val diskTitlesRepository: DiskTitlesRepository

    init {
        genreRepository = GenreRepository(application)
        diskTitlesRepository = DiskTitlesRepository(application)
    }

    fun addDiskTitle(
        author: String,
        coverImageUrl: String,
        description: String,
        genreId: String,
        name: String
    ) = liveData(Dispatchers.IO) {
        diskTitlesRepository.addDiskTitleToFireStore(
            author,
            coverImageUrl,
            description,
            genreId,
            name
        ).collect { response ->
            emit(response)
        }
    }

    fun updateDiskTitle(
        id: String,
        author: String,
        coverImageUrl: String,
        description: String,
        genreId: String,
        name: String
    ) = liveData(Dispatchers.IO) {
        diskTitlesRepository.updateDiskTitleToFireStore(
            id,
            author,
            coverImageUrl,
            description,
            genreId,
            name
        ).collect { response ->
            emit(response)
        }
    }

    fun getGenres() = liveData(Dispatchers.IO) {
        genreRepository.getGenresFromFireStore().collect { response ->
            emit(response)
        }
    }

    fun addImage(filePath: Uri?, name: String?) = liveData(Dispatchers.IO) {
        diskTitlesRepository.addImageToFireStore(filePath,name).collect { response ->
            emit(response)
        }
    }
}