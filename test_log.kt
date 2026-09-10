fun getUmpTestDeviceIdFromLogcat(): String? {
    try {
        val process = Runtime.getRuntime().exec("logcat -d")
        val reader = java.io.BufferedReader(java.io.InputStreamReader(process.inputStream))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            if (line!!.contains("addTestDeviceHashedId")) {
                // Example log: Use new ConsentDebugSettings.Builder().addTestDeviceHashedId("ABCDEF123456")
                val regex = "addTestDeviceHashedId\\(\"([A-Fa-f0-9]+)\"\\)".toRegex()
                val match = regex.find(line!!)
                if (match != null) {
                    return match.groupValues[1]
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}
