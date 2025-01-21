package com.neatroots.image_compression_test

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.PriorityQueue

@Composable
fun LosslessJpgToJpgCompression_JPEG_UI() {

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var compressedUri by remember { mutableStateOf<Uri?>(null) }
    var decompressedUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Huffman Compression and Decompression", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { launcher.launch("image/*") }) {
            Text("Pick Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        imageUri?.let {
            Text("Original Image")
            DisplayImage2(uri = it)
            DisplayMetadata2(context, it)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (imageUri != null) {
                compressedUri = processImageHuffman(context, imageUri, isCompress = true)
                Toast.makeText(context, "Compression Completed!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Please select an image first!", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Compress Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        compressedUri?.let {
            Text("Compressed File Details")
            DisplayMetadata2(context, it)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (compressedUri != null) {
                decompressedUri = processImageHuffman(context, compressedUri, isCompress = false)
                Toast.makeText(context, "Decompression Completed!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Please compress an image first!", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Decompress Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        decompressedUri?.let {
            Text("Decompressed Image")
            DisplayImage2(uri = it)
            DisplayMetadata2(context, it)
        }
    }
}

@Composable
fun DisplayImage2(uri: Uri) {
    val painter: Painter = rememberImagePainter(data = uri)
    Image(
        painter = painter,
        contentDescription = null,
        modifier = Modifier
            .size(250.dp)
            .padding(8.dp)
    )
}

@Composable
fun DisplayMetadata2(context: Context, uri: Uri) {
    val file = File(uri.path ?: "")
    if (file.exists()) {
        Text("File Name: ${file.name}")
        Text("File Path: ${file.absolutePath}")
        Text("File Size: ${file.length() / 1024} KB")
    } else {
        uri.path?.let { path ->
            val inputStream = context.contentResolver.openInputStream(uri)
            val size = inputStream?.available()?.div(1024) ?: 0
            Text("File Name: ${File(path).name}")
            Text("File Path: $path")
            Text("File Size: $size KB")
        }
    }
}

// Huffman Compression/Decompression
fun processImageHuffman(context: Context, imageUri: Uri?, isCompress: Boolean): Uri? {
    if (imageUri == null) return null
    val inputStream: InputStream = context.contentResolver.openInputStream(imageUri) ?: return null
    val inputBytes = inputStream.readBytes()
    inputStream.close()

    val outputBytes = if (isCompress) {
        compressHuffman(inputBytes)
    } else {
        decompressHuffman(inputBytes)
    }

    val outputFileName = if (isCompress) "compressed_image.huff" else "decompressed_image.jpg"
    val outputFile = File(context.getExternalFilesDir(null), outputFileName)
    outputFile.writeBytes(outputBytes)
    return Uri.fromFile(outputFile)
}

// Huffman Coding Functions
data class Node(val byte: Byte?, val frequency: Int, val left: Node? = null, val right: Node? = null)

@SuppressLint("NewApi")
fun compressHuffman(inputBytes: ByteArray): ByteArray {
    // Build frequency map
    val frequencyMap = inputBytes.asIterable().groupingBy { it }.eachCount()

    // Build Huffman tree
    val priorityQueue = PriorityQueue<Node>(compareBy { it.frequency })
    frequencyMap.forEach { (byte, frequency) ->
        priorityQueue.add(Node(byte, frequency))
    }

    while (priorityQueue.size > 1) {
        val left = priorityQueue.poll()
        val right = priorityQueue.poll()
        priorityQueue.add(Node(null, left.frequency + right.frequency, left, right))
    }
    val huffmanTree = priorityQueue.poll()

    // Generate Huffman codes
    val huffmanCodes = mutableMapOf<Byte, String>()
    generateHuffmanCodes(huffmanTree, "", huffmanCodes)

    // Encode data
    val encodedData = inputBytes.joinToString("") { huffmanCodes[it] ?: "" }
    val outputStream = ByteArrayOutputStream()

    // Write encoded data to output
    outputStream.write(encodedData.toByteArray(Charsets.UTF_8))
    return outputStream.toByteArray()
}

fun decompressHuffman(inputBytes: ByteArray): ByteArray {
    // Decode Huffman data (use a dummy tree for demonstration)
    // You need to store the Huffman tree metadata for actual decompression
    return inputBytes // Placeholder: implement decoding with stored Huffman tree
}

fun generateHuffmanCodes(node: Node?, prefix: String, map: MutableMap<Byte, String>) {
    if (node == null) return
    if (node.byte != null) {
        map[node.byte] = prefix
    }
    generateHuffmanCodes(node.left, prefix + "0", map)
    generateHuffmanCodes(node.right, prefix + "1", map)
}