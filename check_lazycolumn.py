def find_lazycolumn_end(file):
    with open(file, 'r') as f:
        lines = f.readlines()
        
    for i, line in enumerate(lines):
        if 'LazyColumn' in line:
            print(f"LazyColumn found at line {i+1}")
            
find_lazycolumn_end("app/src/main/java/com/example/ui/screens/CryptoDetailScreen.kt")
