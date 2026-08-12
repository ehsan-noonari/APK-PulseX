with open("app/src/main/java/com/example/ui/screens/SearchScreen.kt", "r") as f:
    content = f.read()

# Fix stocks
content = content.replace(
    "itemsIndexed(filteredStocks, key = { _, it -> it.symbol }) { index, stock ->\n            androidx.compose.foundation.layout.Box(modifier = Modifier.animateItem().scrollFadeIn(staggerIndex = index)) {",
    "itemsIndexed(filteredStocks, key = { _, it -> it.symbol }) { index, stock ->\n            androidx.compose.foundation.layout.Column(modifier = Modifier.animateItem().scrollFadeIn(staggerIndex = index)) {"
)

# Fix cryptos
content = content.replace(
    "itemsIndexed(filteredCryptos, key = { _, it -> it.symbol }) { index, crypto ->\n            androidx.compose.foundation.layout.Box(modifier = Modifier.animateItem().scrollFadeIn(staggerIndex = index)) {",
    "itemsIndexed(filteredCryptos, key = { _, it -> it.symbol }) { index, crypto ->\n            androidx.compose.foundation.layout.Column(modifier = Modifier.animateItem().scrollFadeIn(staggerIndex = index)) {"
)

# Fix news
content = content.replace(
    "itemsIndexed(filteredNews, key = { _, it -> it.id }) { index, article ->\n            androidx.compose.foundation.layout.Box(modifier = Modifier.animateItem().scrollFadeIn(staggerIndex = index)) {",
    "itemsIndexed(filteredNews, key = { _, it -> it.id }) { index, article ->\n            androidx.compose.foundation.layout.Column(modifier = Modifier.animateItem().scrollFadeIn(staggerIndex = index)) {"
)

# Wait, we need to make sure the bracket is closed! For stocks and cryptos, they didn't match the second sed command.
# So they are missing the closing bracket for the Column.

