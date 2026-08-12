with open("app/src/main/java/com/example/ui/screens/CryptoDetailScreen.kt", "r") as f:
    lines = f.readlines()
lines.insert(1239, "            }\n")
with open("app/src/main/java/com/example/ui/screens/CryptoDetailScreen.kt", "w") as f:
    f.writelines(lines)

with open("app/src/main/java/com/example/ui/screens/StockDetailScreen.kt", "r") as f:
    lines = f.readlines()
lines.insert(1308, "            }\n")
with open("app/src/main/java/com/example/ui/screens/StockDetailScreen.kt", "w") as f:
    f.writelines(lines)

