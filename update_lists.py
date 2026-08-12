import os
import re

SCREEN_DIR = "app/src/main/java/com/example/ui/screens"

def ensure_imports(content, imports):
    for imp in imports:
        if imp not in content:
            # Find last import
            last_import = content.rfind("import ")
            if last_import != -1:
                end_of_line = content.find("\n", last_import)
                content = content[:end_of_line] + f"\n{imp}" + content[end_of_line:]
    return content

for root, _, files in os.walk(SCREEN_DIR):
    for f in files:
        if f.endswith(".kt"):
            path = os.path.join(root, f)
            with open(path, "r") as file:
                content = file.read()
            
            original_content = content
            
            # This regex looks for items(listName) { itemVar -> 
            # or items(listName, key = ...) { itemVar ->
            # And converts to itemsIndexed
            # However, this is quite risky without a precise parser.
            # I will just write a few precise replacements for the known screens.
            
            content = ensure_imports(content, [
                "import androidx.compose.foundation.lazy.itemsIndexed",
                "import com.example.ui.components.scrollFadeIn"
            ])
            
            if content != original_content:
                with open(path, "w") as file:
                    file.write(content)

