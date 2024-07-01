package com.techcravers.cropingimage

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import android.view.ViewGroup
import okhttp3.internal.addHeaderLenient

@Composable
fun GoogleImageSearchWebView() {
    val context = LocalContext.current

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.apply {
                    javaScriptEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    builtInZoomControls = true
                    displayZoomControls = false
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        // Simulate user actions here
                        view?.evaluateJavascript("""
                            (function() {
                                document.querySelector('div.nDcEnd > svg').click();
                                setTimeout(function() {
                                    document.querySelector('div.M8H8pb input').value = 'https://i.ibb.co/Fw0LhLy/Screenshot-2.png';
                                    document.querySelector('div.PXT6cd > div').click();
                                }, 1000);
                            })();
                        """.trimIndent(), null)
                    }
                }
                loadUrl("https://www.google.in/")
            }
        },
        update = {
            // Update WebView settings if needed
        }
    )
}

@Preview(showBackground = true)
@Composable
fun GoogleImageSearchWebViewPreview() {
    GoogleImageSearchWebView()
}