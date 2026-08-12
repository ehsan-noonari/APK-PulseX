def check(file):
    with open(file, 'r') as f:
        c = f.read()
    level = 0
    for char in c:
        if char == '{': level += 1
        elif char == '}': level -= 1
    print(f"File {file} final level: {level}")

check("app/src/main/java/com/example/ui/screens/CryptoDetailScreen.kt")
check("app/src/main/java/com/example/ui/screens/StockDetailScreen.kt")
check("app/src/main/java/com/example/ui/screens/BirdDetailScreen.kt")
