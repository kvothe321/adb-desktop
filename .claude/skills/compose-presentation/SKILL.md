---
name: compose-presentation
description: Load before writing or editing any presentation code in this repo, meaning feature modules, desktopApp UI, or adbdesktop-ui-kit components. Covers the MVI contract, ViewModel as reducer, state hoisting (Screen vs Content), previews, Compose lifecycle, stability, keys, side effects and the design system boundary.
---

# Compose presentation layer

Follows the official Compose guides (UI architecture, state hoisting, lifecycle, side effects) plus the
rules this repo enforces with compose-rules, detekt and ktlint.

## MVI contract per screen

Each screen has a `contract` package with up to three files:

| File | Content |
|---|---|
| `XUiState.kt` | Immutable `data class` (or `sealed interface` of data classes) with defaults. Derived legality lives in computed properties (`val canConnect: Boolean get() = ...`). No navigation flags. |
| `XIntent.kt` | `sealed interface` of what the user did, named in the past or as a fact (`ConnectClicked`, `AddressChanged(value)`), never as a command (`DoConnect`). `data object` for no payload. |
| `XSideEffect.kt` | One-shot events that must not be replayed: messages, navigation. `sealed interface`. |

## ViewModel is the reducer

- One public entry point: `fun reduceIntent(intent: XIntent)`. No other public mutators.
- State: `private val _uiState = MutableStateFlow(XUiState())`, exposed as `val uiState: StateFlow<XUiState>`.
  Update with `_uiState.update { it.copy(...) }` (atomic).
- Effects: `private val _sideEffects = Channel<XSideEffect>(Channel.BUFFERED)`, exposed as `receiveAsFlow()`.
- Guard illegal intents in the reducer too (`if (!uiState.value.canConnect) return`), never trust the view.
- Depend on domain use cases only. Map `DomainError` to presentation types (a string resource key, not text).
- Long-lived streams: `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)`.
- Everything shown is observed: repositories expose flows that re-emit on any change, so a ViewModel never reloads
  after a mutation and screens need no refresh or retry button.
- ViewModels outlive their screen. Wrap anything that polls the device in `whileActive(isVisible)`, fed by
  `VisibilityEffect` in the Screen, so hidden screens do no work.
- Unit test every ViewModel with fakes from `core:testing`, Kotest and Turbine; set `Dispatchers.setMain` per spec.

## State hoisting: Screen and Content

- `XScreen(modifier, viewModel = koinViewModel())` is the only composable that sees the ViewModel. It collects
  `uiState` with `collectAsStateWithLifecycle()`, collects side effects in one `LaunchedEffect(viewModel)`,
  and calls `XContent`.
- `XContent(uiState, onIntent, modifier)` is stateless: plain data in, a single `onIntent: (XIntent) -> Unit` out.
  Every child below it gets only the values it needs plus lambdas, never the ViewModel or the whole state if a
  few fields suffice.
- Hoist to the lowest common ancestor. Purely visual state nobody else needs (a dropdown being open, hover) stays
  local with `remember`/`rememberSaveable`.
- Stateful parameters get defaults (`listState: LazyListState = rememberLazyListState()`) so components stay
  reusable and previewable.

## Previews

- Every public composable and every Screen's Content has previews. Annotate with `@UiKitPreview` only (never
  `@Preview` directly) and wrap in
  `PreviewContext { }` so light and dark both render.
- Preview functions are `private` and end with `Preview` (`DevicesContentEmptyPreview`).
- Preview each meaningful state: loading, empty, error, content, selected.

## Lifecycle, stability, keys

- UI changes only through state. No work in the composable body; use effect APIs.
- Pass immutable, stable types. Use `@Immutable`/`@Stable` on UI models only when the contract truly holds.
- Lazy lists always pass `key = { it.id }` with a stable unique id (serial, package name), never the index.
  Loops that can reorder use `key(id) { }`.
- Lambdas passed to children are cheap and stable; prefer method references or `remember`ed lambdas only when
  profiling shows it matters.

## Side effects

- `LaunchedEffect(keys)`: every value read inside is a key, unless wrapped in `rememberUpdatedState`.
  `LaunchedEffect(Unit)`/`(true)` is a smell: justify it.
- Event handlers launch coroutines with `rememberCoroutineScope()`, never `LaunchedEffect`.
- `DisposableEffect` always has a meaningful `onDispose`.
- `derivedStateOf` only when inputs change more often than the output.

## Design system boundary

- Feature modules and desktopApp import UI only from `com.tlpcraft.adbdesktop.uikit`. Material, colors, shapes,
  fonts, `background` and `border` are forbidden there (detekt `ForbiddenImport`, and Material is not on their
  classpath).
- Layout primitives from foundation (`Row`, `Column`, `Box`, `LazyColumn`, padding, size) are fine; spacing
  comes from `AppTheme.spacing`.
- ui-kit components are feature-agnostic and closed: no `color`, `shape`, `textStyle`, `contentPadding`
  overrides. Variation goes through enums (`variant`, `size`, `tone`). `modifier` is layout only.
- Feature-specific composables (a device row) live in the feature, built from ui-kit components.

## compose-rules checklist

Every content-emitting public composable takes `modifier: Modifier = Modifier` as the first optional parameter
and applies it to the root. Parameter order: required, `modifier`, optional, trailing content lambda. Event
lambdas use present tense (`onSelect`, not `onSelected`). One root emitter per composable. ViewModels are
injected with `koinViewModel()` as a default parameter of the Screen only.
