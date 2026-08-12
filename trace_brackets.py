def check(file):
    with open(file, 'r') as f:
        c = f.read()
    level = 0
    lines = c.split('\n')
    for i, line in enumerate(lines):
        for char in line:
            if char == '{': level += 1
            elif char == '}': level -= 1
        if level <= 0 and i < len(lines) - 2:
            print(f"File {file} reaches level 0 at line {i+1}")

check("app/src/main/java/com/example/ui/screens/NewsScreen.kt")
check("app/src/main/java/com/example/ui/screens/SearchScreen.kt")
