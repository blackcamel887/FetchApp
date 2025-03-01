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
/**
 * ViewModel responsible for managing and fetching the list of items from the API.
 * Variable items is used as state to hold fetched information
 */
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
/**
 * Retrofit interface defining API endpoints for fetching data.
 */
interface ApiService {
    @GET("hiring.json")
    suspend fun fetchItems(): List<Item>
}


/**
 * Singleton object responsible for creating and managing the Retrofit instance.
 * This ensures that only one instance of Retrofit is created and used throughout the app.
 */
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

/**
 * Sorts list based on listId and name
 */
fun sortAndFilterList(itemsList: List<Item>): List<Item>{
    val filteredList: List<Item> = itemsList.filter {!it.name.isNullOrBlank()}
    val sortedList: List<Item> = filteredList
        // This code sorts by value in name: Item {value}
        // If sorting by name desired remove ?.substring(5)?.toIntOrNull()
        .sortedWith(compareBy<Item> {it.listId}.thenBy{it.name?.substring(5)?.toIntOrNull()})
    return sortedList
}

/**
 * Displays the list fetched from https://fetch-hiring.s3.amazonaws.com/hiring.json
 * in filtered (blank/null names) and sorted order.
 */
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

/**
 * Data type for the object from API fetch.
 */
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