import re

with open('app/src/test/java/com/spinel/pdftools/ui/createpdf/CreatePdfViewModelTest.kt', 'r') as f:
    content = f.read()

content = content.replace("class CreatePdfViewModelTest {", """import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CreatePdfViewModelTest {""")

with open('app/src/test/java/com/spinel/pdftools/ui/createpdf/CreatePdfViewModelTest.kt', 'w') as f:
    f.write(content)
