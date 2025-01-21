package com.neatroots.image_compression_test

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

@Composable
fun ImageCompression_LZW_2_UI() {

    val context = LocalContext.current
    var selectedImage by remember { mutableStateOf<File?>(null) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var compressedImage by remember { mutableStateOf<ByteArray?>(null) }  // Store compressed data as ByteArray
    var decompressedImage by remember { mutableStateOf<Bitmap?>(null) }  // Store decompressed result as Bitmap

    // File picker launcher for images
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val file = File(context.cacheDir, "selected_image.jpg")
            context.contentResolver.openInputStream(it)?.use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            selectedImage = file
            imageBitmap = uriToBitmap(context, it)
        }
    }
    // Use CoroutineScope to handle background tasks
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(text = "LZW Image Comp and Decomp through LZW_2", fontSize = 20.sp)

        Button(onClick = { imagePickerLauncher.launch("image/*") }, modifier = Modifier.padding(16.dp)) {
            Text("Import an Image File")
        }

        selectedImage?.let { file ->
            Spacer(modifier = Modifier.height(8.dp))
            Text("Selected Image: ${file.name}")
            Text("Path: ${file.absolutePath}")
            Text("Size: ${"%.2f".format(file.length() / 1024.0)} KB")
        }

        imageBitmap?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = "Selected Image",
                modifier = Modifier
                    .size(120.dp) // Set the fixed size to 100.dp
            )
        }

        Button(
            onClick = {
                scope.launch {
                    imageBitmap?.let { bitmap ->
                        val byteArray = bitmapToByteArray(bitmap)  // Convert Bitmap to ByteArray
                        val compressed = withContext(Dispatchers.Default) {
                            compressImage(byteArray)  // Compress ByteArray
                        }
                        compressedImage = compressed  // Store the compressed ByteArray

                        // Save compressed file
                        val compressedFileName = "compressed_image_${System.currentTimeMillis()}.bin"
                        val compressedFilePath = "${context.cacheDir}/$compressedFileName"
                        val savedFile = saveByteArrayToFile(compressed, compressedFilePath)
                        savedFile?.let {
                            Log.d("FileSave", "Compressed file saved: ${it.absolutePath}")
                        }
                    }
                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Compress Image")
        }

        compressedImage?.let { byteArray ->
            val compressedBitmap = byteArrayToBitmap(byteArray)
            val compressedFileName = "compressed_image_${System.currentTimeMillis()}.bin"
            val compressedFilePath = "${context.cacheDir}/$compressedFileName"
            val compressedFileSizeKB = "%.2f".format(byteArray.size / 1024.0)

            // Display compressed image preview
            compressedBitmap?.let {
                Image(
                    painter = BitmapPainter(it.asImageBitmap()),
                    contentDescription = "Compressed Image",
                    modifier = Modifier.size(120.dp)
                )
            }

            // Display compressed details
            Text("Compressed File Name: $compressedFileName")
            Text("Compressed File Path: $compressedFilePath")
            Text("Compressed File Size: $compressedFileSizeKB KB")
        }




        Button(
            onClick = {
                scope.launch {
                    compressedImage?.let { byteArray ->
                        val decompressed = withContext(Dispatchers.Default) {
                            decompressImage(byteArray)  // Decompress ByteArray
                        }
                        decompressedImage = decompressed  // Store decompressed Bitmap

                        // Save decompressed file
                        val decompressedFileName = "decompressed_image_${System.currentTimeMillis()}.jpg"
                        val decompressedFilePath = "${context.cacheDir}/$decompressedFileName"
                        decompressedImage?.let { bitmap ->
                            val decompressedByteArray = bitmapToByteArray(bitmap)
                            val savedFile = saveByteArrayToFile(decompressedByteArray, decompressedFilePath)
                            savedFile?.let {
                                Log.d("FileSave", "Decompressed file saved: ${it.absolutePath}")
                            }
                        }
                    }
                }
            },
            modifier = Modifier.padding(16.dp),
            enabled = compressedImage != null
        ) {
            Text("Decompress Image")
        }
//         Display decompressed image separately
        decompressedImage?.let { decompressedBitmap ->
            val decompressedByteArray = bitmapToByteArray(decompressedBitmap)
            val decompressedFileName = "decompressed_image_${System.currentTimeMillis()}.jpg"
            val decompressedFilePath = "${context.cacheDir}/$decompressedFileName"
            val decompressedFileSizeKB = "%.2f".format(decompressedByteArray.size / 1024.0)

            // Display decompressed image preview
            Image(
                painter = rememberAsyncImagePainter(decompressedBitmap),
                contentDescription = "Decompressed Image",
                modifier = Modifier.size(120.dp)
            )

            // Display decompressed details
            Text("Decompressed File Name: $decompressedFileName")
            Text("Decompressed File Path: $decompressedFilePath")
            Text("Decompressed File Size: $decompressedFileSizeKB KB")

            // Validation (optional)
            Text("Validation: Compressed and Decompressed sizes match? ${
                decompressedByteArray.size == (compressedImage?.size ?: 0)
            }")
        }

    }
}




// Convert ByteArray to Bitmap
fun byteArrayToBitmap(byteArray: ByteArray): Bitmap? {
    return try {
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Convert Uri to Bitmap
fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

// Convert Bitmap to ByteArray as JPEG
fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)  // Use JPEG compression instead of PNG
    return byteArrayOutputStream.toByteArray()
}

// Function to save ByteArray to a file
fun saveByteArrayToFile(byteArray: ByteArray, filePath: String): File? {
    return try {
        val file = File(filePath)
        file.outputStream().use { it.write(byteArray) }
        file
    } catch (e: IOException) {
        Log.e("FileSaveError", "Failed to save file: ${e.message}")
        null
    }
}

// Compress Image function - using JPEG
fun compressImage(data: ByteArray): ByteArray {
    Log.d("LZW Compression", "Input ByteArray Size: ${data.size / 1024.0} KB") // Log input size

    // Perform LZW compression
    val compressedData = LZW.compress(data)

    Log.d("LZW Compression", "Compressed ByteArray Size: ${compressedData.size / 1024.0} KB") // Log compressed size

    return compressedData
}

// Decompress Image function - convert back to Bitmap
fun decompressImage(data: ByteArray): Bitmap {
    Log.d("LZW Decompression", "Compressed ByteArray Size: ${data.size / 1024.0} KB") // Log compressed size

    // Perform LZW decompression
    val decompressedData = LZW.decompress(data)

    Log.d("LZW Decompression", "Decompressed ByteArray Size: ${decompressedData.size / 1024.0} KB") // Log decompressed size

    // Convert decompressed data to Bitmap
    return byteArrayToBitmap(decompressedData) ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Fallback to empty bitmap if decompression fails
}

// LZW Compression Class
object LZW {

    // Compression
    fun compress(data: ByteArray): ByteArray {
        val dictionary = mutableMapOf<List<Byte>, Int>()
        var dictSize = 256

        // Initialize dictionary with single-byte sequences
        for (i in 0 until 256) {
            dictionary[listOf(i.toByte())] = i
        }

        val result = ByteArrayOutputStream()
        var w = listOf<Byte>()

        for ((index, byte) in data.withIndex()) {
            val currentByte = listOf(byte)
            val wc = w + currentByte

            if (dictionary.containsKey(wc)) {
                w = wc
            } else {
                val wCode = dictionary[w] ?: throw IllegalStateException("Missing dictionary entry for sequence: $w")
                result.write(wCode)
                if (dictSize < 65536) {
                    dictionary[wc] = dictSize++
                }
                w = currentByte
            }

            // Log dictionary size and result size every 1000 steps
            if (index % 1000 == 0) {
                Log.d("LZW Compression", "Dictionary Size: $dictSize, Result Size: ${result.size() / 1024.0} KB")
            }
        }

        if (w.isNotEmpty()) {
            val wCode = dictionary[w] ?: throw IllegalStateException("Missing dictionary entry for sequence: $w")
            result.write(wCode)
        }

        Log.d("LZW Compression", "Final Dictionary Size: $dictSize, Compressed Data Size: ${result.size() / 1024.0} KB")
        return result.toByteArray()
    }
    // Decompression
    fun decompress(data: ByteArray): ByteArray {
        val dictionary = HashMap<Int, ByteArray>()
        var dictSize = 256

        // Initialize dictionary with single-byte sequences
        for (i in 0 until 256) {
            dictionary[i] = byteArrayOf(i.toByte())
        }

        val result = ByteArrayOutputStream()

        if (data.isEmpty()) {
            Log.e("LZW Decompression", "Input data is empty")
            return ByteArray(0)
        }

        var w = dictionary[data[0].toUByte().toInt()] ?: throw IllegalStateException("Missing dictionary entry")
        result.write(w)

        for ((index, byte) in data.withIndex().drop(1)) {
            val k = byte.toUByte().toInt()
            val entry = dictionary[k] ?: (w + w[0])

            result.write(entry)
            if (dictSize < 65536) {
                dictionary[dictSize++] = w + entry[0]
            }
            w = entry

            // Log dictionary and result size every 1000 steps
            if (index % 1000 == 0) {
                Log.d("LZW Decompression", "Dictionary Size: $dictSize, Decompressed Size: ${result.size() / 1024.0} KB")
            }
        }

        Log.d("LZW Decompression", "Final Dictionary Size: $dictSize, Decompressed Data Size: ${result.size() / 1024.0} KB")
        return result.toByteArray()
    }


}


