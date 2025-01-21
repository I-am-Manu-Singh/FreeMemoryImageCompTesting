package com.neatroots.image_compression_test

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

@Preview
@Composable
fun ImageCompression_LZW_UI() {
    val context = LocalContext.current
    // State variables
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var compressedImageUri by remember { mutableStateOf<Uri?>(null) }
    var decompressedImageUri by remember { mutableStateOf<Uri?>(null) }

    var compressedFilePath by remember { mutableStateOf("") }
    var decompressedFilePath by remember { mutableStateOf("") }
    var compressedImageSize by remember { mutableLongStateOf(0L) }
    var decompressedImageSize by remember { mutableLongStateOf(0L) }
    var selectedImageSize by remember { mutableLongStateOf(0L) }  // To hold the size of the selected image

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            selectedImageUri = uri
            // Get the file size of the selected image
            selectedImageUri?.let {
                selectedImageSize = getFileSize(context, it)
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = "LZW Image Compression",
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { pickImageLauncher.launch("image/*") }) {
            Text("Pick Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display selected image with scaled-down size
        selectedImageUri?.let {
            Box(modifier = Modifier.height(200.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Selected Image",
                    modifier = Modifier.fillMaxSize().padding(8.dp) // Adjust padding to scale image
                )
            }
            Text("Selected Image Path: $it")
            Text("Size: ${selectedImageSize / 1024} KB")  // Display size in KB
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (selectedImageUri != null) {
                compressedImageUri = compressImage(context, selectedImageUri!!) { path, size ->
                    compressedFilePath = path
                    compressedImageSize = size
                }

            } else {
                Toast.makeText(context, "Please pick an image first!", Toast.LENGTH_SHORT).show()
            }


        }

        ) {
            Text("Compress Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display the compressed image if the URI is not null
        compressedImageUri?.let {
            Box(modifier = Modifier.height(200.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Compressed Image",
                    modifier = Modifier.fillMaxSize().padding(8.dp)
                )
            }
            Text("Compressed File Path: $compressedFilePath")
            Text("Compressed File Size: ${compressedImageSize / 1024} KB")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (compressedImageUri != null) {
                decompressedImageUri = decompressImage(context, compressedImageUri!!) { path, size ->
                    decompressedFilePath = path
                    decompressedImageSize = size
                }
            } else {
                Toast.makeText(context, "Please compress the image first!", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Decompress Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display the decompressed image if the URI is not null
        decompressedImageUri?.let {
            Box(modifier = Modifier.height(200.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Decompressed Image",
                    modifier = Modifier.fillMaxSize().padding(8.dp)
                )
            }
            Text("Decompressed File Path: $decompressedFilePath")
            Text("Decompressed File Size: ${decompressedImageSize / 1024} KB")
        }
    }
}

// Function to get the file size of the selected image
fun getFileSize(context: Context, uri: Uri): Long {
    var fileSize: Long = 0
    try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            fileSize = inputStream.available().toLong()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return fileSize
}

    // Compress image using LZW algorithm
    fun compressImage(context: Context, imageUri: Uri, updateFileInfo: (String, Long) -> Unit): Uri {
        val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(imageUri))
        val compressedFile = File(context.filesDir, "compressed_image.lzw") // Store in app's internal storage
        val outputStream = FileOutputStream(compressedFile)

        // Use LZW compression algorithm here (simplified example)
        val lzw = LZWCompressor()
        lzw.compressBitmap(bitmap, outputStream)

        // Update the state variables via the lambda function
        updateFileInfo(compressedFile.absolutePath, compressedFile.length())

        return Uri.fromFile(compressedFile)
    }

    // Decompress image using LZW algorithm
    fun decompressImage(context: Context, compressedUri: Uri, updateFileInfo: (String, Long) -> Unit): Uri {
        val compressedFile = File(compressedUri.path!!)
        val decompressedFile = File(context.filesDir, "decompressed_image.jpg")

        val inputStream = FileInputStream(compressedFile)
        val outputStream = FileOutputStream(decompressedFile)

        // Use LZW decompression algorithm here (simplified example)
        val lzw = LZWDecompressor()
        lzw.decompress(inputStream, outputStream)

        // Update the state variables via the lambda function
        updateFileInfo(decompressedFile.absolutePath, decompressedFile.length())

        return Uri.fromFile(decompressedFile)
    }

    class LZWCompressor {
        fun compressBitmap(bitmap: Bitmap, outputStream: OutputStream) {
            // Convert the bitmap into a byte array (uncompressed raw image data)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val data = byteArrayOutputStream.toByteArray()

            // Perform LZW compression
            val compressedData = compress(data)
            outputStream.write(compressedData)
        }

        private fun compress(data: ByteArray): ByteArray {
            val dictionary = mutableMapOf<String, Int>()
            var dictSize = 256
            for (i in 0 until 256) dictionary[i.toChar().toString()] = i

            val result = ByteArrayOutputStream()
            var w = ""

            for (byte in data) {
                val char = byte.toUByte().toInt().toChar() // Safely convert Byte to Char
                val wc = w + char
                if (dictionary.containsKey(wc)) {
                    w = wc
                } else {
                    result.write(dictionary[w] ?: 0)
                    dictionary[wc] = dictSize++
                    w = char.toString()
                }
            }
            if (w.isNotEmpty()) result.write(dictionary[w] ?: 0)

            return result.toByteArray()
        }
    }

    class LZWDecompressor {
        fun decompress(inputStream: InputStream, outputStream: OutputStream) {
            val data = inputStream.readBytes()

            // Perform LZW decompression
            val decompressedData = decompress(data)
            outputStream.write(decompressedData)
        }

        private fun decompress(data: ByteArray): ByteArray {
            val dictionary = mutableMapOf<Int, String>()
            var dictSize = 256
            for (i in 0 until 256) dictionary[i] = i.toChar().toString()

            val result = ByteArrayOutputStream()

            if (data.isEmpty()) return ByteArray(0) // Handle empty input case

            var w = dictionary[data[0].toUByte().toInt()] ?: ""
            result.write(w.toByteArray())

            for (i in 1 until data.size) {
                val k = data[i].toUByte().toInt()
                val entry = if (dictionary.containsKey(k)) {
                    dictionary[k]!!
                } else if (k == dictSize) {
                    w + w[0] // Special case when k == dictSize
                } else {
                    throw IllegalStateException("Invalid LZW data")
                }

                result.write(entry.toByteArray())
                dictionary[dictSize++] = w + entry[0]
                w = entry
            }

            return result.toByteArray()
        }
    }