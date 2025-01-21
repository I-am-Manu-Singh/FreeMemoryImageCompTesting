package com.neatroots.image_compression_test

import android.util.Base64
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.Inflater

@Preview
@Composable


fun StringCompression_DEFALTE_UI() {
    var inputText by remember { mutableStateOf("") }
    var compressedString by remember { mutableStateOf<String?>(null) }
    var decompressedString by remember { mutableStateOf<String?>(null) }
    val isCompressClicked by remember { mutableStateOf(false) } // Track if the first button is clicked


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {

        Text(
            text = "String Compression Testing :-",
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
        // Input text field for the string
        OutlinedTextField(
            value = inputText,
            placeholder = { Text("Enter your text here.") },
            onValueChange = { inputText = it },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .height(100.dp)
        )
//        Text("Input String Length: ${inputText.length}")

        // Compress string button
        Button(
            onClick = {
                compressedString = compressString(inputText)
            },
            modifier = Modifier.padding(16.dp),

        ) {
            Text("Compress String")
        }

        // Display compressed string
        compressedString?.let {
            Text("Compressed String: $it")
        }

        // Decompress string button
        Button(
            onClick = {
                decompressedString = decompressString(compressedString ?: "")
            },

            modifier = Modifier.padding(16.dp),
        ) {
            Text("Decompress String")
        }

        // Display decompressed string
        decompressedString?.let {
            Text("Decompressed String: $it")
        }
    }
}

// Function to compress a string using Deflate and Base64 encoding
fun compressString(input: String): String {
    val deflater = Deflater()
    deflater.setInput(input.toByteArray())
    deflater.finish()

    val byteArrayOutputStream = ByteArrayOutputStream(input.length)
    val buffer = ByteArray(1024)
    while (!deflater.finished()) {
        val byteCount = deflater.deflate(buffer)
        byteArrayOutputStream.write(buffer, 0, byteCount)
    }
    deflater.end()

    // Return the compressed data as a Base64 string for easy storage
    return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)
}

// Function to decompress a string using Inflater and Base64 decoding
fun decompressString(input: String): String {
    val decodedBytes = Base64.decode(input, Base64.DEFAULT)

    val inflater = Inflater()
    inflater.setInput(decodedBytes)
    val byteArrayOutputStream = ByteArrayOutputStream(decodedBytes.size)
    val buffer = ByteArray(1024)

    while (!inflater.finished()) {
        val byteCount = inflater.inflate(buffer)
        byteArrayOutputStream.write(buffer, 0, byteCount)
    }
    inflater.end()

    // Return the decompressed string
    return byteArrayOutputStream.toString()
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    StringCompression_DEFALTE_UI()
}

