package com.haidoan.android.ceedee.ui.disk_screen.repository

import android.app.Application
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.navigation.findNavController
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.taskState

import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import com.haidoan.android.ceedee.ui.disk_screen.utils.TypeUtils

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

import kotlinx.coroutines.tasks.await
import java.util.*

class DiskTitlesRepository(private val application: Application) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var queryDiskTitle: CollectionReference = db.collection("DiskTitle")
    private val storageReference = FirebaseStorage.getInstance().reference

    init {

    }

    fun getDiskTitleFilterByGenreIdFromFireStore(id: String) = flow {
        emit(Response.Loading())
        emit(
            Response.Success(queryDiskTitle
                .whereEqualTo("genreId", id)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    Log.d("TAG", "GET POST SUCCESS")
                    doc.toObject(DiskTitle::class.java)
                })
        )
    }.catch { error ->
        error.message?.let { errorMessage ->
            emit(Response.Failure(errorMessage))
        }
    }

    fun getDiskTitlesSortByNameFromFireStore(type: TypeUtils.SORT_BY_NAME) = flow {
        emit(Response.Loading())
        emit(
            Response.Success(queryDiskTitle
                .orderBy(
                    "name", when (type) {
                        TypeUtils.SORT_BY_NAME.Ascending -> {
                            Query.Direction.ASCENDING
                        }
                        TypeUtils.SORT_BY_NAME.Descending -> {
                            Query.Direction.DESCENDING
                        }
                    }
                )
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    Log.d("TAG", "GET POST SUCCESS")
                    doc.toObject(DiskTitle::class.java)
                })
        )
    }.catch { error ->
        error.message?.let { errorMessage ->
            emit(Response.Failure(errorMessage))
        }
    }

    fun getDiskTitlesFromFireStore() = flow {
        emit(Response.Loading())
        emit(Response.Success(queryDiskTitle.get().await().documents.mapNotNull { doc ->
            Log.d("TAG", "GET POST SUCCESS")
            doc.toObject(DiskTitle::class.java)
        }))
    }.catch { error ->
        error.message?.let { errorMessage ->
            emit(Response.Failure(errorMessage))
        }
    }

    fun addDiskTitleToFireStore(
        author: String,
        coverImageUrl: String,
        description: String,
        genreId: String,
        name: String
    ) = flow {
        val hash = hashMapOf(
            "author" to author,
            "coverImageUrl" to coverImageUrl,
            "description" to description,
            "genreId" to genreId,
            "name" to name
        )
        emit(Response.Loading())
        emit(
            Response.Success(
                queryDiskTitle.add(hash).await()
            )
        )
    }.catch { error ->
        error.message?.let { errorMessage ->
            emit(Response.Failure(errorMessage))
        }
    }

    fun updateDiskTitleToFireStore(
        id: String,
        author: String,
        coverImageUrl: String,
        description: String,
        genreId: String,
        name: String
    ) = flow {
        val hash = hashMapOf(
            "author" to author,
            "coverImageUrl" to coverImageUrl,
            "description" to description,
            "genreId" to genreId,
            "name" to name
        )

        emit(Response.Loading())
        emit(
            Response.Success(
                queryDiskTitle.document(id).update(
                    "author", author,
                    "coverImageUrl", coverImageUrl,
                    "description", description,
                    "genreId", genreId,
                    "name", name
                ).await()
            )
        )
    }.catch { error ->
        error.message?.let { errorMessage ->
            emit(Response.Failure(errorMessage))
        }
    }

    fun addImageToFireStore(
        filePath: Uri?,
        name: String?
    ) = flow {
        val ref = storageReference
            .child("disk_titles_img/$name")
        emit(Response.Loading())
        emit(
            Response.Success(
                ref
                    .putFile(filePath!!)
                    .continueWithTask { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        return@continueWithTask ref.downloadUrl
                    }
                    .await()
            )
        )
    }.catch { error ->
        error.message?.let { errorMessage ->
            emit(Response.Failure(errorMessage))
        }
    }

}


/* private fun addDiskTitleToFireStore() {
       binding.btnSave.visibility = View.GONE
       binding.progressBarDiskAddEditSave.visibility = View.VISIBLE
       if (filePath != null) {
           val storageReference: StorageReference = FirebaseStorage.getInstance().reference
           val ref = storageReference.child("disk_titles_img/" + UUID.randomUUID().toString())
           val uploadTask = ref.putFile(filePath!!)

           uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
               if (!task.isSuccessful) {
                   task.exception?.let {
                       throw it
                   }
               }
               return@Continuation ref.downloadUrl
           }).addOnCompleteListener { task ->
               if (task.isSuccessful) {
                   val downloadUri = task.result
                   coverImgUrl = downloadUri.toString()

                   val author = binding.edtDiskAddEditAuthor.text.toString()
                   val description = binding.edtDiskAddEditDescription.text.toString()
                   val name = binding.edtDiskAddEditDiskTitleName.text.toString()
                   diskAddEditViewModel.addDiskTitle(
                       author,
                       coverImgUrl,
                       description,
                       genreId,
                       name
                   )
                       .observe(viewLifecycleOwner) { response ->
                           when (response) {
                               is Response.Loading -> {
                               }
                               is Response.Success -> {
                                   Toast.makeText(
                                       requireActivity(),
                                       "Add disk title success!!!",
                                       Toast.LENGTH_SHORT
                                   ).show()
                                   view?.findNavController()?.popBackStack()
                                   binding.btnSave.visibility = View.VISIBLE
                                   binding.progressBarDiskAddEditSave.visibility = View.GONE
                               }
                               is Response.Failure -> {
                                   Toast.makeText(
                                       requireActivity(),
                                       "Fail to disk title!!!",
                                       Toast.LENGTH_SHORT
                                   ).show()

                                   binding.btnSave.visibility = View.VISIBLE
                                   binding.progressBarDiskAddEditSave.visibility = View.GONE
                               }
                               else -> print(response.toString())
                           }
                       }
               } else {
                   // Handle failures
                   binding.btnSave.visibility = View.VISIBLE
                   binding.progressBarDiskAddEditSave.visibility = View.GONE
               }
           }.addOnFailureListener {
               Log.d("TAG_REF", it.message.toString())
               binding.btnSave.visibility = View.VISIBLE
               binding.progressBarDiskAddEditSave.visibility = View.GONE
           }
       }
   }
*/