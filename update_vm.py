import re

filepath = 'app/src/main/java/com/spinel/pdftools/ui/imagetopdf/ImageToPdfViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Replace state
old_state = '''data class ImageToPdfState(
    val pages: List<DocumentPage> = emptyList(),
    val generationState: GenerationState = GenerationState.Idle,
    val isTextEditorVisible: Boolean = false
)'''
new_state = '''data class ImageToPdfState(
    val pages: List<DocumentPage> = emptyList(),
    val generationState: GenerationState = GenerationState.Idle,
    val isTextEditorVisible: Boolean = false,
    val editingTextPageId: String? = null
)'''
content = content.replace(old_state, new_state)

# Replace showTextEditor and hideTextEditor
old_methods = '''    fun showTextEditor() {
        _state.update { it.copy(isTextEditorVisible = true) }
    }

    fun hideTextEditor() {
        _state.update { it.copy(isTextEditorVisible = false) }
    }

    fun addTextPage(title: String, body: String) {
        val newPage = DocumentPage.Text(title = title, body = body)
        _state.update { 
            it.copy(
                pages = it.pages + newPage,
                isTextEditorVisible = false
            )
        }
    }'''
new_methods = '''    fun showTextEditor(pageId: String? = null) {
        _state.update { it.copy(isTextEditorVisible = true, editingTextPageId = pageId) }
    }

    fun hideTextEditor() {
        _state.update { it.copy(isTextEditorVisible = false, editingTextPageId = null) }
    }

    fun saveTextPage(title: String, body: String, alignment: TextAlignment, fontSize: Int, isBold: Boolean, color: TextColor) {
        _state.update { currentState ->
            if (currentState.editingTextPageId != null) {
                val updatedPages = currentState.pages.map {
                    if (it.id == currentState.editingTextPageId && it is DocumentPage.Text) {
                        it.copy(title = title, body = body, alignment = alignment, fontSize = fontSize, isBold = isBold, color = color)
                    } else it
                }
                currentState.copy(pages = updatedPages, isTextEditorVisible = false, editingTextPageId = null)
            } else {
                val newPage = DocumentPage.Text(title = title, body = body, alignment = alignment, fontSize = fontSize, isBold = isBold, color = color)
                currentState.copy(pages = currentState.pages + newPage, isTextEditorVisible = false, editingTextPageId = null)
            }
        }
    }'''
content = content.replace(old_methods, new_methods)

with open(filepath, 'w') as f:
    f.write(content)
