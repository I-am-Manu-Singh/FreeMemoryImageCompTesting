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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import java.io.File
import java.io.FileOutputStream

@Composable
fun LossyJpgToJpgCompression_JPEG_UI() {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var compressedUri by remember { mutableStateOf<Uri?>(null) }
    var decompressedUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    // Image Picker Launcher
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUri = uri
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(text = "Lossy JPG Image Comp and Decomp through JPEG", fontSize = 20.sp)

        Button(onClick = { launcher.launch("image/*") }) {
            Text("Pick Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display Metadata and Original Image
        imageUri?.let {
            Text(text = "Original Image")
            DisplayImage(uri = it)
            DisplayMetadata(context = context, uri = it)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Compress Image Button
        Button(onClick = {
            if (imageUri != null) {
                compressedUri = processImage(
                    context = context,
                    imageUri = imageUri,
                    quality = 50,
                    outputFileName = "compressed_image_${System.currentTimeMillis()}.jpg"
                )
                Toast.makeText(context, "Image Compressed!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Select an Image First", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Compress Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display Metadata and Compressed Image
        compressedUri?.let {
            Text(text = "Compressed Image")
            DisplayImage(uri = it)
            DisplayMetadata(context = context, uri = it)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Decompress Image Button
        Button(onClick = {
            if (compressedUri != null) {
                decompressedUri = processImage(
                    context = context,
                    imageUri = compressedUri,
                    quality = 100,
                    outputFileName = "decompressed_image_${System.currentTimeMillis()}.jpg"
                )
                Toast.makeText(context, "Image Decompressed!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Compress an Image First", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Decompress Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display Metadata and Decompressed Image
        decompressedUri?.let {
            Text(text = "Decompressed Image")
            DisplayImage(uri = it)
            DisplayMetadata(context = context, uri = it)
        }
    }
}

@Composable
fun DisplayImage(uri: Uri) {
    val painter: Painter = rememberImagePainter(data = uri)
    Image(
        painter = painter,
        contentDescription = "Displayed Image",
        modifier = Modifier
            .size(250.dp)
            .padding(8.dp)
    )
}

@Composable
fun DisplayMetadata(context: Context, uri: Uri) {
    val file = File(uri.path ?: "")
    val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))

    if (file.exists()) {
        Text(text = "File Name: ${file.name}")
        Text(text = "File Path: ${file.absolutePath}")
        Text(text = "File Size: ${file.length() / 1024} KB")
    } else {
        val fileDetails = getFileDetailsFromUri(context, uri)
        fileDetails?.let {
            Text(text = "File Name: ${it.name}")
            Text(text = "File Path: ${it.path}")
            Text(text = "File Size: ${it.size / 1024} KB")
        }
    }

    // Display dimensions
    bitmap?.let {
        Text(text = "Dimensions: ${it.width} x ${it.height}")
    }
}

// Helper function to get file details from Uri
data class FileDetails(val name: String, val path: String, val size: Long)

fun getFileDetailsFromUri(context: Context, uri: Uri): FileDetails? {
    val cursor = context.contentResolver.query(uri, null, null, null, null) ?: return null
    cursor.use {
        val nameIndex = cursor.getColumnIndexOrThrow("_display_name")
        val sizeIndex = cursor.getColumnIndexOrThrow("_size")
        cursor.moveToFirst()
        val name = cursor.getString(nameIndex)
        val size = cursor.getLong(sizeIndex)
        val path = uri.path ?: "Unknown"
        return FileDetails(name, path, size)
    }
}

// Function to Process (Compress/Decompress) Image
fun processImage(
    context: Context,
    imageUri: Uri?,
    quality: Int,
    outputFileName: String
): Uri? {
    if (imageUri == null) return null

    val inputStream = context.contentResolver.openInputStream(imageUri)
    val bitmap = BitmapFactory.decodeStream(inputStream)

    // Get the external Pictures directory
    val picturesDir = context.getExternalFilesDir(null)
    val outputFile = File(picturesDir, outputFileName)

    val outputStream = FileOutputStream(outputFile)

    // Compress and save the Bitmap
    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

    outputStream.flush()
    outputStream.close()

    return Uri.fromFile(outputFile)
}