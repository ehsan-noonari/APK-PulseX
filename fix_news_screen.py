import re

with open("app/src/main/java/com/example/ui/screens/NewsScreen.kt", "r") as f:
    content = f.read()

content = content.replace(
    "androidx.compose.foundation.layout.Box(modifier = Modifier.animateItem().scrollFadeIn(staggerIndex = index)) {\n                ArticleRowItem(article) { onNavigateToArticle(article.id) }\n            }\n                Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 20.dp))",
    "androidx.compose.foundation.layout.Column(modifier = Modifier.animateItem().scrollFadeIn(staggerIndex = index)) {\n                ArticleRowItem(article) { onNavigateToArticle(article.id) }\n                Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 20.dp))\n            }"
)

with open("app/src/main/java/com/example/ui/screens/NewsScreen.kt", "w") as f:
    f.write(content)
