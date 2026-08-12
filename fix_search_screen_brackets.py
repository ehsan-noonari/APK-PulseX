with open("app/src/main/java/com/example/ui/screens/SearchScreen.kt", "r") as f:
    content = f.read()

# For stocks, the block ends with:
# Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 20.dp))
#                         }
# We need an extra `}` for the Column.
content = content.replace(
    "Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 20.dp))\n                        }",
    "Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 20.dp))\n                        }\n                    }"
)
with open("app/src/main/java/com/example/ui/screens/SearchScreen.kt", "w") as f:
    f.write(content)
