package com.example.fetchapp

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FetchAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column {
                        ItemsList(
                            myItems = testItems,
                            pad = innerPadding
                        )
                    }

                }
            }
        }
    }
}

//Sorts list based on listId and name
fun sortAndFilterList(itemsList: List<Item>): List<Item>{
    val filteredList: List<Item> = itemsList.filter {!it.name.isNullOrBlank()}
    val sortedList: List<Item> = filteredList.sortedWith(compareBy<Item> {it.listId}.thenBy{it.name})
    return sortedList
}


val testItems = listOf(
    Item(2, "Asteroid"),
    Item(1, "Banana"),
    Item(1, ""),
    Item(3, "Peach"),
    Item(1, "Apple"),
    Item(2, "Pear"),
    Item(3, "Mango")
)

@Composable
fun ItemsList(myItems: List<Item>, pad:PaddingValues){
    val sortedList: List<Item> = sortAndFilterList(myItems)
//    var currList = mutableListOf<Item>()
    var currListId: Int? = null
    LazyColumn (
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(pad)
    ){
        items(sortedList.size){
            i ->
            if(currListId != sortedList[i].listId){
                currListId = sortedList[i].listId
                Row (modifier =  Modifier.fillMaxWidth()){
                    Text(
                        text = "List Id: $currListId",
                        modifier = Modifier.padding(start = 12.dp, top = 12.dp).weight(1f)
                    )
                }
            }
            Text(
                text = "${sortedList[i].name}"
            )

        }
    }
}

data class Item(
    val listId: Int,
    val name: String?
)

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FetchAppTheme {
        ItemsList(testItems, PaddingValues(16.dp))
    }
}