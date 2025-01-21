package com.neatroots.image_compression_test

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream


@Preview
@Composable
fun ImageCompression_DEFLATE_UI() {

    val context = LocalContext.current
    var selectedImageFile by remember { mutableStateOf<File?>(null) }
    var compressedImagePath by remember { mutableStateOf<String?>(null) }
    var compressedImagePreview by remember { mutableStateOf<Bitmap?>(null) }
    var compressedImageSize by remember { mutableStateOf<Long?>(null) }
    var decompressedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var decompressedImageSize by remember { mutableStateOf<Long?>(null) }

    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val file = File(context.cacheDir, "selected_image.jpg")
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.copyTo(file.outputStream())
            }
            selectedImageFile = file
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
        Text(
            text = "Image Compression using DEFLATE :-",
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Button to select an image
        Button(onClick = { pickImageLauncher.launch("image/*") }) {
            Text("Select an Image File")
        }

//         Display selected file details
        selectedImageFile?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Selected File: ${it.name}")
            Text("Path: ${it.absolutePath}")
//            Text("Size: ${it.length()} bytes")
            Text("Size: ${"%.2f".format(it.length() / 1024.0)} KB")

            Spacer(modifier = Modifier.height(8.dp))
            Image(
                bitmap = BitmapFactory.decodeFile(it.absolutePath).asImageBitmap(),
                contentDescription = "Selected Image",
                modifier = Modifier.size(150.dp)
            )
        }


        Spacer(modifier = Modifier.height(16.dp))

        // Button to compress the image file
        Button(
            onClick = {
                selectedImageFile?.let { file ->
                    val compressedFile = compressFileWithGZIP(file, context)
                    compressedImagePath = compressedFile.absolutePath
                    compressedImageSize = compressedFile.length()
                    compressedImagePreview = previewCompressedImage(compressedFile, context)
                    Toast.makeText(context, "Image Compressed", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = selectedImageFile != null
        ) {
            Text("Compress Image File")
        }

        // Display compressed file details and preview
        compressedImagePath?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Compressed File Path: $it")
//            Text("Compressed File Size: $compressedImageSize bytes")
            Text("Compressed File Size: ${"%.2f".format(compressedImageSize?.div(1024.0) ?: 0.0)} KB")

            Spacer(modifier = Modifier.height(8.dp))
            compressedImagePreview?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Compressed Image Preview",
                    modifier = Modifier.size(150.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Button to decompress the image file
        Button(
            onClick = {
                compressedImagePath?.let { path ->
                    val decompressedFile = decompressFileWithGZIP(path, context)
                    decompressedImageBitmap = BitmapFactory.decodeFile(decompressedFile.absolutePath)
                    decompressedImageSize = decompressedFile.length()
                    Toast.makeText(context, "Image Decompressed", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = compressedImagePath != null
        ) {
            Text("Decompress Image File")
        }

        // Display decompressed file details
        decompressedImageBitmap?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Decompressed File Path: $it")
//            Text("Decompressed File Size: $decompressedImageSize bytes")
            Text("Compressed File Size: ${"%.2f".format(decompressedImageSize?.div(1024.0) ?: 0.0)} KB")

            Spacer(modifier = Modifier.height(8.dp))
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Decompressed Image",
                modifier = Modifier.size(150.dp)
            )
        }
    }


}

// Function to preview a compressed file

fun previewCompressedImage(compressedFile: File, context: Context): Bitmap? {
    val tempDecompressedFile = File(context.cacheDir, "temp_preview_image.jpg")
    GZIPInputStream(BufferedInputStream(FileInputStream(compressedFile))).use { gzipInputStream ->
        FileOutputStream(tempDecompressedFile).use { fileOutputStream ->
            gzipInputStream.copyTo(fileOutputStream)
        }
    }
    return BitmapFactory.decodeFile(tempDecompressedFile.absolutePath)
}
// Function to compress a GZIP file

fun compressFileWithGZIP(file: File, context: Context): File {
    val compressedFile = File(context.cacheDir, "compressed_image.gz")
    GZIPOutputStream(FileOutputStream(compressedFile)).use { output ->
        FileInputStream(file).use { input ->
            input.copyTo(output)
        }
    }
    return compressedFile
}


// Function to decompress a GZIP file
fun decompressFileWithGZIP(compressedFilePath: String, context: Context): File {
    val decompressedFile = File(context.cacheDir, "decompressed_image.jpg")
    GZIPInputStream(BufferedInputStream(FileInputStream(compressedFilePath))).use { gzipInputStream ->
        FileOutputStream(decompressedFile).use { fileOutputStream ->
            gzipInputStream.copyTo(fileOutputStream)
        }
    }
    return decompressedFile
}
