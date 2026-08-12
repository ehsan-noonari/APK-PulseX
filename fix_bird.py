with open("app/src/main/java/com/example/ui/screens/BirdDetailScreen.kt", "r") as f:
    lines = f.readlines()
lines.insert(492, "            }\n")
with open("app/src/main/java/com/example/ui/screens/BirdDetailScreen.kt", "w") as f:
    f.writelines(lines)
