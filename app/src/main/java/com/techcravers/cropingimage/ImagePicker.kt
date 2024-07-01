package com.techcravers.cropingimage

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.InputStream
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import android.os.Environment

//circlesearch899@gmail.com

val SCREENSHOT_FILE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/image.jpg"
const val IMGBB_API_KEY = "IMGBB_API_KEY"

@Composable
fun ImagePickerApp() {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadedImageUrl by remember { mutableStateOf<String?>(null) }
    var googleSearchUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                saveImageToFilePath(context, uri, SCREENSHOT_FILE_PATH)
            }
        }
    }

    if (selectedImageUri == null) {
        SelectImageScreen {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }
    } else
        if (uploadedImageUrl == null) {
        UploadImageScreen(SCREENSHOT_FILE_PATH) { url ->
            uploadedImageUrl = url
            googleSearchUrl = performGoogleSearch(url)
        }
    } else if (googleSearchUrl != null) {
        GoogleSearchScreen(googleSearchUrl!!) {
            selectedImageUri = null
            uploadedImageUrl = null
            googleSearchUrl = null
        }
    }
}

@Composable
fun SelectImageScreen(onPickImageClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Button(onClick = onPickImageClick) {
                Text("Pick Image from Gallery")
            }
        }
    }
}

fun saveImageToFilePath(context: Context, imageUri: Uri, filePath: String) {
    try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
        val file = File(filePath)
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        outputStream.close()
        inputStream?.close()
    } catch (e: Exception) {
        Log.e("SaveImage", "Image save failed", e)
    }
}

@Composable
fun UploadImageScreen(imagePath: String, onUploadComplete: (String) -> Unit) {
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(imagePath) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient().newBuilder().build()
                val file = File(imagePath)

                val mediaType = "application/octet-stream".toMediaTypeOrNull()
                val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("image", file.name, file.asRequestBody(mediaType))
                    .build()

                val request = Request.Builder()
                    .url("https://api.imgbb.com/1/upload?expiration=6000&key=$IMGBB_API_KEY")
                    .method("POST", requestBody)
                    .build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                val imageUrl = responseBody?.let { parseImageUrl(it) }
                imageUrl?.let { onUploadComplete(it) }
            } catch (e: Exception) {
                Log.e("UploadImage", "Image upload failed", e)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text("Uploading...")
        }
    }
}

@Composable
fun GoogleSearchScreen(url: String, onBackClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Button(onClick = onBackClick, modifier = Modifier.padding(16.dp)) {
                Text("Back")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading...")
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                request?.url?.let { view?.loadUrl(it.toString(), getHeaders()) }
                                return true
                            }
                        }
                        loadUrl(url, getHeaders())
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

fun parseImageUrl(responseBody: String): String {
    val jsonObject = JSONObject(responseBody)
    val dataObject = jsonObject.getJSONObject("data")
    return dataObject.getString("url")
}

fun performGoogleSearch(imageUrl: String): String {
    return "https://lens.google.com/uploadbyurl?url=$imageUrl&hl=en-IN&re=df&st=1719450563620&vpw=372&vph=983&ep=gsbubu"
}

fun getHeaders(): Map<String, String> {
    return mapOf(
        "accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
        "accept-language" to "en-US,en;q=0.9",
        "priority" to "u=0, i",
        "referer" to "https://www.google.com/",
        "sec-ch-ua" to "\"Not/A)Brand\";v=\"8\", \"Chromium\";v=\"126\", \"Google Chrome\";v=\"126\"",
        "sec-ch-ua-arch" to "\"x86\"",
        "sec-ch-ua-bitness" to "\"64\"",
        "sec-ch-ua-form-factors" to "\"Desktop\"",
        "sec-ch-ua-full-version" to "\"126.0.6478.127\"",
        "sec-ch-ua-full-version-list" to "\"Not/A)Brand\";v=\"8.0.0.0\", \"Chromium\";v=\"126.0.6478.127\", \"Google Chrome\";v=\"126.0.6478.127\"",
        "sec-ch-ua-mobile" to "?0",
        "sec-ch-ua-model" to "\"\"",
        "sec-ch-ua-platform" to "\"Windows\"",
        "sec-ch-ua-platform-version" to "\"15.0.0\"",
        "sec-ch-ua-wow64" to "?0",
        "sec-fetch-dest" to "document",
        "sec-fetch-mode" to "navigate",
        "sec-fetch-site" to "same-site",
        "sec-fetch-user" to "?1",
        "upgrade-insecure-requests" to "1",
        "user-agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
    )
}