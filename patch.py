import os

app_open_file = "app/src/main/java/com/spinel/pdftools/monetization/AppOpenAdManager.kt"
main_file = "app/src/main/java/com/spinel/pdftools/MainActivity.kt"

# Patch AppOpenAdManager
with open(app_open_file, "r") as f:
    content = f.read()

target1 = """    private var backgroundTime: Long = 0L
    private var currentActivity: Activity? = null"""

replacement1 = """    private var backgroundTime: Long = 0L
    private var currentActivity: Activity? = null
    private var suppressNextAd = false

    fun suppressNextAppOpen() {
        suppressNextAd = true
    }"""

target2 = """        if (isMeaningfulReturn) {
            Log.d(LOG_TAG, "Meaningful return or cold start detected. (Time since bg: $timeSinceBackground ms)")
            val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
            if (consentInformation.canRequestAds()) {
                 showAdIfAvailable(activity)
            }
        } else {"""

replacement2 = """        if (isMeaningfulReturn) {
            Log.d(LOG_TAG, "Meaningful return or cold start detected. (Time since bg: $timeSinceBackground ms)")
            val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
            if (consentInformation.canRequestAds()) {
                 if (suppressNextAd) {
                     Log.d(LOG_TAG, "App Open Ad suppressed for this foreground transition.")
                     suppressNextAd = false
                 } else {
                     showAdIfAvailable(activity)
                 }
            }
        } else {"""

if target1 in content and target2 in content:
    content = content.replace(target1, replacement1)
    content = content.replace(target2, replacement2)
    with open(app_open_file, "w") as f:
        f.write(content)
    print("SUCCESS AppOpenAdManager")
else:
    print("FAILED AppOpenAdManager")

# Patch MainActivity
with open(main_file, "r") as f:
    content = f.read()

target3 = """            val uri = Uri.parse(url)
            val viewIntent = Intent(Intent.ACTION_VIEW, uri)
            viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(viewIntent)"""

replacement3 = """            val uri = Uri.parse(url)
            val viewIntent = Intent(Intent.ACTION_VIEW, uri)
            viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            AppOpenAdManager.suppressNextAppOpen()
            startActivity(viewIntent)"""

if target3 in content:
    content = content.replace(target3, replacement3)
    with open(main_file, "w") as f:
        f.write(content)
    print("SUCCESS MainActivity")
else:
    print("FAILED MainActivity")
