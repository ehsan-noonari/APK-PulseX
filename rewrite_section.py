with open("app/src/main/java/com/example/ui/screens/SearchScreen.kt", "r") as f:
    lines = f.readlines()

with open("app/src/main/java/com/example/ui/screens/SearchScreen.kt", "w") as f:
    for i, line in enumerate(lines):
        if i >= 548:
            break
        f.write(line)
    f.write("\n@Composable\n")
    f.write("fun SectionHeader(title: String) {\n")
    f.write("    androidx.compose.material3.Text(\n")
    f.write("        text = title,\n")
    f.write("        color = com.example.ui.components.PulseXColors.Outline,\n")
    f.write("        fontSize = 12.sp,\n")
    f.write("        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,\n")
    f.write("        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)\n")
    f.write("    )\n")
    f.write("}\n")
