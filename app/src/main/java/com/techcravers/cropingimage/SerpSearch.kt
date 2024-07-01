package com.techcravers.cropingimage

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import org.json.JSONObject
import java.io.IOException
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import okhttp3.OkHttpClient
import okhttp3.Request

const val SERP_API_KEY = "YOUR_API_KEY"

data class ImageData(val title: String, val link: String, val url: String)



@Composable
fun Run(){
    // State to hold the uploaded image URL
    val (imageUrl, setImageUrl) = remember { mutableStateOf<String?>(null) }

    if (imageUrl == null) {
        // Upload Image Screen if the image URL is not available
        UploadImageScreen(
            imagePath = "/path/to/your/image.png", // Replace with the actual image path
            onUploadComplete = { url ->
                setImageUrl(url) // Set the image URL and switch to the FetchAndDisplayData screen
            }
        )
    } else {
        // Fetch and Display Data Screen if the image URL is available
        FetchAndDisplayData(imageUrl = imageUrl)
    }
}

@Composable
fun FetchAndDisplayData(imageUrl : String = "https://i.ibb.co/gPxcwsZ/Screenshot-2.png") {
    var responseData by remember { mutableStateOf<List<ImageData>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient().newBuilder().build()
                val baseUrl = "https://serpapi.com/search"
                val apiKey = SERP_API_KEY
                val engine = "google_lens"
                val outputFormat = "JSON"
                val request: Request = Request.Builder()
                    .url("$baseUrl?api_key=$apiKey&engine=$engine&url=$imageUrl&output=$outputFormat")
                    .get()
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        val images = parseResponse(responseBody)
                        responseData = images
                    }
                } else {
                    error = "Request failed: ${response.message}"
                }
            } catch (e: IOException) {
                error = "Exception: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    if (isLoading) {
        CircularProgressIndicator()
    } else {
        error?.let {
            Text(text = it)
        } ?: run {
            currentUrl?.let { url ->
                WebViewScreen(url, onBack = { currentUrl = null })
            } ?: responseData?.let { images ->
                ImageGrid(images, onItemClick = { currentUrl = it })
            }
        }
    }
}

@Composable
fun ImageGrid(images: List<ImageData>, onItemClick: (String) -> Unit) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val cellSize = 210.dp
    val numberOfColumns = (screenWidth / cellSize).toInt()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(images.chunked(numberOfColumns)) { rowImages ->
            Row(
                modifier = Modifier
                    .padding(2.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                rowImages.forEach { image ->
                    ImageItem(image, Modifier.size(cellSize), onItemClick)
                }
            }
        }
    }
}

@Composable
fun ImageItem(imageData: ImageData, modifier: Modifier, onItemClick: (String) -> Unit) {
    Column(modifier = modifier.padding(8.dp)) {
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            onClick = { onItemClick(imageData.url) }
        ) {
            Column {
                Image(
                    painter = rememberImagePainter(data = imageData.link),
                    contentDescription = imageData.title,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = imageData.title,
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(url: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Web View") },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(painter = painterResource(id = android.R.drawable.ic_media_previous), contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        WebViewContent(url)
    }
}

@Composable
fun WebViewContent(url: String) {
    val context = LocalContext.current
    AndroidView(factory = {
        WebView(context).apply {
            webViewClient = WebViewClient()
            loadUrl(url)
        }
    })
}

fun parseResponse(responseBody: String): List<ImageData> {
    val imageDataList = mutableListOf<ImageData>()
    val json = JSONObject(responseBody)
    val visualMatches = json.getJSONArray("visual_matches")
    for (i in 0 until visualMatches.length()) {
        val match = visualMatches.getJSONObject(i)
        val title = match.getString("title")
        val link = match.getString("thumbnail")
        val url = match.getString("link")
        imageDataList.add(ImageData(title, link, url))
    }
    return imageDataList
}