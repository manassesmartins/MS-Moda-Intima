package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.example.data.*
import com.example.ui.theme.*
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun TransactionListItem(
    item: TransactionEntity,
    onItemClick: () -> Unit
) {
    val Primary = MaterialTheme.colorScheme.primary
    val OnPrimary = MaterialTheme.colorScheme.onPrimary
    val Secondary = MaterialTheme.colorScheme.secondary
    val OnSecondary = MaterialTheme.colorScheme.onSecondary
    val Tertiary = MaterialTheme.colorScheme.tertiary
    val OnTertiary = MaterialTheme.colorScheme.onTertiary
    val OnSurface = MaterialTheme.colorScheme.onSurface
    val OnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val SurfaceContainer = MaterialTheme.colorScheme.surfaceVariant
    val SurfaceContainerHigh = MaterialTheme.colorScheme.surfaceVariant
    val SurfaceDark = MaterialTheme.colorScheme.background
    val ErrorColor = MaterialTheme.colorScheme.error

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("transaction_item_${item.id}")
            .clickable { onItemClick() }
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = getGlassContainerColor()
            ),
            border = getGlassBorderStroke(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left & Middle Content (wrapped in a row with weight(1f) to restrict its width)
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Category Emblem
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = if (item.type == "INFLOW") Tertiary.copy(alpha = 0.1f) else ErrorColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .border(
                                1.dp,
                                if (item.type == "INFLOW") Tertiary.copy(alpha = 0.2f) else ErrorColor.copy(alpha = 0.2f),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (item.type == "INFLOW") Icons.Default.Check else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (item.type == "INFLOW") Tertiary else ErrorColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Text Details Column (weight(1f) avoids pushing other elements)
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = item.description,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (item.extraText.isNotEmpty()) {
                                Text(
                                    text = item.extraText,
                                    fontSize = 12.sp,
                                    color = OnSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Text(
                                    text = "•",
                                    fontSize = 12.sp,
                                    color = OnSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                            Text(
                                text = item.dateString,
                                fontSize = 11.sp,
                                color = OnSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Right Content (Amount and Week badge)
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.wrapContentWidth(Alignment.End)
                ) {
                    val sign = if (item.type == "INFLOW") "+" else "-"
                    val color = if (item.type == "INFLOW") Tertiary else ErrorColor
                    Text(
                        text = String.format(Locale("pt", "BR"), "%s R$ %,.2f", sign, item.amount),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.week.uppercase(Locale.getDefault()),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionGroupedRow(
    item: TransactionEntity,
    onItemClick: () -> Unit
) {
    val Tertiary = MaterialTheme.colorScheme.tertiary
    val OnSurface = MaterialTheme.colorScheme.onSurface
    val OnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val ErrorColor = MaterialTheme.colorScheme.error

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(vertical = 12.dp, horizontal = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category Emblem
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (item.type == "INFLOW") Tertiary.copy(alpha = 0.1f) else ErrorColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .border(
                        1.dp,
                        if (item.type == "INFLOW") Tertiary.copy(alpha = 0.2f) else ErrorColor.copy(alpha = 0.2f),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.type == "INFLOW") Icons.Default.Check else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = if (item.type == "INFLOW") Tertiary else ErrorColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Description and custom extra text
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.description,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (item.extraText.isNotEmpty()) {
                        Text(
                            text = item.extraText,
                            fontSize = 11.sp,
                            color = OnSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Text(
                            text = "•",
                            fontSize = 11.sp,
                            color = OnSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    Text(
                        text = item.dateString,
                        fontSize = 10.sp,
                        color = OnSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Amount and sign
        val sign = if (item.type == "INFLOW") "+" else "-"
        val color = if (item.type == "INFLOW") Tertiary else ErrorColor
        Text(
            text = String.format(Locale("pt", "BR"), "%s R$ %,.2f", sign, item.amount),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun TransactionsScreen(
    viewModel: TransactionViewModel,
    transactions: List<TransactionEntity>,
    activeFilter: String,
    onFilterChanged: (String) -> Unit,
    onItemClick: (TransactionEntity) -> Unit
) {
    val Primary = MaterialTheme.colorScheme.primary
    val OnPrimary = MaterialTheme.colorScheme.onPrimary
    val Secondary = MaterialTheme.colorScheme.secondary
    val OnSecondary = MaterialTheme.colorScheme.onSecondary
    val Tertiary = MaterialTheme.colorScheme.tertiary
    val OnTertiary = MaterialTheme.colorScheme.onTertiary
    val OnSurface = MaterialTheme.colorScheme.onSurface
    val OnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val SurfaceContainer = MaterialTheme.colorScheme.surfaceVariant
    val SurfaceContainerHigh = MaterialTheme.colorScheme.surfaceVariant
    val SurfaceDark = MaterialTheme.colorScheme.background
    val ErrorColor = MaterialTheme.colorScheme.error

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Gastos",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Primary
        )
        Text(
            text = "Controle de despesas, custos e saídas gerais",
            fontSize = 13.sp,
            color = OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Large Horizontal Scrolling Filter Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val filters = listOf("SAIDAS" to "Custos / Saídas")
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { (key, display) ->
                    val isSelected = activeFilter == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(32.dp))
                            .background(
                                if (isSelected) Primary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)
                            )
                            .border(
                                1.dp,
                                if (isSelected) Primary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(32.dp)
                            )
                            .clickable { onFilterChanged(key) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("filter_${key.lowercase()}")
                    ) {
                        Text(
                            text = display,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Primary else OnSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Scrolling lists
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhum lançamento corresponde ao filtro.",
                            color = OnSurfaceVariant,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                val groupedByWeek = transactions.groupBy { it.week }
                groupedByWeek.forEach { (weekName, weekTransactions) ->
                    item(key = "header_$weekName") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = weekName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        }
                    }

                    item(key = "card_$weekName") {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = getGlassContainerColor()
                            ),
                            border = getGlassBorderStroke(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                weekTransactions.forEachIndexed { index, item ->
                                    TransactionGroupedRow(
                                        item = item,
                                        onItemClick = { onItemClick(item) }
                                    )
                                    if (index < weekTransactions.lastIndex) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 14.dp),
                                            color = OnSurfaceVariant.copy(alpha = 0.12f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
