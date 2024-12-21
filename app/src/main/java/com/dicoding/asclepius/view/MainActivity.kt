package com.dicoding.asclepius.view

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var currentImageUri: Uri? = null

    private  var galleryResult = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ){ uri ->
        if (uri != null){
            currentImageUri = uri
            showImage()
        } else Log.d("Photo Picker", "No media selected")
    }

    private val imageClassifierHelper by lazy {
        ImageClassifierHelper(context = this, onError = {
            showToast(it)
            setLoading(false)
        }, onResult = { results, inferenceTime ->
            setLoading(false)
            if (!results.isNullOrEmpty()){
                val result = results.first()
                val data = result.categories.first()
                moveToResult(data.label, data.score, inferenceTime)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.galleryButton.setOnClickListener{
            startGallery()
        }
        binding.analyzeButton.setOnClickListener{
            analyzeImage()
        }
    }

//    private val launcherGallery = registerForActivityResult(
//        ActivityResultContracts.PickVisualMedia()
//    ) { uri: Uri? ->
//        // TODO: Mendapatkan gambar dari Gallery.
//        if (
//            uri != null
//        ){
//            currentImageUri = uri
//            showImage()
//        } else{
//            Toast.makeText(this, R.string.image_error, Toast.LENGTH_SHORT).show()
//        }
//
//    }

//    private fun initAction(){
//        binding?.apply {
//            galleryButton.setOnClickListener {startGallery()}
//            analyzeButton.setOnClickListener{moveToResult()}
//        }
//    }

    private fun startGallery() {
        // TODO: Mendapatkan gambar dari Gallery.
        galleryResult.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun showImage() {
        // TODO: Menampilkan gambar sesuai Gallery yang dipilih.
        currentImageUri?.let { uri ->
            binding.previewImageView.setImageURI(uri)
        } ?: run{
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun analyzeImage() {
        // TODO: Menganalisa gambar yang berhasil ditampilkan.
        if (currentImageUri != null){
            setLoading(true)
            lifecycleScope.launch(Dispatchers.Default){
                imageClassifierHelper.classifyStaticImage(currentImageUri!!)
            }
        } else {
            showToast(getString(R.string.image_error))
        }
    }

    private fun moveToResult(result: String, confidenceScore: Float, inferenceTime: Long) {
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra(ResultActivity.RESULT, result)
            putExtra(ResultActivity.CONFIDENCE_SCORE, confidenceScore)
            putExtra(ResultActivity.INFERENCE_TIME, inferenceTime)
            putExtra(ResultActivity.IMAGE_URI, currentImageUri.toString())
        }
        startActivity(intent)
    }

    private fun setLoading(value: Boolean){
        runOnUiThread{
            binding.apply {
                if (value){
                    progressIndicator.visibility = View.VISIBLE
                    analyzeButton.visibility = View.INVISIBLE
                } else {
                    progressIndicator.visibility = View.INVISIBLE
                    analyzeButton.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}
