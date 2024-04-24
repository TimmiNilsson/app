package com.example.newpldh

import android.content.Context
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.os.Bundle
import android.provider.ContactsContract.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.newpldh.ui.theme.FWAppDarkGreen
import com.example.newpldh.ui.theme.FWAppDarkGrey
import com.example.newpldh.ui.theme.FWAppGreen
import com.example.newpldh.ui.theme.FWAppLightGreen
import com.example.newpldh.ui.theme.FWAppLightGrey
import com.example.newpldh.ui.theme.NewPldhTheme
import com.example.newpldh.ui.theme.interFamily
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.MalformedURLException
import java.net.URL


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            NewPldhTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    Greeting(navController)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Greeting(navController: NavHostController) {
    Box {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        var ip by remember { mutableStateOf("10.0.2.2:5000") }

        val destinations = listOf(
            "Home" to painterResource(id = R.drawable.home),
            "Gallery" to painterResource(id = R.drawable.gallery),
            "Live feed" to painterResource(id = R.drawable.live),
            "Settings" to painterResource(id = R.drawable.settings)
        )

        val selectedItem = remember { mutableStateOf(destinations[0].first) }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(color = FWAppGreen)
                    ) {
                        Text(
                            text = "Menu",
                            modifier = Modifier
                                .padding(28.dp)
                                .align(Alignment.CenterStart),
                            fontSize = 20.sp,
                            fontFamily = interFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    destinations.forEach { (destination, icon) ->
                        NavigationDrawerItem(
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = FWAppLightGreen.copy(alpha = 0.3f),
                                unselectedBadgeColor = Color.White,
                            ),
                            icon = { Icon(icon, contentDescription = null) },
                            label = { Text(destination) },
                            selected = destination == selectedItem.value,
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    selectedItem.value = destination
                                    // Navigate to the selected destination
                                    navController.navigate(destination)
                                }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            },
            content = {
                NavHost(navController = navController, startDestination = "Home") {
                    composable("Home") {
                        Home(drawerState, scope)
                    }
                    composable("Gallery") {
                        gallery(drawerState, scope, ip)
                    }
                    composable("Live feed") {
                        liveFeed()
                    }
                    composable("Settings") {
                        ip = settings(drawerState, scope, ip)
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(drawerState: DrawerState, scope: CoroutineScope) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = FWAppLightGrey)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp) // Adjust the height as needed
                .background(color = FWAppGreen)
        ) {
            Text(
                text = "Fox and Wolf App",
                modifier = Modifier.align(Alignment.Center),
                fontSize = 20.sp,
                fontFamily = interFamily,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            IconButton(
                onClick = { scope.launch { drawerState.open() } },
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = 18.dp) // Adjust the offset as needed to center vertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.bars),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = Color.Unspecified
                )
            }
        }
        Box(
            modifier = Modifier
                .width(330.dp)
                .height(140.dp)
                .clip(shape = RoundedCornerShape(50.dp))
                .background(FWAppDarkGrey)
                .align(Alignment.Center)
        ) {
            var text by remember { mutableStateOf("") }

            LaunchedEffect(Unit) {
                val isConnected = withContext(Dispatchers.IO) {
                    try {
                        InetAddress.getByName("http://10.0.2.2:5000/").isReachable(1000)
                    } catch (e: IOException) {
                        false
                    }
                }
                text = if (isConnected) "Connected" else "NOT CONNECTED"
            }


            Text(
                text = text,
                modifier = Modifier.align(Alignment.Center),
                fontSize = 20.sp,
                fontFamily = interFamily,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun gallery(drawerState: DrawerState, scope: CoroutineScope, ip: String) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(FWAppGreen)
        ) {
            Text(
                text = "Gallery",
                modifier = Modifier
                    .align(Alignment.Center),
                fontSize = 20.sp,
                fontFamily = interFamily, fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            IconButton(
                onClick = { scope.launch { drawerState.open() } },
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = 18.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.bars),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = Color.Unspecified
                )
            }
        }
        UploadedImagesGrid(modifier = Modifier, ip)
    }

}

sealed class ImageResult {
    data class Success(val bitmap: android.graphics.Bitmap) : ImageResult()
    object Error : ImageResult()
}

@Composable
fun UploadedImagesGrid(modifier: Modifier, ip: String) {
    val context = LocalContext.current
    var imageUrls by remember { mutableStateOf(emptyList<String>()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                //val ip = "http://10.0.2.2:5000/"
                val url = URL("http://" + ip + "/uploaded_images")
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()
                val inputStream = connection.inputStream
                val response = inputStream.bufferedReader().use { it.readText() }
                val filenames = JSONObject(response).getJSONArray("uploaded_files")
                val list = mutableListOf<String>()
                for (i in 0 until filenames.length()) {
                    list.add("http://" + ip + "/uploads/" + filenames.getString(i))
                }
                imageUrls = list
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.fillMaxSize()) {
        items(imageUrls) { imageUrl ->
            LoadImageFromUrl(imageUrl)
        }
    }
}

@Composable
private fun LoadImageFromUrl(imageUrl: String) {
    val context = LocalContext.current
    var imageResult by remember { mutableStateOf<ImageResult?>(null) }

    LaunchedEffect(imageUrl) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()
                val inputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    imageResult = ImageResult.Success(bitmap)
                } else {
                    imageResult = ImageResult.Error
                }
            } catch (e: IOException) {
                e.printStackTrace()
                imageResult = ImageResult.Error
            }
        }
    }

    when (val result = imageResult) {
        is ImageResult.Success -> {
            Image(
                bitmap = result.bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .padding(4.dp),
                contentScale = ContentScale.Crop
            )
        }
        ImageResult.Error -> {
            // Handle error - Show placeholder image or error message
            Image(
                painter = painterResource(id = R.drawable.error),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .padding(4.dp),
                contentScale = ContentScale.Crop
            )
        }
        null -> {
            // Loading state - Show loading indicator
            Box(modifier = Modifier.size(120.dp)){
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .then(Modifier.size(64.dp)),
                    color = FWAppGreen,

                    )
            }
        }
    }
}




@Composable
fun liveFeed() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FWAppLightGrey)
    ){
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp) // Adjust the height as needed
                .background(FWAppGreen)
        ){
            Text(
                text = "Live Feed",
                modifier = Modifier
                    .align(Alignment.Center),
                fontSize = 20.sp,
                fontFamily = interFamily, fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun settingsPreviw(){
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    settings(drawerState, scope, "10.0.2.2:5000")
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun settings(drawerState: DrawerState, scope: CoroutineScope, ip: String): String {
    var text by remember { mutableStateOf(ip) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = FWAppLightGrey)
    ) {
    Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(FWAppGreen)
            ) {
                Text(
                    text = "Settings",
                    modifier = Modifier
                        .align(Alignment.Center),
                    fontSize = 20.sp,
                    fontFamily = interFamily, fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                IconButton(
                    onClick = { scope.launch { drawerState.open() } },
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterStart)
                        .offset(x = 18.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.bars),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        tint = Color.Unspecified
                    )

                }

            }
            OutlinedTextField(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                value = text,
                singleLine = true,
                onValueChange = { text = it },
                label = {Text("Ip address")},
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = FWAppGreen,
                    unfocusedBorderColor = FWAppDarkGreen)
            )

        }

    }
    return text
}






