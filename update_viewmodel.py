import os

filepath = 'app/src/main/java/com/spinel/pdftools/ui/imagetopdf/ImageToPdfViewModel.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# Add reorderImages method
reorder_method = """    fun removeImage(uri: Uri) {
        _state.update { currentState ->
            currentState.copy(
                selectedUris = currentState.selectedUris.filter { it != uri }
            )
        }
    }

    fun reorderImages(fromIndex: Int, toIndex: Int) {
        _state.update { currentState ->
            val uris = currentState.selectedUris.toMutableList()
            if (fromIndex in uris.indices && toIndex in uris.indices) {
                val item = uris.removeAt(fromIndex)
                uris.add(toIndex, item)
                currentState.copy(selectedUris = uris)
            } else {
                currentState
            }
        }
    }"""

content = content.replace("""    fun removeImage(uri: Uri) {
        _state.update { currentState ->
            currentState.copy(
                selectedUris = currentState.selectedUris.filter { it != uri }
            )
        }
    }""", reorder_method)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
