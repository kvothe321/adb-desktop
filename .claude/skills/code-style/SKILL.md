---
name: code-style
description: Use when writing or editing any Kotlin, Gradle, Markdown, strings.xml or commit message in this repo. Covers comments, naming, user-facing text and punctuation rules.
---

# Code style

## Code explains itself

- Prefer clear names and small functions over comments. If a comment restates the code, delete it.
- Comment only the non-obvious *why*: a platform quirk, a protocol detail, a deliberate trade-off.
  One or two lines, written for a reader who has never seen this conversation.
- KDoc is required only on public `adbdesktop-ui-kit` components (one short paragraph: what it is,
  plus any constraint on `modifier`). Elsewhere, add KDoc only when the contract is not obvious
  from the signature.
- Never write session or process notes into code, docs or commits: no "as requested", "new",
  "refactored from", "previously", "TODO for later", step numbers, or references to other projects.
- No section-divider comments (`// ── Helpers ──`). Split the file instead.
- No dead code, no commented-out code. Unfinished work gets a linked note in `docs/`, not a TODO.

## Idiomatic Kotlin

- Expression bodies for single-expression functions; `when` over `if`/`else` chains.
- Null safety through `?.`, `?:`, `let`/`takeIf` and smart casts, never `!!`.
- `val` and immutable collections by default; `data class`, `sealed interface`, `enum class` and `value class`
  to model data and states.
- Collection operations (`map`, `filter`, `associate`, `firstOrNull`, `buildList`) over manual loops and
  mutable accumulators, unless a loop is clearly simpler.
- Extension functions for mappers and small helpers; named arguments when a call has several values of the
  same type or boolean flags.
- Scope functions only where they make the intent clearer; do not nest them.
- `require`/`check`/`error` for preconditions, not hand-thrown `IllegalArgumentException`/`IllegalStateException`.
- No Java-isms: no getters/setters, no static utility classes, no `Optional`, no explicit `Unit` returns.

## Punctuation

- Never use the em dash character (U+2014). Use a comma, colon, parentheses or a plain hyphen.

## User-facing text

- Every string a user can see lives in the module's `src/commonMain/composeResources/values/strings.xml`
  and is read with `stringResource(Res.string.x)`. No hardcoded UI text, so the app can be localized.
- Names are `snake_case`, prefixed by screen or component (`devices_empty_title`).
- Write `'` and `"` literally inside `<string>` content. The Compose resource generator is not the
  Android resource compiler: `\'` renders a literal backslash, `&apos;`/`&quot;` are needless noise.
- Domain and data layers never produce display text. `DomainError.message` is for logs only; map
  errors to string resources in presentation.

## Commits

- Conventional Commits, short subject (the `commit-msg` hook enforces 50 characters).
- Add a body only when the subject cannot carry the reason, and keep it to one or two lines.
- No AI attribution trailers.
