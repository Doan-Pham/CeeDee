package com.haidoan.android.ceedee.ui.disk_screen.disk_titles

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.liveData
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskTitlesRepository
import com.haidoan.android.ceedee.ui.disk_screen.repository.DisksRepository
import com.haidoan.android.ceedee.ui.disk_screen.repository.GenreRepository
import com.haidoan.android.ceedee.ui.disk_screen.repository.SupplierRepository
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import kotlinx.coroutines.Dispatchers

class DiskTitlesViewModel(application: Application) : AndroidViewModel(application) {
    private val diskTitlesRepository: DiskTitlesRepository
    private val disksRepository: DisksRepository
    private val genreRepository: GenreRepository
    private val supplierRepository: SupplierRepository

    init {
        diskTitlesRepository = DiskTitlesRepository(application)
        disksRepository = DisksRepository(application)
        genreRepository = GenreRepository(application)
        supplierRepository = SupplierRepository(application)
    }

    fun deleteDiskTitle(diskTitleId: String) = liveData(Dispatchers.IO) {
        diskTitlesRepository.deleteDiskTitleFromFireStore(diskTitleId).collect { response ->
            emit(response)
        }
    }

    fun getFiveDiskTitlesWithTotalRentalAmountDescending() = liveData(Dispatchers.IO) {
        diskTitlesRepository.getFiveDiskTitlesWithTotalRentalAmountDescendingFromFireStore()
            .collect { response ->
                emit(response)
            }
    }

    fun getDiskTitleFilterByGenreId(id: String) = liveData(Dispatchers.IO) {
        emit(Response.Loading())
        diskTitlesRepository.getDiskTitleFilterByGenreIdFromFireStore(id).collect { response ->
            emit(response)
        }
    }

    fun addGenres(genreName: String) = liveData(Dispatchers.IO) {
        genreRepository.addGenreToFireStore(genreName).collect { response ->
            emit(response)
        }
    }

    fun addSupplier(supplier: HashMap<String, String>) = liveData(Dispatchers.IO) {
        supplierRepository.addSupplierToFireStore(supplier).collect { response ->
            emit(response)
        }
    }

    fun getGenres() = liveData(Dispatchers.IO) {
        genreRepository.getGenresFromFireStore().collect { response ->
            emit(response)
        }
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

    fun addImage(filePath: Uri?, name: String?) = liveData(Dispatchers.IO) {
        diskTitlesRepository.addImageToFireStore(filePath, name).collect { response ->
            emit(response)
        }
    }

    fun getDiskTitles() = liveData(Dispatchers.IO) {
        emit(Response.Loading())
        diskTitlesRepository.getDiskTitlesFromFireStore().collect { response ->
            emit(response)
        }
    }
}