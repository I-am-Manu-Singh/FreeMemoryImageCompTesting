package com.neatroots.image_compression_test

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
@Preview
@Composable
fun StringCompression_DEFLATE_TXT_UI() {
    val context = LocalContext.current
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var compressedFilePath by remember { mutableStateOf<String?>(null) }
    var compressedFileName by remember { mutableStateOf<String?>(null) }
    var compressedFileSize by remember { mutableStateOf<Long?>(null) }
    var decompressedFilePath by remember { mutableStateOf<String?>(null) }
    var decompressedFileName by remember { mutableStateOf<String?>(null) }
    var decompressedFileSize by remember { mutableStateOf<Long?>(null) }
    var decompressedText by remember { mutableStateOf<String?>(null) } // Holds the decompressed text content

    // Launch activity to select a file
    val pickFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val file = File(context.cacheDir, "selected_text_file.txt")
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.copyTo(file.outputStream())
            }
            selectedFile = file
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
            text = "Compression testing through .txt file:-",
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
        // Button to select a file
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { pickFileLauncher.launch("text/plain") }) {
            Text("Select a Text File")
        }

        selectedFile?.let {
            Text("Selected File: ${it.name}")
            Text("Selected File Path: ${it.absolutePath}")
            Text("Selected File Size: ${it.length()} bytes")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Button to compress the file
        Button(
            onClick = {
                selectedFile?.let { file ->
                    val compressedFile = compressFile(file, context)
                    compressedFilePath = compressedFile.absolutePath
                    compressedFileName = compressedFile.name
                    compressedFileSize = compressedFile.length()
                    Toast.makeText(context, "File Compressed", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = selectedFile != null
        ) {
            Text("Compress File")
        }

        compressedFilePath?.let {
            Text("Compressed File Path: $it")
        }
        compressedFileName?.let {
            Text("Compressed File Name: $it")
        }
        compressedFileSize?.let {
            Text("Compressed File Size: $it bytes")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Button to decompress the compressed file
        Button(
            onClick = {
                compressedFilePath?.let { path ->
                    val decompressedFile = decompressFile(path, context)
                    decompressedFilePath = decompressedFile.absolutePath
                    decompressedFileName = decompressedFile.name
                    decompressedFileSize = decompressedFile.length()
                    // Read the decompressed text content
                    decompressedText = decompressedFile.readText()
                    Toast.makeText(context, "File Decompressed", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = compressedFilePath != null
        ) {
            Text("Decompress File")
        }
        decompressedFilePath?.let {
            Text("Decompressed File Path: $it")
        }
        decompressedFileName?.let {
            Text("Decompressed File Name: $it")
        }
        decompressedFileSize?.let {
            Text("Decompressed File Size: $it bytes")
        }

        // Display the decompressed text content
        decompressedText?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Decompressed Text Content:")
            OutlinedTextField(
                value = it,
                onValueChange = {},
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                readOnly = true // Makes the text non-editable
            )
        }
    }
}

// Function to compress a file
fun compressFile(inputFile: File, context: Context): File {
    val compressedFile = File(context.cacheDir, "${inputFile.name}.gz")
    GZIPOutputStream(BufferedOutputStream(FileOutputStream(compressedFile))).use { gzipOutputStream ->
        FileInputStream(inputFile).use { fileInputStream ->
            fileInputStream.copyTo(gzipOutputStream)
        }
    }
    return compressedFile
}

// Function to decompress a file
fun decompressFile(compressedFilePath: String, context: Context): File {
    val decompressedFile = File(context.cacheDir, "decompressed_file.txt")
    GZIPInputStream(BufferedInputStream(FileInputStream(compressedFilePath))).use { gzipInputStream ->
        FileOutputStream(decompressedFile).use { fileOutputStream ->
            gzipInputStream.copyTo(fileOutputStream)
        }
    }
    return decompressedFile
}