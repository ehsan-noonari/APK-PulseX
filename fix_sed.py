with open("app/src/main/java/com/example/ui/screens/CryptoDetailScreen.kt", "r") as f:
    c = f.read()

# Undo the bad sed replacement
c = c.replace("                    }\n            }", "                    }")

with open("app/src/main/java/com/example/ui/screens/CryptoDetailScreen.kt", "w") as f:
    f.write(c)

