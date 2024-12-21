package com.dicoding.asclepius.helper

import com.dicoding.asclepius.R
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier

class ImageClassifierHelper(
    val context: Context,
    val onError: (error: String) -> Unit,
    val onResult: (result: List<Classifications>?, inferenceTime: Long) -> Unit,
    private val maxResult: Int = 1,
    private val thresholds: Float = 0.51f,
    private val modelName: String = "cancer_classification.tflite"
) {

    private var imageClassifier: ImageClassifier? = null

    init {
        setupImageClassifier()
    }

    private fun setupImageClassifier() {
        // TODO: Menyiapkan Image Classifier untuk memproses gambar.
        val optionBuilder = ImageClassifier.ImageClassifierOptions.builder()
            .setScoreThreshold(thresholds)
            .setMaxResults(maxResult)

        val baseOptionsBuilder = BaseOptions.builder()
            .setNumThreads(4)
        optionBuilder.setBaseOptions(baseOptionsBuilder.build())

        try {
            imageClassifier = ImageClassifier.createFromFileAndOptions(context, modelName, optionBuilder.build())
        } catch (e:Exception){
            onError.invoke(context.getString(R.string.image_error))
            Log.e("Classification", e.message.toString())
//            classifierListener?.onError(e.toString())
        }
    }

    fun classifyStaticImage(imageUri: Uri) {
        // TODO: mengklasifikasikan imageUri dari gambar statis.
        if (imageClassifier == null)
            setupImageClassifier()

        var bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, imageUri))
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
        }.copy(Bitmap.Config.ARGB_8888,true)

//        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888,true)

        val tensorImage = TensorImage(DataType.UINT8)
        tensorImage.load(bitmap)
        val imageProcess = org.tensorflow.lite.support.image.ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
            .add(CastOp(DataType.UINT8))
            .build()
        val image = imageProcess.process(TensorImage.fromBitmap(bitmap))
        var inferenceTime = SystemClock.uptimeMillis()
        val result = imageClassifier?.classify(image)
        inferenceTime = SystemClock.uptimeMillis() - inferenceTime

        onResult.invoke(result,inferenceTime)
    }
}
