package com.example.fetchapp
import com.google.gson.annotations.SerializedName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.fetchapp.ui.theme.FetchAppTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FetchAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column {
                        ItemsList(
                            pad = innerPadding
                        )
                    }

                }
            }
        }
    }
}

class MyViewModel : ViewModel() {
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items
    fun getItems(){
        viewModelScope.launch {
            try{
                val retrievedItems = RetrofitClient.api.fetchItems()
                _items.value = retrievedItems
                println("Get Items succeeded")
            }
            catch (e: Exception){
                println("Fetch error: ${e.message}")
            }
        }
    }
}

interface ApiService {
    @GET("hiring.json")
    suspend fun fetchItems(): List<Item>
}

object RetrofitClient{
    private const val FETCH_URL = "https://fetch-hiring.s3.amazonaws.com/"
    val api: ApiService by lazy{
        Retrofit.Builder()
            .baseUrl(FETCH_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

//Sorts list based on listId and name
fun sortAndFilterList(itemsList: List<Item>): List<Item>{
    val filteredList: List<Item> = itemsList.filter {!it.name.isNullOrBlank()}
    val sortedList: List<Item> = filteredList.sortedWith(compareBy<Item> {it.listId}.thenBy{it.name})
    return sortedList
}

@Composable
fun ItemsList(pad:PaddingValues){
    val viewModel: MyViewModel = viewModel()
    val myItems = viewModel.items.collectAsState().value
    LaunchedEffect(Unit) {
        viewModel.getItems()
    }
    val sortedList: List<Item> = sortAndFilterList(myItems)
    LazyColumn (
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(pad)
    ){
        sortedList.groupBy { it.listId }.forEach{
            (listId, items) ->
            item{
                Row (modifier =  Modifier.fillMaxWidth()){
                    Text(
                        text = "List Id: $listId",
                        modifier = Modifier
                            .padding(start = 12.dp, top = 12.dp)
                            .weight(1f)
                    )
                }
            }
            items(items.size) {
                i ->  Text(
                    text = "${items[i].name}"
                )
            }
        }
    }
}

data class Item(
    @SerializedName("listId") val listId: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("id") val id: Int
)

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FetchAppTheme {
        ItemsList(PaddingValues(16.dp))
    }
}