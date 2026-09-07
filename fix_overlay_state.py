import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/imagetopdf/ImageToPdfScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace("GenerationOverlay(state.generationState)", "GenerationOverlay(state.generationState)") # Oh wait, before it was state
content = content.replace("when (state.generationState) {\n                    is GenerationState.Generating -> {", "when (state) {\n                    is GenerationState.Generating -> {")

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
