package com.commu.luklan.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commu.luklan.ui.theme.LuklanColors
import org.jetbrains.compose.resources.painterResource
import luklan.composeapp.generated.resources.Res
import luklan.composeapp.generated.resources.*

@Composable
fun MedicineIcon(
    category: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 45.dp
) {
    val resource = when (category) {
        "แคปซูล" -> Res.drawable.capsule
        "เม็ด" -> Res.drawable.pill
        "น้ำ", "ยาน้ำ" -> Res.drawable.liquid
        "ครีม" -> Res.drawable.cream
        "เหน็บ" -> Res.drawable.suppository
        "ฉีด" -> Res.drawable.inject
        "อื่นๆ" -> Res.drawable.other
        else -> null
    }

    if (resource != null) {
        if (category == "น้ำ" || category == "ยาน้ำ" || category == "ครีม") {
            Box(contentAlignment = Alignment.Center, modifier = modifier) {
                // Outline using Secondary color
                listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1).forEach { (dx, dy) ->
                    Image(
                        painter = painterResource(resource),
                        contentDescription = null,
                        modifier = Modifier.size(iconSize).offset(dx.dp, dy.dp),
                        colorFilter = ColorFilter.tint(LuklanColors.Secondary)
                    )
                }
                Image(
                    painter = painterResource(resource),
                    contentDescription = null,
                    modifier = Modifier.size(iconSize)
                )
            }
        } else {
            Image(
                painter = painterResource(resource),
                contentDescription = null,
                modifier = modifier.size(iconSize)
            )
        }
    } else {
        Box(modifier = modifier.size(iconSize), contentAlignment = Alignment.Center) {
            Text(text = "💊", fontSize = (iconSize.value * 0.8).sp)
        }
    }
}
