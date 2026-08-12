def check(file):
    with open(file, 'r') as f:
        c = f.read()
    level = 0
    for idx, char in enumerate(c):
        if char == '{': level += 1
        elif char == '}': level -= 1
        if level < 0:
            print(f"Underflow at {idx} in {file}")
            break
    print(f"{file} final level: {level}")

check("app/src/main/java/com/example/ui/screens/SearchScreen.kt")
check("app/src/main/java/com/example/ui/screens/NewsScreen.kt")
check("app/src/main/java/com/example/ui/screens/WatchlistScreen.kt")
