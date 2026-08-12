with open("app/src/main/java/com/example/ui/screens/StockDetailScreen.kt", "r") as f:
    lines = f.readlines()
lines.insert(1243, "                    }\n")
with open("app/src/main/java/com/example/ui/screens/StockDetailScreen.kt", "w") as f:
    f.writelines(lines)
