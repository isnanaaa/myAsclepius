package com.dicoding.asclepius.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.net.toUri
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_result)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri = intent.getStringExtra(IMAGE_URI) ?: ""
        var confidenceScore = intent.getFloatExtra(CONFIDENCE_SCORE, 0.0f)
        val result = intent.getStringExtra(RESULT) ?: ""
        val inferenceTime = intent.getLongExtra(INFERENCE_TIME, 0L)

        // TODO: Menampilkan hasil gambar, prediksi, dan confidence score.

        with(binding){
            confidenceScore *= 100
            Log.i("Inference Time", inferenceTime.toString().plus(" ms"))

            resultImage.setImageURI(imageUri.toUri())
            resultText.text = resources.getString(R.string.result, result)
        }
    }

    companion object{
        const val IMAGE_URI = "image_uri"
        const val CONFIDENCE_SCORE = "confidence_score"
        const val RESULT = "result"
        const val INFERENCE_TIME = "inference_time"
    }
}
