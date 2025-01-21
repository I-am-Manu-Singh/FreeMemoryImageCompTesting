package com.neatroots.image_compression_test

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

@Preview
@Composable
fun StringCompressionLZW_UI() {

    var inputText by remember { mutableStateOf("") }
    var compressedString by remember { mutableStateOf<String?>(null) }
    var decompressedString by remember { mutableStateOf<String?>(null) }
    var isCompressClicked by remember { mutableStateOf(false) } // Track first button click

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ){
        Text(
            text = "String Compression Testing through LZW:",
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )

        OutlinedTextField(
            value = inputText,
            placeholder = { Text("Enter your text here.") },
            onValueChange = { inputText = it },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .height(100.dp)
        )

        // Compress string button
        Button(
            onClick = {
                compressedString = compress(inputText).joinToString(",")
                isCompressClicked = true // Set to true when clicked
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Compress String")
        }

        compressedString?.let {
            Text("Compressed String: $it", modifier = Modifier.padding(8.dp))
        }

        // Decompress string button (disabled until first button is clicked)
        Button(
            onClick = {
                decompressedString = compressedString?.split(",")
                    ?.map { it.toInt() }
                    ?.let { decompress(it) }
            },
            modifier = Modifier.padding(16.dp),
            enabled = isCompressClicked // Disable based on state
        ) {
            Text("Decompress String")
        }

        decompressedString?.let {
            Text("Decompressed String: $it", modifier = Modifier.padding(8.dp))
        }
    }

}


fun compress(input: String): List<Int> {
    val dictionary = mutableMapOf<String, Int>() // Initialize dictionary
    var dictSize = 256 // Start codes for custom sequences
    var w = "" // Current sequence
    val result = mutableListOf<Int>() // Store resulting codes

    // Initialize dictionary with single-character ASCII mappings
    for (i in 0 until 256) {
        dictionary[i.toChar().toString()] = i
    }

    input.forEach { char ->
        val combined = w + char // Form a new sequence
        if (dictionary.containsKey(combined)) {
            w = combined // Continue extending the sequence
        } else {
            // Add the code for the current sequence to the result
            result.add(dictionary[w]!!)
            // Add the new sequence to the dictionary
            dictionary[combined] = dictSize++
            w = char.toString() // Reset to the current character
        }
    }

    // Add the final sequence to the result
    if (w.isNotEmpty()) {
        result.add(dictionary[w]!!)
    }

    return result
}

fun decompress(codes: List<Int>): String {
    val dictionary = mutableMapOf<Int, String>() // Initialize dictionary
    var dictSize = 256 // Start codes for custom sequences

    // Initialize dictionary with single-character ASCII mappings
    for (i in 0 until 256) {
        dictionary[i] = i.toChar().toString()
    }

    val result = StringBuilder()
    var previous = dictionary[codes.first()]!! // Decode the first code
    result.append(previous)

    codes.drop(1).forEach { code ->
        val entry = when {
            dictionary.containsKey(code) -> dictionary[code]!!
            code == dictSize -> previous + previous[0] // Special case for unknown code
            else -> throw IllegalArgumentException("Invalid LZW code: $code")
        }

        result.append(entry)

        // Add a new sequence to the dictionary
        dictionary[dictSize++] = previous + entry[0]
        previous = entry
    }
    return result.toString()
}
