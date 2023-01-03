package com.haidoan.android.ceedee.ui.disk_screen.disk_requisition

import androidx.lifecycle.*
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskTitlesRepository
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import kotlinx.coroutines.Dispatchers

class DisksToImportViewModel(private val diskTitlesRepository: DiskTitlesRepository) :
    ViewModel() {

    private val searchQuery = MutableLiveData("")

    private val _diskTitlesInStore = searchQuery.switchMap { currentSearchQuery ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            diskTitlesRepository.getDiskTitlesFromFireStore()
                .collect {
                    if (it is Response.Success) {
                        emit(it.data.filter { diskTitle ->
                            diskTitle.name.contains(
                                currentSearchQuery.toString(),
                                true
                            )
                        })
                    }

                }
        }
    }


    val diskTitlesInStore: LiveData<List<DiskTitle>>
        get() = _diskTitlesInStore


    fun searchDiskTitle(query: String?) {
        searchQuery.value = query ?: ""
    }

    class Factory(
        private val diskTitlesRepository: DiskTitlesRepository
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DisksToImportViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DisksToImportViewModel(
                    diskTitlesRepository
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}