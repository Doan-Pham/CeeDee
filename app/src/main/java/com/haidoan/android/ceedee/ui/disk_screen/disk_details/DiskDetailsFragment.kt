package com.haidoan.android.ceedee.ui.disk_screen.disk_details

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import coil.load
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.databinding.FragmentDiskDetailsBinding
import java.io.Serializable

class DiskDetailsFragment : Fragment() {

    private var _binding: FragmentDiskDetailsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @Suppress("DEPRECATION")
    inline fun <reified T : Serializable> Bundle.customGetSerializable(key: String): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getSerializable(key, T::class.java)
        } else {
            getSerializable(key) as? T
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDiskDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val diskTitle = arguments?.customGetSerializable<DiskTitle>("disk_title") as DiskTitle
        val amount = arguments?.getLong("amount_disk_title") as Long
        val genreName = arguments?.getString("genre_name") as String
        if (arguments!=null){
            bindImage(binding.imgDiskDetailsCoverImg, diskTitle.coverImageUrl)
            binding.tvDiskDetailsTitle.text = diskTitle.name
            binding.tvDiskDetailsAuthor.text=diskTitle.author
            binding.tvDiskDetailsGenreName.text = genreName
            binding.tvDiskDetailsAmountName.text= "$amount CD"
            binding.tvDiskDetailsDescriptionName.text=diskTitle.description
            binding.progressBar.visibility = View.GONE
        }

    }

    private fun bindImage(imgView: ImageView, imgUrl: String?) {
        imgUrl?.let {
            val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
            imgView.load(imgUri) {
                crossfade(true)
                placeholder(R.drawable.ic_disk_cover_placeholder_96)
                error(R.drawable.ic_disk_cover_placeholder_96)
            }
        }
    }
}