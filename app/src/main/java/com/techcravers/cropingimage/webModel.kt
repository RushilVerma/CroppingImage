import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WebViewViewModel : ViewModel() {
    private val _url = MutableLiveData("https://www.google.com/")
    val url: LiveData<String> = _url

    fun updateUrl(newUrl: String) {
        _url.value = newUrl
    }
}
