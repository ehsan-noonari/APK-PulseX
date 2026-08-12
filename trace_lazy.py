with open("app/src/main/java/com/example/ui/screens/BirdDetailScreen.kt", "r") as f:
    lines = f.readlines()
level = 0
lazy = 0
for i, line in enumerate(lines):
    if "LazyColumn" in line:
        lazy = level + 1
        print(f"LazyColumn start at line {i+1} with level {lazy}")
    
    for char in line:
        if char == '{': 
            level += 1
        elif char == '}': 
            level -= 1
            if lazy > 0 and level < lazy:
                print(f"LazyColumn ends at line {i+1}")
                lazy = 0
