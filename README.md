
# Anpassad ScrollState i Jetpack Compose

Detta projekt demonstrerar implementeringen av en anpassad `ScrollState` i Jetpack Compose, som möjliggör både vertikal och horisontell rullning med omedelbar och animerad rullningsfunktionalitet. Koden innehåller även exempel på återanvändbara komponenter för rullbart innehåll.

## Funktioner

- Anpassad `ScrollState` med omedelbar och animerad rullningsfunktionalitet.
- `Modifier.verticalScroll` och `Modifier.horizontalScroll`-utökningar för att lägga till rullbeteende till composables.
- Komponenter för att visa rullbart innehåll: `ScrollableListView` och `HorizontalScrollableContent`.
- Användning av `IconButton` för att rulla till början eller slutet av det rullbara innehållet.

## Installation

1. Klona detta repository:
   ```sh
   git clone https://github.com/ditt-användarnamn/ditt-repository.git
   ```
2. Öppna projektet i Android Studio.
3. Synkronisera projektet med Gradle-filerna.

## Användning

### ScrollState

`ScrollState`-klassen tillåter dig att hantera rullpositionen för dina composables. Den stöder både omedelbar och animerad rullning.

```kotlin
@Composable
fun rememberScrollState(initial: Int = 0): ScrollState {
    return rememberSaveable(saver = ScrollState.Saver) {
        ScrollState(initial = initial)
    }
}
```

### Modifier för rullning

Lägg till vertikalt eller horisontellt rullbeteende till dina composables med de medföljande modifierarna.

```kotlin
fun Modifier.verticalScroll(
    state: ScrollState,
    enabled: Boolean = true,
    flingBehavior: FlingBehavior? = null,
    reverseScrolling: Boolean = false
): Modifier

fun Modifier.horizontalScroll(
    state: ScrollState,
    enabled: Boolean = true,
    flingBehavior: FlingBehavior? = null,
    reverseScrolling: Boolean = false
): Modifier
```

### ScrollableListView

Denna komponent visar vertikalt rullbart innehåll.

```kotlin
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
```

### HorizontalScrollableContent

Denna komponent visar horisontellt rullbart innehåll.

```kotlin
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
```

### Förhandsgranskning

Förhandsgranska komponenterna i IDE:n.

```kotlin
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
```

### Hur man återanvänder

1. **Vertikalt rullbart innehåll**: Använd `ScrollableListView`-komponenten för att visa en lista med objekt som kan rullas vertikalt. Du kan anpassa objekten efter dina behov.

2. **Horisontellt rullbart innehåll**: Använd `HorizontalScrollableContent`-komponenten för att visa en lista med objekt som kan rullas horisontellt. Anpassa objekten efter behov.

### Exempel

#### Vertikalt rullbart innehåll

```kotlin
@Composable
fun ExampleVerticalScrollableListView() {
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
```

#### Horisontellt rullbart innehåll

```kotlin
@Composable
fun ExampleHorizontalScrollableContent() {
    HorizontalScrollableContent()
}
```


