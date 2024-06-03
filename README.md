
# Kotlin Compose Scrollable Components

Denna komponentbibliotek erbjuder scrollbara vyer i Jetpack Compose för både vertikal och horisontell scrollning. Du kan enkelt integrera dessa komponenter i dina Compose-projekt för att hantera scrollbar layout.

## Funktioner

- **Vertikal scrollning** med `verticalScroll`.
- **Horisontell scrollning** med `horizontalScroll`.
- Möjlighet att animera och hoppa till specifika scrollpositioner.
- Interaktionskällor för att lyssna på användarinteraktioner.

## Installation

För att använda dessa scrollbara komponenter, kopiera `ScrollComponent.kt` till din projektmapp under `ui.components`.

## Användning

### Vertikal Scrollbar Detaljvy

För att skapa en vertikal scrollbar detaljvy, använd `MyScrollableDetailView`-komponenten.

```kotlin
@Composable
fun MyScrollableDetailView(content: String) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = content,
            modifier = Modifier
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.1f))
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                coroutineScope.launch {
                    scrollState.animateScrollTo(0)
                }
            }) {
                Text("Scroll to Top")
            }
            Button(onClick = {
                coroutineScope.launch {
                    scrollState.animateScrollTo(scrollState.maxValue)
                }
            }) {
                Text("Scroll to Bottom")
            }
            Button(onClick = {
                coroutineScope.launch {
                    scrollState.scrollTo(scrollState.maxValue)
                }
            }) {
                Text("Jump to Bottom")
            }
        }
    }
}
```

### Horisontell Scrollbar Innehållsvy

För att skapa en horisontell scrollbar innehållsvy, använd `MyHorizontalScrollableContent`-komponenten.

```kotlin
@Composable
fun MyHorizontalScrollableContent() {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        for (i in 1..20) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Text("Item $i", modifier = Modifier.align(Alignment.Center))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                coroutineScope.launch {
                    scrollState.animateScrollTo(0)
                }
            }) {
                Text("Scroll to Start")
            }
            Button(onClick = {
                coroutineScope.launch {
                    scrollState.animateScrollTo(scrollState.maxValue)
                }
            }) {
                Text("Scroll to End")
            }
            Button(onClick = {
                coroutineScope.launch {
                    scrollState.scrollTo(scrollState.maxValue)
                }
            }) {
                Text("Jump to End")
            }
        }
    }
}
```

## ScrollState

ScrollState används för att hantera scrollpositioner och tillhandahålla metoder för att animera eller hoppa till specifika positioner.

### Funktioner

- **scrollTo(value: Int)**: Hoppar omedelbart till en given position i pixlar.
- **animateScrollTo(value: Int, animationSpec: AnimationSpec<Float>)**: Animerar scrollningen till en given position i pixlar.

### Användningsexempel

```kotlin
val scrollState = rememberScrollState()

// Scrolla till toppen
coroutineScope.launch {
    scrollState.scrollTo(0)
}

// Animerad scrollning till botten
coroutineScope.launch {
    scrollState.animateScrollTo(scrollState.maxValue)
}
```

## Modifier Extensions

- **Modifier.verticalScroll**: Lägger till vertikal scrollfunktionalitet.
- **Modifier.horizontalScroll**: Lägger till horisontell scrollfunktionalitet.

### Exempel

```kotlin
Column(
    modifier = Modifier.verticalScroll(scrollState)
) {
    // Innehåll
}

Row(
    modifier = Modifier.horizontalScroll(scrollState)
) {
    // Innehåll
}
```

## Preview

För att förhandsgranska komponenterna, använd `@Preview`-anoteringen:

```kotlin
@Preview(showBackground = true)
@Composable
fun PreviewMyScrollableDetailView() {
    MyScrollableDetailView(content = "Detta är en lång text för att visa hur scroll fungerar i en detaljvy. " +
            "Du kan lägga till så mycket text du vill här för att se scrollbeteendet.")
}

@Preview(showBackground = true)
@Composable
fun PreviewMyHorizontalScrollableContent() {
    MyHorizontalScrollableContent()
}
```

## Slutsats

Denna scrollbara komponentbibliotek erbjuder enkel och flexibel hantering av både vertikal och horisontell scrollning i Jetpack Compose. Anpassa komponenterna efter dina behov och integrera dem i dina projekt för att förbättra användarupplevelsen.

För ytterligare information och exempel, se källkoden i `ScrollComponent.kt`.
