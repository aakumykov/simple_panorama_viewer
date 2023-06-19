package com.github.aakumykov.simple_panorama_viewer

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.github.aakumykov.simple_panorama_viewer.databinding.FragmentPanoramaGvrBinding
import com.github.aakumykov.simple_panorama_viewer.panorama_fragment.BundleReader
import com.google.vr.sdk.widgets.pano.VrPanoramaView
import java.io.FileDescriptor

@SuppressLint("LogNotTimber")
class PanoramaFragmentGVR: Fragment() {

    private val TAG: String = javaClass.name
    private var _binding: FragmentPanoramaGvrBinding? = null
    private val binding get() = _binding!!

    companion object {

        private val FILE_URI_STRING = "FILE_URI_STRING"

        fun onTouchEvent(event: MotionEvent): Boolean {
            TODO("Not yet implemented")
        }

        fun create(fileURI: Uri?): PanoramaFragmentGVR {
            val panoramaFragment = PanoramaFragmentGVR()
            fileURI?.let {
                val bundle = bundleOf(Pair(FILE_URI_STRING, fileURI.toString()))
                panoramaFragment.arguments = bundle
            }
            return panoramaFragment
        }

        fun openImageIntent(): Intent {
            val intent = Intent()
            intent.action = Intent.ACTION_OPEN_DOCUMENT
            intent.type = "image/*"
            return intent
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPanoramaGvrBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        processInputData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun processInputData() {

        val fileUri: Uri? = BundleReader.getUri(arguments, FILE_URI_STRING)
        if (null ==fileUri) {
            Log.e(TAG, "File uri is null")
            return }

        val parcelFileDescriptor: ParcelFileDescriptor? = context?.contentResolver?.openFileDescriptor(fileUri, "r")
        val fileDescriptor: FileDescriptor? = parcelFileDescriptor?.fileDescriptor

        val bitmap =  BitmapFactory.decodeFileDescriptor(fileDescriptor)
        val options: VrPanoramaView.Options = VrPanoramaView.Options()
        binding.vrPanoramaView.loadImageFromBitmap(bitmap, options)

        parcelFileDescriptor?.close()
    }


}