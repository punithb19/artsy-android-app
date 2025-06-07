@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.artsyandroidapp

/* ───────────── Imports ───────────── */
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.artsyandroidapp.auth.AuthApi
import com.example.artsyandroidapp.auth.AuthRepository
import com.example.artsyandroidapp.net.NetModule
import com.example.artsyandroidapp.ui.theme.ArtsyAndroidAppTheme
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/* ───────────── Retrofit setup ───────────── */

private const val BASE_URL = "https://artsy-android-backend.wl.r.appspot.com/"

@JsonClass(generateAdapter = true)
data class ArtistBrief(val id: String, val name: String, val thumbnail: String?)

data class ArtistDetail(
    val id: String,
    val name: String,
    val nationality: String,
    val birthday: String,
    val deathday: String,
    val biography: String,
    val thumbnail: String?
)

data class ArtworkBrief(val id: String, val title: String, val date: String, val thumbnail: String?)
data class Gene(val name: String, val thumbnail: String?, val description: String?)

data class FavoriteArtist(
    val id: String,
    val name: String,
    val nationality: String,
    val birthday: String,
    val thumbnail: String?,
    val addedDate: String
)

data class FavoriteResponse(
    val success: Boolean,
    val favorites: List<FavoriteArtist>
)

interface ArtsyApi {
    @GET("api/search_artist")
    suspend fun searchArtist(@Query("name") q: String): List<ArtistBrief>
    @GET("api/artist/{id}")
    suspend fun getArtist(@Path("id") id: String): ArtistDetail
    @GET("api/artworks/{id}")
    suspend fun getArtworks(@Path("id") id: String): List<ArtworkBrief>
    @GET("api/categories/{id}")
    suspend fun getCategories(@Path("id") id: String): List<Gene>
    @GET("api/similar_artists/{id}")
    suspend fun getSimilarArtists(@Path("id") id: String): List<ArtistBrief>
    @GET("api/users/favorites")
    suspend fun getFavorites(): FavoriteResponse
    @POST("api/users/favorites")
    suspend fun addFavorite(@Body body: Map<String, String>): FavoriteResponse
    @DELETE("api/users/favorites/{artistId}")
    suspend fun removeFavorite(@Path("artistId") artistId: String): FavoriteResponse
}

// ServiceLocator.kt
object ServiceLocator {
    lateinit var api: ArtsyApi
        private set
    lateinit var authRepo: AuthRepository
        private set
    lateinit var uiAuth: AuthUiState
        private set

    fun init(ctx: Context) {
        NetModule.init(ctx)   // ← initialize Retrofit + cookieJar

        api      = NetModule.retrofit.create(ArtsyApi::class.java)
        val authApi = NetModule.retrofit.create(AuthApi::class.java)
        authRepo = AuthRepository(authApi)
        uiAuth   = AuthUiState(authRepo)
    }
}



/* ───────────── Activity ───────────── */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ServiceLocator.init(applicationContext)
        enableEdgeToEdge()
        setContent { ArtsyAndroidAppTheme { HomeScreen() } }
    }
}

/* ───────────── Home screen ───────────── */


@Composable
fun HomeScreen() {
    val auth = ServiceLocator.uiAuth

    // Kick off session-restore exactly once
    LaunchedEffect(Unit) {
        try {
            Log.d("HomeScreen", "Calling tryRestoreSession")
            auth.tryRestoreSession()
        } catch (e: Exception) {
            Log.e("HomeScreen", "Error restoring session", e)
        }
    }

    // While restoring, show a full-screen spinner
    val isRestoring by auth.isRestoring.collectAsState()
    if (isRestoring) {
        BoxFill { CircularProgressIndicator() }
        return
    }

    // Once restored, show the real UI
    var showLogin    by remember { mutableStateOf(false) }
    var showRegister by remember { mutableStateOf(false) }
    val snackHost    = remember { SnackbarHostState() }
    auth.setNavigateToLoginCallback { showLogin = true }

    val user        by auth.user.collectAsState(initial = null)
    val snack       by auth.snackbar.collectAsState(initial = null)
    var searchOn    by remember { mutableStateOf(false) }
    var query       by remember { mutableStateOf("") }
    var results     by remember { mutableStateOf<List<ArtistBrief>>(emptyList()) }
    var selected    by remember { mutableStateOf<ArtistBrief?>(null) }
    var favoritesSet by remember { mutableStateOf<Set<String>>(emptySet()) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(user) {
        if (user != null) {
            val favs = ServiceLocator.api.getFavorites().favorites.map { it.id }.toSet()
            favoritesSet = favs
        } else {
            favoritesSet = emptySet()
        }
    }

    val scope = rememberCoroutineScope()
    fun toggleFavorite(id: String) {
        scope.launch {
            if (favoritesSet.contains(id)) {
                runCatching { ServiceLocator.api.removeFavorite(id) }
                favoritesSet = favoritesSet - id
                snackHost.showSnackbar("Removed from favorites")
            } else {
                runCatching { ServiceLocator.api.addFavorite(mapOf("artistId" to id)) }
                favoritesSet = favoritesSet + id
                snackHost.showSnackbar("Added to favorites")
            }
        }
    }

    // debounce search
    LaunchedEffect(query) {
        snapshotFlow { query }
            .filter { it.length >= 3 }
            .debounce(300)
            .collectLatest { term ->
                results = runCatching {
                    ServiceLocator.api.searchArtist(term)
                }.getOrElse {
                    Log.e("ArtsySearch", "API failed", it)
                    emptyList()
                }
            }
    }

    // Login / register screens
    if (showLogin) {
        LoginScreen(
            onBack     = { showLogin = false },
            onLogin    = { _, _ -> showLogin = false },
            onRegister = {
                showLogin    = false
                showRegister = true
            }
        )
        return
    }
    if (showRegister) {
        RegisterScreen(
            onBack     = { showRegister = false },
            onRegister = { _, _, _ -> showRegister = false },
            onLogin    = {
                showRegister = false
                showLogin    = true
            }
        )
        return
    }

    // Back button closes search
    if (searchOn) BackHandler { searchOn = false }

    Scaffold(
        snackbarHost = { SnackbarHost(snackHost) },
        topBar = {
            when {
                // Detail view
                selected != null -> TopAppBar(
                    title          = { Text(selected!!.name) },
                    navigationIcon = {
                        IconButton(onClick = { selected = null }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        // only show star if logged in
                        if (user != null) {
                            IconButton(onClick = { toggleFavorite(selected!!.id) }) {
                                Icon(
                                    imageVector = if (favoritesSet.contains(selected!!.id))
                                        Icons.Filled.Star
                                    else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    },
                    //colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFbfcaf5))
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor           = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor        = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor   = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                // Search bar
                searchOn -> SearchTopBar(
                    query          = query,
                    onQueryChange  = { query = it },
                    onClear        = { query = "" },
                    onClose        = { searchOn = false },
                    focusRequester = focusRequester
                )
                // Normal top bar
                else -> NormalTopBar(
                    onSearchClick = { searchOn = true },
                    auth          = auth
                )
            }
        }
    ) { pad ->
        when {
            // Details tab
            selected != null -> ArtistTabs(
                modifier   = Modifier.padding(pad),
                artistId   = selected!!.id,
                artistName = selected!!.name,
                onArtistSelected  = { selected = it },
                isFavorite        = { id -> favoritesSet.contains(id) },
                onToggleFavorite  = { id -> toggleFavorite(id) }
            )

            // Search results
            searchOn -> LazyColumn(
                contentPadding = PaddingValues(12.dp),
                modifier       = Modifier
                    .fillMaxSize()
                    .padding(pad)
            ) {
                items(results) { artist ->
                    ArtistCard(
                        artist = artist,
                        isFavorite = favoritesSet.contains(artist.id),
                        onToggleFavorite = { toggleFavorite(it.id) },
                        onClick = { selected = artist },
                        onArrowClick = { selected = artist }
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }

            else -> FavouritesBlock(
                padding = pad,
                onLoginClick = { showLogin = true }
            ) { fav ->
                selected = ArtistBrief(fav.id, fav.name, fav.thumbnail)
            }

        }
    }

    // Show any snackbar messages
    LaunchedEffect(snack) {
        snack?.let {
            snackHost.showSnackbar(it)
            auth.onSnackShown()
        }
    }
}

/* ────────── Top bars ────────── */
@Composable
private fun NormalTopBar(
    onSearchClick: () -> Unit,
    auth: AuthUiState
) {
    val scope     = rememberCoroutineScope()
    val userState = auth.user.collectAsState()

    /* local state for the drop‑down */
    var menuOpen by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("Artist Search") },
        actions = {
            // ── SEARCH ICON ──────────────────────────────────────────
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Filled.Search, null)
            }

            // ── PERSON / AVATAR + MENU ──────────────────────────────
            if (userState.value == null) {
                /* not logged‑in → simple person icon that navigates to Login */
                IconButton(onClick = { auth.navigateToLogin() }) {
                    Icon(Icons.Default.Person, contentDescription = "Login")
                }
            } else {
                /* logged‑in → show avatar & dropdown */
                IconButton(onClick = { menuOpen = true }) {
                    AsyncImage(
                        model = userState.value!!.avatarUrl,
                        contentDescription = "User avatar",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Log out") },
                        onClick = {
                            menuOpen = false
                            scope.launch { auth.logout() }   // <── call repo
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete account") },
                        onClick = {
                            menuOpen = false
                            scope.launch { auth.deleteAccount() }
                        }
                    )
                }
            }
        },
        //colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFbfcaf5))
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor           = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor        = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor   = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}



@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit,
    focusRequester: FocusRequester
) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer) {
        Row(
            Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Search, null)
            Spacer(Modifier.width(8.dp))
            TextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                placeholder = { Text("Search artists…", fontSize = 20.sp) },
                textStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            IconButton(onClick = { if (query.isNotEmpty()) onClear() else onClose() }) {
                Icon(Icons.Filled.Close, "Clear/close")
            }
        }
    }
}

/* ────────── Favourites block ────────── */
@Composable
private fun FavouritesBlock(
    padding: PaddingValues,
    onLoginClick: () -> Unit,
    onArtistClick: (FavoriteArtist) -> Unit
) {
    val user       by ServiceLocator.uiAuth.user.collectAsState()
    var favorites  by remember { mutableStateOf<List<FavoriteArtist>?>(null) }
    var loading    by remember { mutableStateOf(true) }
    var error      by remember { mutableStateOf<String?>(null) }

    // Load favorites once when user becomes non-null
    LaunchedEffect(user) {
        if (user != null) {
            try {
                favorites = ServiceLocator.api.getFavorites().favorites
            } catch (e: Exception) {
                error = "Failed to load favorites: ${e.localizedMessage}"
            }
        }
        loading = false
    }

    // Tick every second to force recomposition
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            // 1) update immediately …
            nowMillis = System.currentTimeMillis()
            // 2) … then wait one second
            delay(1_000L)
        }
    }

    // Our own “X seconds/minutes/hours ago” formatter
    fun formatAgo(iso: String, now: Long): String {
        val then = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(iso)).toEpochMilli()
        val delta = (now - then).coerceAtLeast(0L)
        val secs  = delta / 1000
        return when {
            secs < 60 -> "$secs second${if (secs != 1L) "s" else ""} ago"
            secs < 3600 -> {
                val m = secs / 60
                "$m minute${if (m != 1L) "s" else ""} ago"
            }
            else -> {
                val h = secs / 3600
                "$h hour${if (h != 1L) "s" else ""} ago"
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        // Header: today's date
        val dateText = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.getDefault()))
        Text(
            dateText,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 16.sp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
        Surface(tonalElevation = 1.dp, color = MaterialTheme.colorScheme.surfaceVariant) {
            Text(
                "Favorites",
                style = MaterialTheme.typography.bodyMedium
                    .copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        when {
            user == null -> Box(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Button(onClick = onLoginClick) { Text("Log in to see favorites") }
            }
            loading -> BoxFill { CircularProgressIndicator() }
            error != null -> BoxFill { Text(error!!, color = Color.Red) }
            favorites.isNullOrEmpty() -> Box(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        "No favorites",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> LazyColumn(
                Modifier
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(favorites!!) { fav ->
                    val agoText = formatAgo(fav.addedDate, nowMillis)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onArtistClick(fav) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(fav.name, fontSize = 18.sp)
                            // build a list of non‑"unknown" parts
                            val parts = mutableListOf<String>()
                            if (!fav.nationality.equals("unknown", ignoreCase = true)) {
                                parts += fav.nationality
                            }
                            if (!fav.birthday.equals("unknown", ignoreCase = true)) {
                                // take first 4 chars as year
                                parts += fav.birthday.take(4)
                            }
                            val subtitle = parts.joinToString(", ")

                            if (subtitle.isNotEmpty()) {
                                Text(
                                    subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                        Text(
                            agoText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        IconButton(onClick = { onArtistClick(fav) }) {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "Details",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
        val uriHandler = LocalUriHandler.current
        Text(
            "Powered by Artsy",
            modifier = Modifier
                .fillMaxWidth()
                .clickable { uriHandler.openUri("https://www.artsy.net/") }
                .padding(vertical = 12.dp),
            textAlign = TextAlign.Center,
            fontStyle = FontStyle.Italic,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black
        )
    }
}


/* ────────── Artist card ────────── */

@Composable
fun ArtistCard(
    artist: ArtistBrief,
    isFavorite: Boolean,
    onToggleFavorite: (ArtistBrief) -> Unit,
    onClick: () -> Unit = {},
    cardHeight: Dp = 200.dp,
    nameColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    nameBackground: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.60f),
    onArrowClick: () -> Unit = onClick
) {
    val user by ServiceLocator.uiAuth.user.collectAsState()
    val isLoggedIn = user != null
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(artist.thumbnail)
                    .crossfade(true)
                    .fallback(R.drawable.ic_launcher_foreground)
                    .build(),
                contentDescription = artist.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (user != null) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,      // pale-blue pill background
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)                   // container diameter
                ) {
                    IconButton(
                        onClick = { onToggleFavorite(artist) },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)               // inset so icon isn’t flush to the edge
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (isFavorite)
                                Color.Black                    // filled star color
                            else
                                Color.Black.copy(alpha = 0.6f) // outline star color
                        )
                    }
                }
            }
            Surface(
                color = nameBackground,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        artist.name,
                        color = nameColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onArrowClick) {
                        Icon(Icons.Filled.ChevronRight, "Details", tint = nameColor, modifier = Modifier.size(28.dp))
                    }
                }
            }
        }
    }
}

/* ────────── Tabs ────────── */
@Composable
fun ArtistTabs(
    modifier: Modifier = Modifier,
    artistId: String,
    artistName: String,
    onArtistSelected: (ArtistBrief) -> Unit,
    isFavorite: (String) -> Boolean,
    onToggleFavorite: (String) -> Unit
) {
    // Observe login state
    val user by ServiceLocator.uiAuth.user.collectAsState()
    val isLoggedIn = user != null

    // Which tab is selected
    var tab by remember { mutableStateOf(0) }
    // State for each tab's content
    var detail   by remember { mutableStateOf<ArtistDetail?>(null) }
    var artworks by remember { mutableStateOf<List<ArtworkBrief>?>(null) }
    var similars by remember { mutableStateOf<List<ArtistBrief>?>(null) }
    var loading  by remember { mutableStateOf(false) }
    var err      by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(artistId) {
        tab = 0
        detail = null
        artworks = null
        similars = null
        err = null
    }

    // If the user logs out while on "Similar", reset to Details
    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn && tab == 2) {
            tab = 0
        }
    }

    // Only show the Similar tab when logged in
    val titles = if (isLoggedIn) {
        listOf("Details", "Artworks", "Similar")
    } else {
        listOf("Details", "Artworks")
    }
    val icons = if (isLoggedIn) {
        listOf(Icons.Outlined.Info, Icons.Outlined.AccountBox, Icons.Outlined.PersonSearch)
    } else {
        listOf(Icons.Outlined.Info, Icons.Outlined.AccountBox)
    }

    // Loader helper
    suspend fun <T> load(block: suspend () -> T, save: (T) -> Unit) {
        loading = true; err = null
        runCatching { block() }
            .onSuccess(save)
            .onFailure { err = it.localizedMessage }
        loading = false
    }

    // Fetch data when tab or artistId changes
    LaunchedEffect(tab, artistId) {
        when (tab) {
            0 -> if (detail == null)   load({ ServiceLocator.api.getArtist(artistId) })     { detail = it }
            1 -> if (artworks == null) load({ ServiceLocator.api.getArtworks(artistId) })  { artworks = it }
            2 -> if (isLoggedIn && similars == null) load({ ServiceLocator.api.getSimilarArtists(artistId) }) { similars = it }
        }
    }

    Column(modifier) {
        // Tab row
        TabRow(selectedTabIndex = tab) {
            titles.forEachIndexed { i, title ->
                Tab(
                    selected = tab == i,
                    onClick  = { tab = i },
                    text     = { Text(title) },
                    icon     = { Icon(icons[i], contentDescription = null) }
                )
            }
        }

        // Content
        when (tab) {
            0 -> DetailsContent(detail, loading, err, artistName)
            1 -> ArtworksContent(artworks, loading, err)
            2 -> if (isLoggedIn) SimilarContent(
                list              = similars,
                loading           = loading,
                err               = err,
                onArtistSelected  = onArtistSelected,
                isFavorite        = isFavorite,
                onToggleFavorite  = onToggleFavorite
            )
        }
    }
}

/* ───── Details tab ───── */

@Composable
private fun DetailsContent(
    d: ArtistDetail?,
    loading: Boolean,
    err: String?,
    fallbackName: String
) {
    when {
        loading -> BoxFill { CircularProgressIndicator() }
        err != null -> BoxFill { Text("Error: $err", color = Color.Red) }
        d == null -> BoxFill { Text("No details") }
        else -> Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                d.name.ifBlank { fallbackName },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            val nat   = d.nationality  .takeIf { it != "Unknown" } ?: ""
            val birth = d.birthday     .takeIf { it != "Unknown" } ?: ""
            val death = d.deathday     .takeIf { it != "Unknown" } ?: ""
            Text(
                "${nat}, ${birth} – ${death}",
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Text(
                d.biography.ifBlank { "" },
                textAlign = TextAlign.Justify,
                lineHeight = 20.sp
            )
        }
    }
}

/* ───── Artworks tab ───── */

@Composable
private fun ArtworksContent(
    artworks: List<ArtworkBrief>?,
    loading: Boolean,
    err: String?
) {
    when {
        loading -> BoxFill { CircularProgressIndicator() }
        err != null -> BoxFill { Text("Error: $err") }
        artworks.isNullOrEmpty() -> BoxFill {
            Surface(
                shape  = RoundedCornerShape(12.dp),
                color  = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    "No Artworks",
                    modifier = Modifier
                        .padding(vertical = 14.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        else -> LazyColumn(contentPadding = PaddingValues(12.dp)) {
            items(artworks) { art -> ArtworkCard(art) }
        }
    }
}

@Composable
private fun ArtworkCard(art: ArtworkBrief) {
    var show by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                model = art.thumbnail ?: "",
                contentDescription = art.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentScale = ContentScale.Crop
            )
            Text(
                "${art.title}, ${art.date}",
                modifier = Modifier.padding(8.dp),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = { show = true },
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .defaultMinSize(minWidth = 140.dp)
            ) { Text("View categories") }
        }
    }

    if (show) CategoryDialog(art.id) { show = false }
}

/* ────────── CATEGORY DIALOG ────────── */

@Composable
private fun CategoryDialog(
    artworkId: String,
    onClose:   () -> Unit
) {
    var genes      by remember { mutableStateOf<List<Gene>?>(null) }
    var isLoading  by remember { mutableStateOf(true) }
    var error      by remember { mutableStateOf<String?>(null) }

    /* load once */
    LaunchedEffect(artworkId) {
        runCatching { ServiceLocator.api.getCategories(artworkId) }
            .onSuccess { genes = it }
            .onFailure { error = it.localizedMessage }
        isLoading = false
    }

    AlertDialog(
        onDismissRequest = onClose,
        shape  = RoundedCornerShape(28.dp),
        tonalElevation = 6.dp,                    // ∙ soft elevation
        confirmButton = {
            Button(
                onClick = onClose,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF365A9C),          // deep blue
                    contentColor   = Color.White
                )
            ) { Text("Close") }
        },
        title = {
            Text(
                "Categories",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
            )
        },
        text = {
            when {
                isLoading -> {
                    /* centre spinner + label, but let dialog wrap its content */
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()          // wide, but …
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(8.dp))
                            Text("Loading…")
                        }
                    }
                }

                error != null -> Text("Error: $error", color = Color.Red)

                genes.isNullOrEmpty() -> Text("No categories available")

                else -> GeneCarousel(genes!!)
            }
        }
    )
}

/* ────────── CAROUSEL WITH ARROWS & SCROLLABLE DESCRIPTION ────────── */

@Composable
private fun GeneCarousel(genes: List<Gene>) {
    val pagerState = rememberPagerState()
    val nPages     = genes.size
    val scope = rememberCoroutineScope()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {

        Box {                                     /* arrows sit on this Box */
            HorizontalPager(
                count  = nPages,
                state  = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)               // tall enough for text
            ) { page ->

                val g = genes[page]

                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxSize(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {

                        /* image */
                        AsyncImage(
                            model = g.thumbnail ?: "",
                            contentDescription = g.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(Modifier.height(8.dp))

                        /* name */
                        Text(
                            g.name,
                            fontWeight = FontWeight.SemiBold,
                            textAlign  = TextAlign.Center,
                            fontSize   = 18.sp
                        )

                        /* description ‑ cleaned & scrollable */
                        g.description?.takeIf { it.isNotBlank() }?.let { raw ->
                            // convert  [John Currin](/artist/john-currin)  →  John Currin
                            val clean = raw.replace(
                                Regex("\\[([^]]+)]\\([^)]*\\)"),   // one regex does it all
                                "$1"
                            )

                            Spacer(Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .weight(1f)                       // fill remaining height
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    clean,
                                    style     = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Justify,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            /* left / right nav chevrons */
            if (nPages > 1) {
                IconButton(
                    onClick = {
                        val prev = (pagerState.currentPage - 1 + nPages) % nPages
                        scope.launch {                           // ← wrap in launch
                            pagerState.animateScrollToPage(prev)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(32.dp)
                ) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "Previous",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = {
                        val next = (pagerState.currentPage + 1) % nPages
                        scope.launch {                           // ← wrap in launch
                            pagerState.animateScrollToPage(next)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(32.dp)
                ) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Next",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/* ───── Similar tab ───── */

@Composable
private fun SimilarContent(
    list: List<ArtistBrief>?,
    loading: Boolean,
    err: String?,
    onArtistSelected: (ArtistBrief) -> Unit,
    isFavorite: (String) -> Boolean,
    onToggleFavorite: (String) -> Unit
) {
    when {
        loading      -> BoxFill { CircularProgressIndicator() }
        err != null   -> BoxFill { Text("Error: $err") }
        list.isNullOrEmpty() -> BoxFill {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    "No similar artists",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        else -> LazyColumn(contentPadding = PaddingValues(12.dp)) {
            items(list) { artist ->
                ArtistCard(
                    artist          = artist,
                    isFavorite        = isFavorite(artist.id),
                    onToggleFavorite  = { onToggleFavorite(artist.id) },
                    onClick         = { onArtistSelected(artist) },
                    onArrowClick    = { onArtistSelected(artist) }
                )
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}


@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onLogin: (String, String) -> Unit,
    onRegister: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val auth = ServiceLocator.uiAuth

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var loading      by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                //colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFbfcaf5))
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor           = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor        = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor   = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
                .imePadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                    loginError = null
                },
                label = { Text("Email") },
                isError = emailError != null,
                singleLine = true,
                enabled     = !loading,
                modifier = Modifier.fillMaxWidth()
            )
            if (emailError != null) {
                Text(
                    text = emailError!!,
                    color = Color.Red,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp),
                    textAlign = TextAlign.Start
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                    loginError = null
                },
                label = { Text("Password") },
                isError = passwordError != null,
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            )
            if (passwordError != null) {
                Text(
                    text = passwordError!!,
                    color = Color.Red,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp),
                    textAlign = TextAlign.Start
                )
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    // basic client-side validation
                    var isValid = true

                    if (email.isBlank()) {
                        emailError = "Email cannot be empty"
                        isValid = false
                    } else if (!email.matches(emailRegex)) {
                        emailError = "Invalid email format"
                        isValid = false
                    }

                    if (password.isBlank()) {
                        passwordError = "Password cannot be empty"
                        isValid = false
                    }

                    if (isValid) {
                        scope.launch {
                            loading = true
                            val success = auth.login(email, password)
                            loading = false
                            if (success) {
                                onBack()
                            } else {
                                loginError = "Username or password is incorrect"
                            }
                        }
                    }
                },
                enabled = !loading,
                shape = RoundedCornerShape(percent = 50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF365A9C), contentColor = Color.White)
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text("Login")
                }
            }

            if (loginError != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = loginError!!,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp),
                    textAlign = TextAlign.Start
                )
            }

            Spacer(Modifier.height(16.dp))
            Row {
                Text("Don't have an account yet?")
                Spacer(Modifier.width(4.dp))
                Text(
                    "Register",
                    color = Color(0xFF365A9C),
                    modifier = Modifier.clickable(onClick = onRegister),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onRegister: (String, String, String) -> Unit,
    onLogin: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val auth  = ServiceLocator.uiAuth

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var registerError by remember { mutableStateOf<String?>(null) }
    var loading       by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\$")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                //colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFbfcaf5))
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor           = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor        = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor   = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
                .imePadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = null
                    registerError = null
                },
                label = { Text("Enter full name") },
                isError = nameError != null,
                singleLine = true,
                enabled     = !loading,
                modifier = Modifier.fillMaxWidth()
            )
            if (nameError != null) {
                Text(
                    text = nameError!!,
                    color = Color.Red,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp),
                    textAlign = TextAlign.Start
                )
            }
            Spacer(Modifier.height(12.dp))

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                    registerError = null
                },
                label = { Text("Enter email") },
                isError = emailError != null || registerError != null,
                singleLine = true,
                enabled     = !loading,
                modifier = Modifier.fillMaxWidth()
            )
            if (emailError != null) {
                Text(
                    text = emailError!!,
                    color = Color.Red,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp),
                    textAlign = TextAlign.Start
                )
            }
            // backend error
            if (registerError != null) {
                Text(
                    text = registerError!!,
                    color = Color.Red,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp),
                    textAlign = TextAlign.Start
                )
            }

            Spacer(Modifier.height(12.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                    registerError = null
                },
                label = { Text("Password") },
                isError = passwordError != null,
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                enabled     = !loading,
                modifier = Modifier.fillMaxWidth()
            )
            if (passwordError != null) {
                Text(
                    text = passwordError!!,
                    color = Color.Red,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp),
                    textAlign = TextAlign.Start
                )
            }

            Spacer(Modifier.height(16.dp))

            // Register button
            Button(
                onClick = {
                    // client‑side validation
                    var valid = true
                    if (name.isBlank()) {
                        nameError = "Full name cannot be empty"
                        valid = false
                    }
                    if (email.isBlank()) {
                        emailError = "Email cannot be empty"
                        valid = false
                    } else if (!email.matches(emailRegex)) {
                        emailError = "Invalid email format"
                        valid = false
                    }
                    if (password.isBlank()) {
                        passwordError = "Password cannot be empty"
                        valid = false
                    }

                    if (valid) {
                        loading = true
                        scope.launch {
                            val ok = auth.register(name, email, password)
                            loading = false
                            if (ok) {
                                onBack()
                            } else {
                                registerError = "Email already exists"
                            }
                        }
                    }
                },
                enabled = !loading,
                shape = RoundedCornerShape(percent = 50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF365A9C),
                    contentColor   = Color.White
                )
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text("Register")
                }
            }

            Spacer(Modifier.height(16.dp))

            Row {
                Text("Already have an account?")
                Spacer(Modifier.width(4.dp))
                Text(
                    "Login",
                    color = Color(0xFF365A9C),
                    modifier = Modifier.clickable(onClick = onLogin),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/* helper */
@Composable
private fun BoxFill(content: @Composable BoxScope.() -> Unit) = Box(
    Modifier
        .fillMaxSize()
        .padding(24.dp),
    contentAlignment = Alignment.TopCenter,
    content = content
)

/* ────────── Preview ────────── */

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
private fun HomePreview() {
    ArtsyAndroidAppTheme { HomeScreen() }
}
