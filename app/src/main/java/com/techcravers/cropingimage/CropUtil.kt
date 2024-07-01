package com.techcravers.cropingimage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.InputStream

@Composable
fun ImageCropperApp() {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var croppedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current
    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
        }
    }

    var startX by remember { mutableStateOf(100f) }
    var startY by remember { mutableStateOf(100f) }
    var endX by remember { mutableStateOf(300f) }
    var endY by remember { mutableStateOf(300f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        selectedImageUri?.let { uri ->
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .background(Color.Gray)
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
                DraggableResizableBox(
                    modifier = Modifier.fillMaxSize(),
                    initialStartX = startX,
                    initialStartY = startY,
                    initialEndX = endX,
                    initialEndY = endY
                ) { newStartX, newStartY, newEndX, newEndY ->
                    startX = newStartX
                    startY = newStartY
                    endX = newEndX
                    endY = newEndY
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            selectedImageUri?.let { uri ->
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                croppedImageBitmap = cropImage(bitmap, startX, startY, endX, endY)
            }
        }) {
            Text(text = "Crop Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        croppedImageBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { pickImageLauncher.launch("image/*") }) {
            Text(text = "Select Image")
        }
    }
}

fun cropImage(bitmap: Bitmap, startX: Float, startY: Float, endX: Float, endY: Float): Bitmap {
    val x = startX.toInt().coerceIn(0, bitmap.width - 1)
    val y = startY.toInt().coerceIn(0, bitmap.height - 1)
    val width = (endX - startX).toInt().coerceIn(1, bitmap.width - x)
    val height = (endY - startY).toInt().coerceIn(1, bitmap.height - y)
    return Bitmap.createBitmap(bitmap, x, y, width, height)
}