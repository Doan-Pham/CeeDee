package com.haidoan.android.ceedee.ui.disk_screen.disk_details

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import coil.load
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.DiskTitle

import com.haidoan.android.ceedee.databinding.FragmentDiskDetailsBinding

class DiskDetailsFragment : Fragment() {

    private var _binding: FragmentDiskDetailsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDiskDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val diskTitle = arguments?.getSerializable("disk_title") as DiskTitle
        val amount = arguments?.getLong("amount_disk_title") as Long
        val genreName = arguments?.getString("genre_name") as String
        if (arguments!=null){
            bindImage(binding.imgDiskDetailsCoverImg, diskTitle.coverImageUrl)
            binding.tvDiskDetailsTitle.text = diskTitle.name
            binding.tvDiskDetailsAuthor.text=diskTitle.author
            binding.tvDiskDetailsGenreName.text = genreName
            binding.tvDiskDetailsAmountName.text= amount.toString() + " CD"
            binding.tvDiskDetailsDescriptionName.text=diskTitle.description
        }

    }

    private fun bindImage(imgView: ImageView, imgUrl: String?) {
        imgUrl?.let {
            val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
            imgView.load(imgUri) {
                placeholder(R.drawable.ic_launcher)
                error(R.drawable.ic_app_logo)
            }
        }
    }
}