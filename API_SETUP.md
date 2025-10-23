# API Configuration Instructions

## Setting up Google Maps API Key

To make this app work properly, you need to replace the placeholder API key with your actual Google Maps API key.

### Steps to get your API key:

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable these APIs:
   - Maps SDK for Android
   - Places API
   - Directions API
4. Create credentials (API Key)
5. Restrict the API key to your app's package name and SHA-1 fingerprint

### Replace the API key in these files:

1. **AndroidManifest.xml** (line 22):
   ```xml
   <meta-data
       android:name="com.google.android.geo.API_KEY"
       android:value="YOUR_ACTUAL_API_KEY_HERE" />
   ```

2. **MainActivity.kt** (line 54):
   ```kotlin
   private val apiKey = "YOUR_ACTUAL_API_KEY_HERE"
   ```

### Security Note:
- Never commit your actual API key to version control
- Consider using BuildConfig or local.properties for API key storage in production apps
- Add your API key to .gitignore if storing in a separate file

### Current Status:
❌ Placeholder API key is currently set
✅ All necessary permissions are configured
✅ Network security config is set up
✅ App architecture is ready
