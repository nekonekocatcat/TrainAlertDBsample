package com.example.trainalertsample

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.trainalertsample.db.RouteEntity

@Composable
fun InputForm(
    initialRouteData: RouteEntity?,
    onSave: (String, Double, Double, Double, Double, List<String>) -> Unit,
    onNavigateToList: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var startLongitude by remember { mutableStateOf("") }
    var startLatitude by remember { mutableStateOf("") }
    var endLongitude by remember { mutableStateOf("") }
    var endLatitude by remember { mutableStateOf("") }

    var isNotificationEnabled by remember { mutableStateOf(false) }
    var isVibrationEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(initialRouteData) {
        title = initialRouteData?.title ?: ""
        startLongitude = initialRouteData?.startLongitude?.toString() ?: ""
        startLatitude = initialRouteData?.startLatitude?.toString() ?: ""
        endLongitude = initialRouteData?.endLongitude?.toString() ?: ""
        endLatitude = initialRouteData?.endLatitude?.toString() ?: ""
        isNotificationEnabled = initialRouteData?.alertMethods?.contains("通知") == true
        isVibrationEnabled = initialRouteData?.alertMethods?.contains("バイブレーション") == true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // フォームのUIコンポーネントを設定
        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("ルート名") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = startLongitude,
            onValueChange = { startLongitude = it },
            label = { Text("出発地点の経度") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = startLatitude,
            onValueChange = { startLatitude = it },
            label = { Text("出発地点の緯度") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = endLongitude,
            onValueChange = { endLongitude = it },
            label = { Text("到着地点の経度") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = endLatitude,
            onValueChange = { endLatitude = it },
            label = { Text("到着地点の緯度") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Checkbox(
                checked = isNotificationEnabled,
                onCheckedChange = { isNotificationEnabled = it }
            )
            Text("通知")
            Spacer(modifier = Modifier.width(16.dp))
            Checkbox(
                checked = isVibrationEnabled,
                onCheckedChange = { isVibrationEnabled = it }
            )
            Text("バイブレーション")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val startLon = startLongitude.toDoubleOrNull() ?: 0.0
                val startLat = startLatitude.toDoubleOrNull() ?: 0.0
                val endLon = endLongitude.toDoubleOrNull() ?: 0.0
                val endLat = endLatitude.toDoubleOrNull() ?: 0.0

                val alertMethods = mutableListOf<String>()
                if (isNotificationEnabled) alertMethods.add("通知")
                if (isVibrationEnabled) alertMethods.add("バイブレーション")

                onSave(title, startLon, startLat, endLon, endLat, alertMethods)
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("保存")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                onNavigateToList() // ルート一覧へ遷移
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("ルート一覧")
        }
    }
}
