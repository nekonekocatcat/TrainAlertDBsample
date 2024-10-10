package com.example.trainalertsample

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.trainalertsample.db.AppDatabase
import com.example.trainalertsample.db.RouteEntity
import com.example.trainalertsample.ui.theme.TrainAlertSampleTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // データベースのインスタンスを作成
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "room_database"
        ).build()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // UIをエッジまで拡張する
        setContent {
            TrainAlertSampleTheme {
                // ナビゲーションコントローラを生成
                val navController = rememberNavController()
                // ナビゲーションホストを設定
                NavHost(
                    navController = navController,
                    startDestination = "routeList" // 初期表示画面を設定
                ) {
                    composable(
                        "inputForm/{routeId}",
                        arguments = listOf(navArgument("routeId") { type = NavType.IntType })
                    ) {backStackEntry ->
                        val id = backStackEntry.arguments?.getInt("routeId")

                        InputFormScreen(navController, database , id)
                    }
                    composable("routeList") {
                        RouteListScreen(navController, database)
                    }
                }
            }
        }
    }
}


@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun InputFormScreen(
    navController: NavHostController,
    database: AppDatabase,
    routeId: Int?
) {
    var routeData by remember { mutableStateOf<RouteEntity?>(null) }
    val isEditing = routeId != -1

    // デバッグ用にログを追加して、action と routeId を確認
    Log.d("InputFormScreen", "RouteId: $routeId, IsEditing: $isEditing")

    // routeIdが渡されている場合、既存データをロード
    LaunchedEffect(routeId) {
        if (isEditing) {
            CoroutineScope(Dispatchers.IO).launch {
                val dao = database.routeDao()
                val route = dao.loadAllRoute().find { it.id == routeId }
                withContext(Dispatchers.Main) {
                    routeData = route
                    Log.d("InputFormScreen", "Loaded route data: $routeData")
                }
            }
        }
    }

    InputForm(
        initialRouteData = routeData,
        onSave = { title, startLongitude, startLatitude, endLongitude, endLatitude, alertMethods ->
            CoroutineScope(Dispatchers.IO).launch {
                val dao = database.routeDao()

                if (isEditing && routeData != null) {
                    // 編集の処理
                    Log.d("InputFormScreen", "Updating existing route ID: ${routeData!!.id}")
                    val updatedRoute = routeData!!.copy(
                        title = title,
                        startLongitude = startLongitude,
                        startLatitude = startLatitude,
                        endLongitude = endLongitude,
                        endLatitude = endLatitude,
                        alertMethods = alertMethods.joinToString(", ")
                    )
                    dao.update(updatedRoute)
                } else {
                    // 新規作成の処理
                    Log.d("InputFormScreen", "Creating new route")
                    val newRoute = RouteEntity(
                        title = title,
                        startLongitude = startLongitude,
                        startLatitude = startLatitude,
                        endLongitude = endLongitude,
                        endLatitude = endLatitude,
                        alertMethods = alertMethods.joinToString(", ")
                    )
                    dao.saveRoute(newRoute)
                }

                withContext(Dispatchers.Main) {
                    navController.navigate("routeList") {
                        popUpTo("routeList") { inclusive = true }
                    }
                }
            }
        },
        onNavigateToList = {
            navController.navigate("routeList") {
                popUpTo("routeList") { inclusive = true }
            }
        }
    )
}




@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun RouteListScreen(navController: NavHostController, database: AppDatabase) {
    val sampleList = remember { mutableStateListOf<RouteEntity>() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showActionDialog by remember { mutableStateOf(false) }
    var routeToDelete by remember { mutableStateOf<RouteEntity?>(null) }
    var routeToEdit by remember { mutableStateOf<RouteEntity?>(null) }

    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = database.routeDao()
                val routes = dao.loadAllRoute()
                withContext(Dispatchers.Main) {
                    sampleList.clear()
                    sampleList.addAll(routes)
                }
            } catch (e: Exception) {
                Log.e("RouteListScreen", "Error loading routes: ${e.message}", e)
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(sampleList) { sample ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            Log.d("RouteListScreen", "Card clicked for route ID: ${sample.id}")
                            routeToDelete = sample
                            routeToEdit = sample
                            showActionDialog = true
                        },
                    elevation = CardDefaults.elevatedCardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ルート名: ${sample.title}",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "出発地点の経度: ${sample.startLongitude}")
                        Text(text = "出発地点の緯度: ${sample.startLatitude}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "到着地点の経度: ${sample.endLongitude}")
                        Text(text = "到着地点の緯度: ${sample.endLatitude}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "アラート方法: ${sample.alertMethods}")
                    }
                }
            }
        }

        // 編集画面に遷移する際の処理
        if (showActionDialog) {
            AlertDialog(
                onDismissRequest = { showActionDialog = false },
                title = { Text("ルートの操作") },
                text = { Text("このルートを編集しますか、それとも削除しますか？") },
                confirmButton = {
                    Button(
                        onClick = {
                            routeToEdit?.let { route ->
                                Log.d("RouteListScreen", "Navigating to edit route with ID: ${route.id}")
                                // 遷移時にactionとrouteIdを付与
                                navController.navigate("inputForm/${route.id}")
                            }
                            showActionDialog = false
                        }
                    ) {
                        Text("編集")
                    }

                },
                dismissButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = true
                            showActionDialog = false
                        }
                    ) {
                        Text("削除")
                    }
                }
            )
        }


        // 新規入力画面に遷移するボタン
        Button(
            onClick = {
                // 新規作成用のナビゲーションパラメータとして "new" を追加
                navController.navigate("inputForm/-1")
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 16.dp)
        ) {
            Text("ルートを入力する")
        }


        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                text = { Text("このルートを削除しますか？") },
                confirmButton = {
                    Button(
                        onClick = {
                            routeToDelete?.let { route ->
                                Log.d("RouteListScreen", "Deleting route ID: ${route.id}")
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val dao = database.routeDao()
                                        dao.deleteRoute(route)
                                        withContext(Dispatchers.Main) {
                                            sampleList.remove(route)
                                        }
                                    } catch (e: Exception) {
                                        Log.e(
                                            "RouteListScreen",
                                            "Error deleting route: ${e.message}",
                                            e
                                        )
                                    }
                                }
                            }
                            showDeleteDialog = false
                        }
                    ) {
                        Text("削除")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDeleteDialog = false }) {
                        Text("キャンセル")
                    }
                }
            )
        }
    }
}
