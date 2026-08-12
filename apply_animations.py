import os
import re

SCREEN_DIR = "app/src/main/java/com/example/ui/screens"
COMPONENTS_DIR = "app/src/main/java/com/example/ui/components"

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
            
            modified = False
            
            # 1. Add bounceClick to standard clickable
            if ".clickable {" in content:
                content = content.replace(".clickable {", ".bounceClick {")
                modified = True

            # 2. Add imports
            imports_needed = []
            if modified:
                imports_needed.append("import com.example.ui.components.bounceClick")
                
            if "scrollFadeIn" in content:
                imports_needed.append("import com.example.ui.components.scrollFadeIn")
                
            if imports_needed:
                content = ensure_imports(content, imports_needed)
                
            if modified:
                with open(path, "w") as file:
                    file.write(content)

