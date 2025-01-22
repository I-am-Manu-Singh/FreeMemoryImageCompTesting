# Image and String Compression/Decompression App

This project demonstrates an Android application that implements both image and string compression and decompression using the DEFLATE and LZW algorithms. The app leverages Kotlin Coroutines for asynchronous operations and the Coil library for efficient image loading.

### Features :

1. Image Compression/Decompression

Compress and decompress image files using DEFLATE and LZW algorithms.

Includes support for both Lossless and Lossy JPEG to JPEG compression.

2. String Compression/Decompression

Compress and decompress string data and .txt files using DEFLATE and LZW algorithms.

Allows comparison of compressed file sizes to evaluate the efficiency of algorithms.

3. Coroutines for Asynchronous Operations

Utilizes Kotlin Coroutines for smooth, non-blocking execution.

### Technologies and Libraries Used :

1.  DEFLATE and LZW Algorithms

Core compression techniques for both strings and images.

2. Kotlin Coroutines

Provides efficient threading for asynchronous operations like compression/decompression.

3. Coil

Lightweight image loading library for Jetpack Compose.

### Dependencies Used :
1. // Coil for image loading
```implementation(libs.coil.compose)```

// Kotlin Coroutines for asynchronous operations
```implementation(libs.kotlinx.coroutines.android)```

### Project Structure :

```
com.neatroots.image_compression_test
├── ui.theme
│   ├── ImageCompression_DEFLATE_UI.kt             // DEFLATE-based image compression UI
│   ├── ImageCompression_LZW_2_UI.kt             // Alternative LZW compression UI
│   ├── ImageCompression_LZW_UI.kt               // LZW-based image compression UI
│   ├── LosslessJpgToJpgCompression_JPEG_UI.kt  // Lossless JPEG compression UI
│   ├── LossyJpgToJpgCompression_JPEG_UI.kt     // Lossy JPEG compression UI
│   ├── MainActivity.kt                         // Main activity with navigation logic
│   ├── StringCompression_DEFLATE_TXT_UI.kt     // DEFLATE-based string compression UI for .txt
│   ├── StringCompression_LZW_TXT_UI.kt         // LZW-based string compression UI for .txt
│   └── StringCompressionLZW_UI.kt              // LZW-based string compression for strings                            
```
### How to Use :
1. Clone the repository
   
```git clone <repository-url>```

3. Open the project in Android Studio.

4. Add the required dependencies to your build.gradle file if not already included.

5. Run the app on an emulator or a physical device.

6. Use the app to manage your contacts by adding, editing, and deleting them.

### Screenshots & App Demo Video:

(Will upload these soon.)

### Future Improvements

1. Add support for additional compression algorithms.

2. Provide a detailed analytics screen to compare compression ratios.

3. Implement cloud storage for saving compressed files.

4. Enhance UI/UX for better accessibility.

### License

This project is open-source and available under the MIT License.
