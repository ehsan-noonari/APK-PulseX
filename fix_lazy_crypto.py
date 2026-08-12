with open("app/src/main/java/com/example/ui/screens/CryptoDetailScreen.kt", "r") as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "itemsIndexed(perfGains, key = { _, it -> it.first }) { index, (period, change) ->" in line:
        pass

# Wait, instead of parsing, let's just insert an extra } before line 1176!
lines.insert(1175, "                    }\n")

with open("app/src/main/java/com/example/ui/screens/CryptoDetailScreen.kt", "w") as f:
    f.writelines(lines)
