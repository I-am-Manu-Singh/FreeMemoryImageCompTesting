package com.neatroots.image_compression_test
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.WHITE)
        )
            setContent {
            MaterialTheme{
                TabSwitcherScreen()
            }
        }
    }
}
@Composable
fun TabSwitcherScreen() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Titles for the tabs
    val tabTitles = listOf(
        "String Comp DEFLATE",
        ".txt file Comp DEFLATE",
        "Image Comp DEFLATE",
        "Image Comp LZW",
        "String Comp LZW",
        ".txt file Comp LZW",
        "Image Comp LZW_2",
        "Lossy Jpeg to Jpeg Comp",
        "Lossless Jpeg to Jpeg Comp",

    )
    Column(modifier = Modifier.fillMaxSize()) {
        // Scrollable Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 16.dp // Add padding at the edges for aesthetics
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Default,
                            maxLines = 2, // Limit to 1 line to prevent overflow
                            overflow = TextOverflow.Ellipsis, // Truncate if text is too long
                            modifier = Modifier.padding(22.dp) // Add padding for better spacing
                        )
                    }
                )
            }
        }
        // Display content based on selected tab
        when (selectedTabIndex) {
            0 -> StringCompression_DEFALTE_UI()
            1 -> StringCompression_DEFLATE_TXT_UI()
            2 -> ImageCompression_DEFLATE_UI()
            3 -> ImageCompression_LZW_UI()
            4 -> StringCompressionLZW_UI()
            5 -> StringCompression_LZW_TXT_UI()
            6 -> ImageCompression_LZW_2_UI()
            7 -> LossyJpgToJpgCompression_JPEG_UI()
            8 -> LosslessJpgToJpgCompression_JPEG_UI()

        }
    }
}