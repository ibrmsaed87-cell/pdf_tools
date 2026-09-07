import sys
import re

filepath = 'app/src/main/java/com/spinel/pdftools/utils/PdfCompressor.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Update CompressionDiagnostics
new_diag = """    var fallbackException: String = "",
    var contentStreamsFound: Int = 0,
    var contentStreamsCompressed: Int = 0,
    var contentStreamsSkipped: Int = 0,
    var formStreamsCompressed: Int = 0,
    var monochromeFallbackBlocked: Boolean = false,
    var monochromeFallbackBlockReason: String = "",
    var finalDecision: String = ""
"""
content = re.sub(r'var fallbackException: String = "",\s*var finalDecision: String = ""', new_diag, content)

new_report = """            Fallback exception: ${if (fallbackException.isNotEmpty()) fallbackException else "None"}
            
            Content streams found: $contentStreamsFound
            Content streams Flate-compressed: $contentStreamsCompressed
            Content streams skipped (already filtered): $contentStreamsSkipped
            Form streams compressed: $formStreamsCompressed
            Monochrome fallback blocked: ${monochromeFallbackBlocked.toString().uppercase()}
            Block reason: ${if (monochromeFallbackBlockReason.isNotEmpty()) monochromeFallbackBlockReason else "N/A"}
            
            Final decision: $finalDecision"""
content = re.sub(r'Fallback exception: \$\{if \(fallbackException\.isNotEmpty\(\)\) fallbackException else "None"\}\s*Final decision: \$finalDecision', new_report, content)

# 2. Add isMeaningfulReduction and optimizeStreamInPlace
helpers = """    private fun isMeaningfulReduction(original: Long, newSize: Long): Boolean {
        if (newSize <= 0 || newSize >= original) return false
        val savedBytes = original - newSize
        return (savedBytes.toDouble() / original.toDouble()) >= 0.01
    }

    private fun optimizeStreamInPlace(stream: COSStream, diag: CompressionDiagnostics, processed: MutableSet<COSStream>, isForm: Boolean = false) {
        if (!processed.add(stream)) return
        val filters = stream.getDictionaryObject(COSName.FILTER)
        if (filters != null) {
            diag.contentStreamsSkipped++
            return
        }
        try {
            val bytes = stream.createInputStream().readBytes()
            stream.createOutputStream(COSName.FLATE_DECODE).use { os ->
                os.write(bytes)
            }
            if (isForm) diag.formStreamsCompressed++ else diag.contentStreamsCompressed++
        } catch (e: Exception) {
            Log.e(TAG, "Failed to optimize stream", e)
        }
    }
    
    private fun compressBitmap"""

content = content.replace('    private fun compressBitmap', helpers)

# 3. Insert lossless logic and tracking CCITT/JBIG2/1-bit
lossless_logic = """            val eligibility = CompressionEligibilityDetector.checkEligibility(document)
            diag.fallbackEligible = eligibility.first
            diag.fallbackRejectionReason = eligibility.second
            
            val processedStreams = mutableSetOf<COSStream>()
            for (page in pages) {
                ensureActive()
                val contents = page.cosObject.getDictionaryObject(COSName.CONTENTS)
                if (contents is COSStream) {
                    diag.contentStreamsFound++
                    optimizeStreamInPlace(contents, diag, processedStreams, false)
                } else if (contents is com.tom_roush.pdfbox.cos.COSArray) {
                    for (i in 0 until contents.size()) {
                        val element = contents.getObject(i)
                        if (element is COSStream) {
                            diag.contentStreamsFound++
                            optimizeStreamInPlace(element, diag, processedStreams, false)
                        }
                    }
                }
                
                val resources = page.resources ?: continue
                for (name in resources.xObjectNames) {
                    val xObject = resources.getXObject(name)
                    if (xObject is com.tom_roush.pdfbox.pdmodel.graphics.form.PDFormXObject) {
                        val cosStream = xObject.cosObject
                        if (cosStream is COSStream) {
                            optimizeStreamInPlace(cosStream, diag, processedStreams, true)
                        }
                    }
                }
            }
"""
content = re.sub(
    r'val eligibility = CompressionEligibilityDetector\.checkEligibility\(document\)\s*diag\.fallbackEligible = eligibility\.first\s*diag\.fallbackRejectionReason = eligibility\.second',
    lossless_logic,
    content
)

# 4. Update the uniqueImages loop to check JBIG2, CCITT, 1-bit
image_check_loop = """                        if (uniqueImages.add(xObject.cosObject)) {
                            // Extract metadata for diagnostics on unique images
                            val filtersObj = xObject.cosObject.getDictionaryObject(COSName.FILTER)
                            val filterNames = mutableListOf<String>()
                            if (filtersObj is COSName) filterNames.add(filtersObj.name)
                            else if (filtersObj is com.tom_roush.pdfbox.cos.COSArray) {
                                for (i in 0 until filtersObj.size()) {
                                    val f = filtersObj.getObject(i)
                                    if (f is COSName) filterNames.add(f.name)
                                }
                            }
                            if (filterNames.isEmpty()) filterNames.add("None")
                            filterNames.forEach { f -> diag.filters[f] = diag.filters.getOrDefault(f, 0) + 1 }
                            
                            val bits = xObject.bitsPerComponent
                            val bitsStr = "${bits}-bit"
                            diag.bitsPerComponent[bitsStr] = diag.bitsPerComponent.getOrDefault(bitsStr, 0) + 1
                        }"""
new_image_check_loop = """                        if (uniqueImages.add(xObject.cosObject)) {
                            val filtersObj = xObject.cosObject.getDictionaryObject(COSName.FILTER)
                            val filterNames = mutableListOf<String>()
                            if (filtersObj is COSName) filterNames.add(filtersObj.name)
                            else if (filtersObj is com.tom_roush.pdfbox.cos.COSArray) {
                                for (i in 0 until filtersObj.size()) {
                                    val f = filtersObj.getObject(i)
                                    if (f is COSName) filterNames.add(f.name)
                                }
                            }
                            if (filterNames.isEmpty()) filterNames.add("None")
                            filterNames.forEach { f -> diag.filters[f] = diag.filters.getOrDefault(f, 0) + 1 }
                            
                            val bits = xObject.bitsPerComponent
                            val bitsStr = "${bits}-bit"
                            diag.bitsPerComponent[bitsStr] = diag.bitsPerComponent.getOrDefault(bitsStr, 0) + 1
                        }"""

# Actually, I can just calculate ccitt/jbig2 and 1-bit directly from the maps after the loop!
map_checks = """            val totalImages = uniqueImages.size
            diag.totalImagesFound = totalImages
            
            if (diag.fallbackEligible) {
                var efficientCount = diag.filters.getOrDefault("JBIG2Decode", 0) + diag.filters.getOrDefault("CCITTFaxDecode", 0)
                var oneBitCount = diag.bitsPerComponent.getOrDefault("1-bit", 0)
                if (efficientCount > 0) {
                    diag.fallbackEligible = false
                    diag.monochromeFallbackBlocked = true
                    diag.monochromeFallbackBlockReason = "Contains CCITT/JBIG2 images"
                } else if (totalImages > 0 && oneBitCount >= totalImages / 2) {
                    diag.fallbackEligible = false
                    diag.monochromeFallbackBlocked = true
                    diag.monochromeFallbackBlockReason = "Predominantly 1-bit images ($oneBitCount / $totalImages)"
                }
            }"""

content = re.sub(
    r'val totalImages = uniqueImages\.size\s*diag\.totalImagesFound = totalImages',
    map_checks,
    content
)

# 5. Meaningful reduction check logic
decisions = """            val primarySize = primaryFile.length()
            diag.primarySize = primarySize

            if (isMeaningfulReduction(originalSize, primarySize)) {
                diag.finalDecision = "PRIMARY_SELECTED"
                return@withContext CompressionResult(primaryFile, diag)
            }

            if (!diag.fallbackEligible) {
                primaryFile.delete()
                diag.finalDecision = "NOT_REDUCED"
                return@withContext CompressionResult(null, diag) 
            }"""

content = re.sub(
    r'val primarySize = primaryFile\.length\(\)\s*diag\.primarySize = primarySize\s*if \(primarySize < originalSize\) \{\s*diag\.finalDecision = "PRIMARY_SELECTED"\s*return@withContext CompressionResult\(primaryFile, diag\)\s*\}\s*if \(!diag\.fallbackEligible\) \{\s*diag\.finalDecision = "NOT_REDUCED"\s*return@withContext CompressionResult\(primaryFile, diag\)\s*\}',
    decisions,
    content
)

fallback_decisions = """            val fallbackSize = if (fallbackSuccess && fallbackFile.exists()) fallbackFile.length() else 0L
            diag.fallbackSize = fallbackSize

            if (fallbackSuccess && isMeaningfulReduction(originalSize, fallbackSize) && fallbackSize < primarySize) {
                primaryFile.delete()
                diag.finalDecision = "FALLBACK_SELECTED"
                return@withContext CompressionResult(fallbackFile, diag)
            } else {
                fallbackFile.delete()
                primaryFile.delete()
                diag.finalDecision = "NOT_REDUCED"
                return@withContext CompressionResult(null, diag)
            }"""

content = re.sub(
    r'val fallbackSize = if \(fallbackSuccess && fallbackFile\.exists\(\)\) fallbackFile\.length\(\) else 0L\s*diag\.fallbackSize = fallbackSize\s*if \(fallbackSuccess && fallbackSize < primarySize && fallbackSize < originalSize\) \{\s*primaryFile\.delete\(\)\s*diag\.finalDecision = "FALLBACK_SELECTED"\s*return@withContext CompressionResult\(fallbackFile, diag\)\s*\} else \{\s*fallbackFile\.delete\(\)\s*diag\.finalDecision = "NOT_REDUCED"\s*return@withContext CompressionResult\(primaryFile, diag\)\s*\}',
    fallback_decisions,
    content
)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print("Updated PdfCompressor.kt")
