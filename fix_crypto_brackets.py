with open("app/src/main/java/com/example/ui/screens/CryptoDetailScreen.kt", "r") as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if "androidx.compose.foundation.layout.Box(modifier = Modifier.animateItem().scrollFadeIn(staggerIndex = index)) {" in line:
        continue
    if line.strip() == "}":
        # we have a bunch of these that we added
        pass
    
    # Actually, the sed command was:
    # sed -i 's/                    }/                    }\n            }/g'
    
    # We can just undo that. We'll replace `                    }\n            }` with `                    }`
