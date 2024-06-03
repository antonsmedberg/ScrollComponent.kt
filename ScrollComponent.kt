
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.ScrollAxisRange
import androidx.compose.ui.semantics.horizontalScrollAxisRange
import androidx.compose.ui.semantics.scrollBy
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.verticalScrollAxisRange
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


/**
 * Skapar och [remember] den [ScrollState] baserat på aktuell scrollkonfiguration
 * för att tillåta ändring av scrollposition eller observera scrollbeteende.
 *
 * @param initial initial scrollposition att starta med
 */
@Composable
fun rememberScrollState(initial: Int = 0): ScrollState {
    return rememberSaveable(saver = ScrollState.Saver) {
        ScrollState(initial = initial)
    }
}

/**
 * Tillstånd för scroll. Tillåter utvecklaren att ändra scrollposition eller få aktuellt tillstånd
 * genom att anropa metoder på detta objekt. För att användas med [Modifier.verticalScroll] eller
 * [Modifier.horizontalScroll].
 *
 * @param initial initialvärde för scroll
 */
@Stable
class ScrollState(initial: Int) : ScrollableState {

    /**
     * Nuvarande scrollposition i pixlar
     */
    var value: Int by mutableIntStateOf(initial)
        private set

    /**
     * Maximalt värde för [value], eller [Int.MAX_VALUE] om fortfarande okänt
     */
    var maxValue: Int
        get() = _maxValueState.intValue
        internal set(newMax) {
            _maxValueState.intValue = newMax
            if (value > newMax) {
                value = newMax
            }
        }

    /**
     * [InteractionSource] som kommer användas för att skicka dragevenemang när denna
     * lista dras. För att veta om en "fling" (eller mjuk scrollning) är på gång, använd [isScrollInProgress].
     */
    val interactionSource: MutableInteractionSource = MutableInteractionSource()

    private var _maxValueState = mutableIntStateOf(Int.MAX_VALUE)

    private var accumulator: Float = 0f

    private val scrollableState = ScrollableState {
        val absolute = (value + it + accumulator)
        val newValue = absolute.coerceIn(0f, maxValue.toFloat())
        val changed = absolute != newValue
        val consumed = newValue - value
        val consumedInt = consumed.roundToInt()
        value += consumedInt
        accumulator = consumed - consumedInt

        if (changed) consumed else it
    }

    override suspend fun scroll(
        scrollPriority: MutatePriority,
        block: suspend ScrollScope.() -> Unit
    ) = scrollableState.scroll(scrollPriority, block)

    override fun dispatchRawDelta(delta: Float): Float =
        scrollableState.dispatchRawDelta(delta)

    override val isScrollInProgress: Boolean
        get() = scrollableState.isScrollInProgress

    /**
     * Scrolla till position i pixlar.
     *
     * @param value målposition i pixlar för scrollning, värde kommer att begränsas till
     * 0..maxPosition
     * @param animationSpec animeringskurva för mjuk scrollningsanimation. Om null, sker scrollningen omedelbart.
     */
    suspend fun scrollTo(
        value: Int,
        animationSpec: AnimationSpec<Float>? = null
    ): Float {
        return if (animationSpec == null) {
            this.scrollBy((value - this.value).toFloat())
        } else {
            this.animateScrollBy((value - this.value).toFloat(), animationSpec)
        }
    }

    companion object {
        val Saver: Saver<ScrollState, *> = Saver(
            save = { it.value },
            restore = { ScrollState(it) }
        )
    }
}

/**
 * Modifier för att tillåta vertikal scrollning när innehållets höjd är större än
 * maxbegränsningar tillåter.
 *
 * @param state tillstånd för scroll
 * @param enabled om scrollning via touch är aktiverat
 * @param flingBehavior logik som beskriver flingbeteende när dragning är klar med hastighet.
 * @param reverseScrolling vänder riktningen på scrollningen, när `true` betyder 0 [ScrollState.value] botten
 */
fun Modifier.verticalScroll(
    state: ScrollState,
    enabled: Boolean = true,
    flingBehavior: FlingBehavior? = null,
    reverseScrolling: Boolean = false
) = scroll(
    state = state,
    isScrollable = enabled,
    reverseScrolling = reverseScrolling,
    flingBehavior = flingBehavior,
    isVertical = true
)

/**
 * Modifier för att tillåta horisontell scrollning när innehållets bredd är större än
 * maxbegränsningar tillåter.
 *
 * @param state tillstånd för scroll
 * @param enabled om scrollning via touch är aktiverat
 * @param flingBehavior logik som beskriver flingbeteende när dragning är klar med hastighet.
 * @param reverseScrolling vänder riktningen på scrollningen, när `true` betyder 0 [ScrollState.value] höger
 */
fun Modifier.horizontalScroll(
    state: ScrollState,
    enabled: Boolean = true,
    flingBehavior: FlingBehavior? = null,
    reverseScrolling: Boolean = false
) = scroll(
    state = state,
    isScrollable = enabled,
    reverseScrolling = reverseScrolling,
    flingBehavior = flingBehavior,
    isVertical = false
)

/**
 * Intern funktion för att hantera scrollmodifikationer.
 *
 * @param state tillstånd för scroll
 * @param reverseScrolling vänder riktningen på scrollningen
 * @param flingBehavior logik för fling-beteende
 * @param isScrollable om scrollning är aktiverad
 * @param isVertical om scrollningen är vertikal
 */
private fun Modifier.scroll(
    state: ScrollState,
    reverseScrolling: Boolean,
    flingBehavior: FlingBehavior?,
    isScrollable: Boolean,
    isVertical: Boolean
) = composed(
    factory = {
        val coroutineScope = rememberCoroutineScope()
        val semantics = Modifier.semantics {
            if (isScrollable) {
                val accessibilityScrollState = ScrollAxisRange(
                    value = { state.value.toFloat() },
                    maxValue = { state.maxValue.toFloat() },
                    reverseScrolling = reverseScrolling
                )
                if (isVertical) {
                    this.verticalScrollAxisRange = accessibilityScrollState
                } else {
                    this.horizontalScrollAxisRange = accessibilityScrollState
                }
                scrollBy(
                    action = { x: Float, y: Float ->
                        coroutineScope.launch {
                            if (isVertical) {
                                (state as ScrollableState).animateScrollBy(y)
                            } else {
                                (state as ScrollableState).animateScrollBy(x)
                            }
                        }
                        return@scrollBy true
                    }
                )
            }
        }
        val scrolling = Modifier.scrollable(
            orientation = if (isVertical) Orientation.Vertical else Orientation.Horizontal,
            reverseDirection = run {
                var reverseDirection = !reverseScrolling
                val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
                if (isRtl && !isVertical) {
                    reverseDirection = !reverseDirection
                }
                reverseDirection
            },
            enabled = isScrollable,
            interactionSource = state.interactionSource,
            flingBehavior = flingBehavior ?: ScrollableDefaults.flingBehavior(),
            state = state
        )
        val layout = ScrollingLayoutModifier(state, reverseScrolling, isVertical)
        semantics
            .clipScrollableContainer(isVertical)
            .then(scrolling)
            .then(layout)
            .drawBehind {
                val colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.1f))
                if (isVertical) {
                    val brush = Brush.verticalGradient(colors)
                    drawRect(brush = brush, size = Size(size.width, 50f))
                    drawRect(brush = brush, size = Size(size.width, 50f), topLeft = Offset(0f, size.height - 50f))
                }
            }
    },
    inspectorInfo = debugInspectorInfo {
        name = "scroll"
        properties["state"] = state
        properties["reverseScrolling"] = reverseScrolling
        properties["flingBehavior"] = flingBehavior
        properties["isScrollable"] = isScrollable
        properties["isVertical"] = isVertical
    }
)

/**
 * Modifier för att hantera layout och mätning av scrollkomponenter
 *
 * @param scrollerState scrolltillstånd
 * @param isReversed om scrollningen är omvänd
 * @param isVertical om scrollningen är vertikal
 */
private data class ScrollingLayoutModifier(
    val scrollerState: ScrollState,
    val isReversed: Boolean,
    val isVertical: Boolean
) : LayoutModifier {
    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        constraints.assertNotNestingScrollableContainers(isVertical)
        val childConstraints = constraints.copy(
            maxHeight = if (isVertical) Constraints.Infinity else constraints.maxHeight,
            maxWidth = if (isVertical) constraints.maxWidth else Constraints.Infinity
        )
        val placeable = measurable.measure(childConstraints)
        val width = placeable.width.coerceAtMost(constraints.maxWidth)
        val height = placeable.height.coerceAtMost(constraints.maxHeight)
        val scrollHeight = placeable.height - height
        val scrollWidth = placeable.width - width
        val side = if (isVertical) scrollHeight else scrollWidth
        return layout(width, height) {
            scrollerState.maxValue = side
            val scroll = scrollerState.value.coerceIn(0, side)
            val absScroll = if (isReversed) scroll - side else -scroll
            val xOffset = if (isVertical) 0 else absScroll
            val yOffset = if (isVertical) absScroll else 0
            placeable.placeRelativeWithLayer(xOffset, yOffset)
        }
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int
    ) = measurable.minIntrinsicWidth(height)

    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int
    ) = measurable.minIntrinsicHeight(width)

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int
    ) = measurable.maxIntrinsicWidth(height)

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int
    ) = measurable.maxIntrinsicHeight(width)
}

/**
 * Kontrollera att begränsningar för scrollkomponenter inte är oändliga
 *
 * @param isVertical om scrollningen är vertikal
 */
internal fun Constraints.assertNotNestingScrollableContainers(isVertical: Boolean) {
    if (isVertical) {
        check(maxHeight != Constraints.Infinity) {
            "Vertikalt scrollbar komponent mättes med oändlig maximal höjd " +
                    "begränsningar, vilket inte är tillåtet. En av de vanliga anledningarna är att nästa layouter " +
                    "som LazyColumn och Column(Modifier.verticalScroll()). Om du vill lägga till en " +
                    "rubrik före listan med objekt, vänligen lägg till en rubrik som en separat item() före " +
                    "huvudobjekten() inuti LazyColumn-scope. Det kan finnas andra orsaker " +
                    "för att detta ska hända: din ComposeView lades till i en LinearLayout med någon " +
                    "vikt, du använde Modifier.wrapContentSize(unbounded = true) eller skrev en " +
                    "anpassad layout. Försök att ta bort källan till oändliga begränsningar i " +
                    "hierarkin ovanför scrollbehållaren."
        }
    } else {
        check(maxWidth != Constraints.Infinity) {
            "Horisontellt scrollbar komponent mättes med oändlig maximal bredd " +
                    "begränsningar, vilket inte är tillåtet. En av de vanliga anledningarna är att nästa layouter " +
                    "som LazyRow och Row(Modifier.horizontalScroll()). Om du vill lägga till en " +
                    "rubrik före listan med objekt, vänligen lägg till en rubrik som en separat item() före " +
                    "huvudobjekten() inuti LazyRow-scope. Det kan finnas andra orsaker " +
                    "för att detta ska hända: din ComposeView lades till i en LinearLayout med någon " +
                    "vikt, du använde Modifier.wrapContentSize(unbounded = true) eller skrev en " +
                    "anpassad layout. Försök att ta bort källan till oändliga begränsningar i " +
                    "hierarkin ovanför scrollbehållaren."
        }
    }
}

/**
 * Modifier för att klippa innehåll inom scrollbara behållare
 *
 * @param isVertical om scrollningen är vertikal
 */
internal fun Modifier.clipScrollableContainer(isVertical: Boolean) =
    then(if (isVertical) VerticalScrollableClipModifier else HorizontalScrollableClipModifier)

private val MaxSupportedElevation = 30.dp

private val HorizontalScrollableClipModifier = Modifier.clip(object : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val inflateSize = with(density) { MaxSupportedElevation.roundToPx().toFloat() }
        return Outline.Rectangle(
            Rect(
                left = 0f,
                top = -inflateSize,
                right = size.width,
                bottom = size.height + inflateSize
            )
        )
    }
})

private val VerticalScrollableClipModifier = Modifier.clip(object : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val inflateSize = with(density) { MaxSupportedElevation.roundToPx().toFloat() }
        return Outline.Rectangle(
            Rect(
                left = -inflateSize,
                top = 0f,
                right = size.width + inflateSize,
                bottom = size.height
            )
        )
    }
})



/**
 * En komponent för att visa innehåll som kan scrollas vertikalt.
 *
 * @param items En lista med composable objekt att visa.
 */
@Composable
fun ScrollableListView(items: List<@Composable () -> Unit>) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.background)
    ) {
        items.forEachIndexed { _, item ->
            item()
            Spacer(modifier = Modifier.height(8.dp))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        scrollState.scrollTo(0)
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Scroll to Top",
                    tint = Color.White
                )
            }
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        scrollState.scrollTo(scrollState.maxValue)
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Scroll to Bottom",
                    tint = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewScrollableListView() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFBB86FC),
            onPrimary = Color.Black,
            background = Color(0xFF121212),
            surface = Color(0xFF121212),
            onSurface = Color.White
        )
    ) {
        ScrollableListView(
            items = List(20) { index ->
                {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = "Item $index",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        )
    }
}



/**
 * En komponent för att visa innehåll som kan scrollas horisontellt.
 */
@Composable
fun HorizontalScrollableContent() {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            for (i in 1..20) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .padding(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Item $i", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        scrollState.scrollTo(0)
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Scroll to Start",
                    tint = Color.White
                )
            }
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        scrollState.scrollTo(scrollState.maxValue)
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Scroll to End",
                    tint = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHorizontalScrollableContent() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFBB86FC),
            onPrimary = Color.Black,
            background = Color(0xFF121212),
            surface = Color(0xFF121212),
            onSurface = Color.White
        )
    ) {
        HorizontalScrollableContent()
    }
}
