package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.database.CartEntity
import com.example.data.database.OrderEntity
import com.example.data.model.Category
import com.example.data.model.MenuItem
import com.example.viewmodel.FoodViewModel
import java.text.SimpleDateFormat
import java.util.*

// Brand Color Palette (Warm Sunset / Orchard)
val ThemeBgColor = Color(0xFFFCF9F4) // Soft Warm Cream
val PrimaryOrange = Color(0xFFE65100) // Deep Appetizing Orange
val HighlightGold = Color(0xFFFF9100) // Vibrant Amber Gold
val AccentGreen = Color(0xFF4CAF50) // Fresh Basil Green
val DarkText = Color(0xFF332A25) // Deep Roasted Cocoa
val MutedText = Color(0xFF6D635D) // Soft Charcoal Gray
val LightCardColor = Color(0xFFFFFFFF) // Premium White
val BorderColor = Color(0xFFEFE9DF) // Tender Sand

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodOrderApp(viewModel: FoodViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val trackedOrder by viewModel.trackedOrder.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val cartCount by viewModel.cartCount.collectAsState()
    val cartTotal by viewModel.cartTotal.collectAsState()

    var showCartSheet by remember { mutableStateOf(false) }
    var itemToCustomize by remember { mutableStateOf<MenuItem?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.LocalDining,
                                contentDescription = null,
                                tint = PrimaryOrange,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "隨心點",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = DarkText
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "EasyOrder",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Light
                                ),
                                color = MutedText
                            )
                        }
                        Text(
                            "今日新鮮手作點心、懷舊美味便當",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ThemeBgColor,
                    scrolledContainerColor = ThemeBgColor
                ),
                actions = {
                    // Small Restaurant Hours Badge
                    Surface(
                        color = PrimaryOrange.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(AccentGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "營業中",
                                color = PrimaryOrange,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = LightCardColor,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { viewModel.setTab(0) },
                    icon = { Icon(Icons.Filled.Restaurant, contentDescription = "點餐") },
                    label = { Text("美味點餐") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryOrange,
                        selectedTextColor = PrimaryOrange,
                        indicatorColor = PrimaryOrange.copy(alpha = 0.1f),
                        unselectedIconColor = MutedText,
                        unselectedTextColor = MutedText
                    ),
                    modifier = Modifier.testTag("tab_order")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { viewModel.setTab(1) },
                    icon = { Icon(Icons.Filled.History, contentDescription = "歷史訂單") },
                    label = { Text("訂單紀錄") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryOrange,
                        selectedTextColor = PrimaryOrange,
                        indicatorColor = PrimaryOrange.copy(alpha = 0.1f),
                        unselectedIconColor = MutedText,
                        unselectedTextColor = MutedText
                    ),
                    modifier = Modifier.testTag("tab_history")
                )
            }
        },
        containerColor = ThemeBgColor
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main content based on Tab Selection
            Crossfade(
                targetState = currentTab,
                animationSpec = tween(durationMillis = 250),
                label = "TabTransition"
            ) { tab ->
                when (tab) {
                    0 -> MenuTab(
                        viewModel = viewModel,
                        onMenuItemClick = { itemToCustomize = it }
                    )
                    1 -> HistoryTab(viewModel = viewModel)
                }
            }

            // Pinned Active Tracker (Floating banner showing progress)
            AnimatedVisibility(
                visible = trackedOrder != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(8.dp)
            ) {
                trackedOrder?.let { order ->
                    ActiveTrackerCard(
                        order = order,
                        onDismiss = { viewModel.dismissTracking() }
                    )
                }
            }

            // Floating Cart Indicator FAB
            AnimatedVisibility(
                visible = cartCount > 0 && currentTab == 0 && !showCartSheet,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                FloatingActionButton(
                    onClick = { showCartSheet = true },
                    containerColor = PrimaryOrange,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    elevation = FloatingActionButtonDefaults.elevation(8.dp),
                    modifier = Modifier.testTag("cart_open_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = "查看購物車"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "已點 $cartCount 份",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "$$cartTotal",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold)
                            )
                        }
                    }
                }
            }

            // Customization Dialog
            itemToCustomize?.let { item ->
                CustomizeDialog(
                    menuItem = item,
                    onDismiss = { itemToCustomize = null },
                    onConfirm = { qty, ice, sugar, note ->
                        viewModel.addToCart(item, qty, ice, sugar, note)
                        itemToCustomize = null
                    }
                )
            }

            // Full Cart Overlay Sheet
            if (showCartSheet) {
                CartSheet(
                    viewModel = viewModel,
                    cartItems = cartItems,
                    totalPrice = cartTotal,
                    onDismiss = { showCartSheet = false }
                )
            }
        }
    }
}

// ==========================================
// MENU TAB VIEW
// ==========================================
@Composable
fun MenuTab(
    viewModel: FoodViewModel,
    onMenuItemClick: (MenuItem) -> Unit
) {
    val filteredItems by viewModel.filteredMenuItems.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Banner
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("搜尋香脆排骨、招牌珍珠鮮奶茶...", color = MutedText) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = PrimaryOrange) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Filled.Close, contentDescription = "清除搜尋")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .testTag("search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LightCardColor,
                unfocusedContainerColor = LightCardColor,
                focusedBorderColor = PrimaryOrange,
                unfocusedBorderColor = BorderColor,
                cursorColor = PrimaryOrange
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        // Category Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // "ALL" Category
            item {
                CategoryPill(
                    name = "全部餐點",
                    isSelected = selectedCategory == null,
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.selectCategory(null)
                    }
                )
            }
            // List Categories
            items(Category.values()) { category ->
                CategoryPill(
                    name = category.displayName,
                    isSelected = selectedCategory == category,
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.selectCategory(category)
                    }
                )
            }
        }

        // Menu Items List
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = MutedText.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("找不到相關美食項目", color = MutedText, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    MenuItemCard(
                        item = item,
                        onClick = {
                            focusManager.clearFocus()
                            onMenuItemClick(item)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryPill(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PrimaryOrange else LightCardColor,
        border = BorderStroke(1.dp, if (isSelected) PrimaryOrange else BorderColor),
        shadowElevation = if (isSelected) 4.dp else 1.dp
    ) {
        Text(
            text = name,
            color = if (isSelected) Color.White else DarkText,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun MenuItemCard(
    item: MenuItem,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = LightCardColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food Image (Coil AsyncImage with graceful fallback)
            AsyncImage(
                model = item.iconUrl,
                contentDescription = item.name,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BorderColor),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Food details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = DarkText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.badge != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = PrimaryOrange.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = item.badge,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 9.sp
                                ),
                                color = PrimaryOrange
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Spicy level icon tag
                    if (item.spicyLevel > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = "辣度",
                                tint = PrimaryOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                "辣",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryOrange
                            )
                        }
                    } else if (item.hasIceSugarCustomization) {
                        Surface(
                            color = AccentGreen.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "可客製冰糖",
                                color = AccentGreen,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    // Price & Add button
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$${item.price}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = PrimaryOrange,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = PrimaryOrange,
                            shape = CircleShape,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "選取餐點",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// HISTORY TAB VIEW
// ==========================================
@Composable
fun HistoryTab(viewModel: FoodViewModel) {
    val orders by viewModel.historyOrders.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "已放置訂單",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = DarkText
            )
            if (orders.isNotEmpty()) {
                TextButton(
                    onClick = { viewModel.clearAllHistory() },
                    colors = ButtonDefaults.textButtonColors(contentColor = PrimaryOrange)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("清除明細")
                }
            }
        }

        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.ListAlt,
                        contentDescription = null,
                        tint = MutedText.copy(alpha = 0.3f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "目前尚無排單紀錄",
                        color = MutedText,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "快去精選美味餐單點餐吧！",
                        color = MutedText.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(orders, key = { it.id }) { order ->
                    HistoryOrderCard(order = order)
                }
            }
        }
    }
}

@Composable
fun HistoryOrderCard(order: OrderEntity) {
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }
    val timeString = remember(order.timestamp) { sdf.format(Date(order.timestamp)) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = LightCardColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Order meta header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Table details / Type
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (order.orderType == "內用") PrimaryOrange.copy(alpha = 0.1f) else AccentGreen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = order.orderType,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (order.orderType == "內用") PrimaryOrange else AccentGreen
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        order.tableNumber,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DarkText
                    )
                }

                // Total price
                Text(
                    "$$${order.totalPrice}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = PrimaryOrange,
                        fontWeight = FontWeight.Black
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = BorderColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Sub-items summary description
            Text(
                text = order.summaryText,
                style = MaterialTheme.typography.bodyMedium,
                color = DarkText,
                lineHeight = 20.sp
            )

            if (order.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = ThemeBgColor,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "備註：${order.notes}",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText
                )

                // Current simulated state
                Surface(
                    color = when {
                        order.status.contains("已完成") -> AccentGreen.copy(alpha = 0.1f)
                        order.status.contains("烹調中") -> HighlightGold.copy(alpha = 0.1f)
                        else -> PrimaryOrange.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = when {
                            order.status.contains("已完成") -> Icons.Filled.CheckCircle
                            order.status.contains("烹調中") -> Icons.Filled.Restaurant
                            else -> Icons.Filled.AccessTime
                        }
                        val tint = when {
                            order.status.contains("已完成") -> AccentGreen
                            order.status.contains("烹調中") -> HighlightGold
                            else -> PrimaryOrange
                        }

                        Icon(icon, contentDescription = null, size = 14.dp, tint = tint)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = order.status,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = tint
                        )
                    }
                }
            }
        }
    }
}

// Ext helper function for sizing Icon
@Composable
fun Icon(imageVector: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String?, size: androidx.compose.ui.unit.Dp, tint: Color) {
    Icon(imageVector = imageVector, contentDescription = contentDescription, modifier = Modifier.size(size), tint = tint)
}


// ==========================================
// ACTIVE TRACKER CARD
// ==========================================
@Composable
fun ActiveTrackerCard(
    order: OrderEntity,
    onDismiss: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = DarkText),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocalDining,
                        contentDescription = null,
                        tint = HighlightGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "最新點單製作進度...",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "關閉", tint = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar mapping simulation
            val progress = when {
                order.status.contains("已完成") -> 1.0f
                order.status.contains("烹調中") -> 0.6f
                else -> 0.25f // 製作中
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = HighlightGold,
                trackColor = Color.White.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "目前狀態: ${order.status}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = HighlightGold
                    )
                    Text(
                        text = order.summaryText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 240.dp)
                    )
                }

                // Table info
                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = order.tableNumber,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}


// ==========================================
// CUSTOMIZE OPTIONS DIALOG (ICE / SUGAR / QTY)
// ==========================================
@Composable
fun CustomizeDialog(
    menuItem: MenuItem,
    onDismiss: () -> Unit,
    onConfirm: (qty: Int, ice: String, sugar: String, note: String) -> Unit
) {
    var quantity by remember { mutableStateOf(1) }
    var selectedIce by remember { mutableStateOf("正常冰") }
    var selectedSugar by remember { mutableStateOf("正常甜") }
    var specialNote by remember { mutableStateOf("") }

    val iceLevels = listOf("正常冰", "少冰", "微冰", "去冰")
    val sugarLevels = listOf("正常甜", "半糖", "微糖", "無糖")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = LightCardColor,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with Image background
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = menuItem.iconUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BorderColor),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = menuItem.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = DarkText
                        )
                        Text(
                            text = "$${menuItem.price}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = BorderColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Drink customizability options
                if (menuItem.hasIceSugarCustomization) {
                    Text(
                        "溫度冰量選擇",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = DarkText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowSelector(
                        options = iceLevels,
                        selectedOption = selectedIce,
                        onOptionSelected = { selectedIce = it }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        "調味糖度選擇",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = DarkText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowSelector(
                        options = sugarLevels,
                        selectedOption = selectedSugar,
                        onOptionSelected = { selectedSugar = it }
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Special Notes Text Field
                Text(
                    "客製化備註 (例如：不要香菜、多辣)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = DarkText
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = specialNote,
                    onValueChange = { specialNote = it },
                    placeholder = { Text("選填：特殊備註需求...", color = MutedText.copy(alpha = 0.6f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryOrange,
                        unfocusedBorderColor = BorderColor,
                        cursorColor = PrimaryOrange
                    ),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quantity selector & Total Calculate
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "點購數量",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = DarkText
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Minus
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { if (quantity > 1) quantity-- },
                            shape = CircleShape,
                            color = BorderColor
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Remove, contentDescription = "減少", tint = DarkText)
                            }
                        }

                        Text(
                            text = "$quantity",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DarkText
                        )

                        // Plus
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { quantity++ },
                            shape = CircleShape,
                            color = PrimaryOrange
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Add, contentDescription = "增加", tint = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MutedText),
                        border = BorderStroke(1.dp, BorderColor)
                    ) {
                        Text("取消")
                    }

                    Button(
                        onClick = {
                            val iceArg = if (menuItem.hasIceSugarCustomization) selectedIce else ""
                            val sugarArg = if (menuItem.hasIceSugarCustomization) selectedSugar else ""
                            onConfirm(quantity, iceArg, sugarArg, specialNote)
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("add_to_cart_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                    ) {
                        Text("加入購物車 $$${menuItem.price * quantity}", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun FlowSelector(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { option ->
            val isSel = option == selectedOption
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOptionSelected(option) },
                shape = RoundedCornerShape(10.dp),
                color = if (isSel) PrimaryOrange.copy(alpha = 0.15f) else ThemeBgColor,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSel) PrimaryOrange else BorderColor
                )
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isSel) PrimaryOrange else DarkText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


// ==========================================
// CART DRAWER / OVERLAY SHEET
// ==========================================
@Composable
fun CartSheet(
    viewModel: FoodViewModel,
    cartItems: List<CartEntity>,
    totalPrice: Int,
    onDismiss: () -> Unit
) {
    var orderingType by remember { mutableStateOf("內用") } // "內用" / "外帶"
    var tableNo by remember { mutableStateOf("") }
    var specialInstruction by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = ThemeBgColor,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header of Cart Drawer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = null,
                            tint = PrimaryOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "我的點選清單",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = DarkText
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "關閉", tint = DarkText)
                    }
                }

                HorizontalDivider(color = BorderColor, thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))

                // Cart Scrollable items
                if (cartItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("購物車空空如也，快去挑選好吃的吧！", color = MutedText, style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(cartItems) { item ->
                            CartItemRow(
                                item = item,
                                onIncrement = { viewModel.incrementCartItem(item) },
                                onDecrement = { viewModel.decrementCartItem(item) },
                                onRemove = { viewModel.removeCartItem(item) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BorderColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Dining types Choose: Dine-In vs. Take-Out
                    Text(
                        "選擇用餐方式",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = DarkText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { orderingType = "內用" },
                            shape = RoundedCornerShape(12.dp),
                            color = if (orderingType == "內用") PrimaryOrange.copy(alpha = 0.15f) else LightCardColor,
                            border = BorderStroke(1.dp, if (orderingType == "內用") PrimaryOrange else BorderColor)
                        ) {
                            Text(
                                "店內用餐 (Dine-In)",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (orderingType == "內用") PrimaryOrange else DarkText
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { orderingType = "外帶" },
                            shape = RoundedCornerShape(12.dp),
                            color = if (orderingType == "外帶") PrimaryOrange.copy(alpha = 0.15f) else LightCardColor,
                            border = BorderStroke(1.dp, if (orderingType == "外帶") PrimaryOrange else BorderColor)
                        ) {
                            Text(
                                "外帶自取 (Take-out)",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (orderingType == "外帶") PrimaryOrange else DarkText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (orderingType == "內用") {
                        // Table number config
                        OutlinedTextField(
                            value = tableNo,
                            onValueChange = { if (it.length <= 4) tableNo = it },
                            label = { Text("請輸入桌號 (例如: 3)", color = MutedText) },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("table_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = LightCardColor,
                                unfocusedContainerColor = LightCardColor,
                                focusedBorderColor = PrimaryOrange,
                                unfocusedBorderColor = BorderColor,
                                cursorColor = PrimaryOrange
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Special Notes for whole order
                    OutlinedTextField(
                        value = specialInstruction,
                        onValueChange = { specialInstruction = it },
                        label = { Text("整單特殊代交代（例如：餐具減量）", color = MutedText) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = LightCardColor,
                            unfocusedContainerColor = LightCardColor,
                            focusedBorderColor = PrimaryOrange,
                            unfocusedBorderColor = BorderColor,
                            cursorColor = PrimaryOrange
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Pricing footer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "總金額 Total",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DarkText
                        )
                        Text(
                            "$$totalPrice",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = PrimaryOrange,
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Checkout CTA button
                    Button(
                        onClick = {
                            viewModel.placeOrder(orderingType, tableNo, specialInstruction)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("place_order_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Restaurant, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("確認送出訂單", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartEntity,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        color = LightCardColor,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = DarkText
                )

                // Render specifications
                val specs = mutableListOf<String>()
                if (item.iceLevel.isNotBlank()) specs.add(item.iceLevel)
                if (item.sugarLevel.isNotBlank()) specs.add(item.sugarLevel)
                if (item.notes.isNotBlank()) specs.add(item.notes)

                if (specs.isNotEmpty()) {
                    Text(
                        text = specs.joinToString(" / "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$${item.price * item.quantity}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = PrimaryOrange,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }

            // Adjuster / Trash buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Decrement button
                Surface(
                    modifier = Modifier
                        .size(30.dp)
                        .clickable(onClick = onDecrement),
                    shape = CircleShape,
                    color = BorderColor
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (item.quantity > 1) Icons.Filled.Remove else Icons.Filled.Delete,
                            contentDescription = "減少",
                            tint = DarkText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = "${item.quantity}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = DarkText
                )

                // Increment button
                Surface(
                    modifier = Modifier
                        .size(30.dp)
                        .clickable(onClick = onIncrement),
                    shape = CircleShape,
                    color = PrimaryOrange
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "增加",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Delete all trash icon
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "整筆刪除",
                        tint = MutedText.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
