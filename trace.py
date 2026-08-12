import sys
def check(file):
    with open(file, 'r') as f:
        c = f.read()
    level = 0
    lines = c.split('\n')
    started = False
    for i, line in enumerate(lines):
        for char in line:
            if char == '{': 
                level += 1
                started = True
            elif char == '}': 
                level -= 1
        if started and level <= 0:
            print(f"File {file} reaches level {level} at line {i+1}")
            started = False
check(sys.argv[1])
