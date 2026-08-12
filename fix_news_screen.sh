sed -i 's/androidx.compose.foundation.layout.Box(modifier = Modifier.animateItem().scrollFadeIn(staggerIndex = index)) {/androidx.compose.foundation.layout.Column(modifier = Modifier.animateItem().scrollFadeIn(staggerIndex = index)) {/g' app/src/main/java/com/example/ui/screens/NewsScreen.kt
sed -i '/ArticleRowItem(article) { onNavigateToArticle(article.id) }/d' app/src/main/java/com/example/ui/screens/NewsScreen.kt
sed -i '/Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 20.dp))/d' app/src/main/java/com/example/ui/screens/NewsScreen.kt

sed -i 's/            }//g' app/src/main/java/com/example/ui/screens/NewsScreen.kt

# wait, I messed up the curly braces in NewsScreen.kt.
# Let's just download it and use python to fix it properly.
