package com.neatroots.image_compression_test

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

@Composable
fun StringCompression_LZW_TXT_UI(){
    val context = LocalContext.current
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var fileContent by remember { mutableStateOf<String?>(null) }
    var compressedString by remember { mutableStateOf<String?>(null) }
    var decompressedString by remember { mutableStateOf<String?>(null) }
    var isCompressClicked by remember { mutableStateOf(false) } // Track compression

    // Declare state variables for compressed and decompressed file
    var compressedFile by remember { mutableStateOf<File?>(null) }
    var decompressedFile by remember { mutableStateOf<File?>(null) }

    // File picker launcher
    val filePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val file = File(context.cacheDir, "selected_file.txt")
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    file.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                selectedFile = file
                fileContent = file.readText()
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
            text = ".Txt file String Compression through LZW",
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(8.dp)
        )

        // Button to import a .txt file
        Button(onClick = { filePickerLauncher.launch("text/plain") }, modifier = Modifier.padding(16.dp)) {
            Text("Import a .txt File")
        }

        selectedFile?.let { file ->
            Spacer(modifier = Modifier.height(8.dp))
            Text("Selected File: ${file.name}")
            Text("Path: ${file.absolutePath}")
            Text("Size: ${"%.2f".format(file.length() / 1024.0)} KB")

            Spacer(modifier = Modifier.height(8.dp))


            // Compress content button
            Button(
                onClick = {
                    compressedString = fileContent?.let { compress2(it).joinToString(",") }
                    isCompressClicked = true // Enable decompress button
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Compress File Content")
            }
        }

        compressedString?.let {
            // Save compressed content to file if it's not already done
            if (compressedFile == null) {
                compressedFile = File(context.cacheDir, "compressed_file.txt")
                compressedFile?.writeText(it) // Save the compressed string to the file
            }

            // Display compressed string in the UI
            OutlinedTextField(
                value = it,
                onValueChange = {},
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                readOnly = true // Makes the text non-editable
            )

            // Display the compressed file's size
            compressedFile?.let { file ->
                Spacer(modifier = Modifier.height(8.dp))
                Text("Compressed File: ${file.name}")
                Text("Path: ${file.absolutePath}")
                Text("Compressed File Size: ${"%.2f".format(file.length() / 1024.0)} KB") // Display file size in KB
            }
        }

        // Decompress button
        Button(
            onClick = {
                decompressedString = compressedString?.split(",")
                    ?.map { it.toInt() }
                    ?.let { decompress2(it) }.toString()
            },
            modifier = Modifier.padding(16.dp),
            enabled = isCompressClicked // Enable only after compression
        ) {
            Text("Decompress Content")
        }

        decompressedString?.let {

            // Save decompressed content to file if it's not already done
            if (decompressedFile == null) {
                decompressedFile = File(context.cacheDir, "decompressed_file.txt")
                decompressedFile?.writeText(it) // Save the decompressed string to the file
            }
            OutlinedTextField(
                value = it,
                onValueChange = {},
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                readOnly = true // Makes the text non-editable
            )
        }

        // Display decompressed file details
        decompressedFile?.let { file ->
            Spacer(modifier = Modifier.height(8.dp))
            Text("Decompressed File: ${file.name}")
            Text("Path: ${file.absolutePath}")
            Text("Size: ${"%.2f".format(file.length() / 1024.0)} KB")
        }
    }
}



// Compress function using LZW
fun compress2(input: String): List<Int> {
    val dictionary = mutableMapOf<String, Int>()
    var dictSize = 256
    var w = ""
    val result = mutableListOf<Int>()

    // Initialize dictionary with single-character mappings
    for (i in 0 until 256) {
        dictionary[i.toChar().toString()] = i
    }

    input.forEach { char ->
        val combined = w + char
        if (dictionary.containsKey(combined)) {
            w = combined
        } else {
            result.add(dictionary[w]!!)
            dictionary[combined] = dictSize++
            w = char.toString()
        }
    }

    if (w.isNotEmpty()) {
        result.add(dictionary[w]!!)
    }

    return result
}

// Decompress function using LZW
fun decompress2(codes: List<Int>): String {
    val dictionary = mutableMapOf<Int, String>()
    var dictSize = 256

    // Initialize dictionary with single-character mappings
    for (i in 0 until 256) {
        dictionary[i] = i.toChar().toString()
    }

    val result = StringBuilder()
    var previous = dictionary[codes.first()]!!
    result.append(previous)

    codes.drop(1).forEach { code ->
        val entry = when {
            dictionary.containsKey(code) -> dictionary[code]!!
            code == dictSize -> previous + previous[0]
            else -> throw IllegalArgumentException("Invalid LZW code: $code")
        }

        result.append(entry)
        dictionary[dictSize++] = previous + entry[0]
        previous = entry
    }

    return result.toString()
}