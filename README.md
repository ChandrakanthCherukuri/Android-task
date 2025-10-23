# Google Maps Route and Place Search App

This Android application implements Google Maps integration with route drawing and place search functionality.

## Features

### Task 1: Route Drawing Between Two Points
- **Tap to set points**: Tap anywhere on the map to set source (green marker) and destination (red marker)
- **Automatic route drawing**: Once both points are set, a blue route line is drawn using Google Directions API
- **Route information**: Shows distance and duration in a toast message
- **Camera adjustment**: Automatically adjusts the camera to show the entire route

### Task 2: Place Search with Autocomplete
- **Search bar**: Type in the search field to find places using Google Places Autocomplete API
- **Live suggestions**: Get real-time place suggestions as you type (after 3+ characters)
- **Place selection**: Tap on any suggestion to navigate to that location
- **Marker placement**: Automatically places a marker and moves camera to selected place

## Additional Features
- **Current location**: Floating action button to navigate to your current location
- **Clear route**: Button to clear all markers and routes
- **Location permissions**: Handles location permission requests properly

## Setup Instructions

### 1. Get Google Maps API Key
1. Go to the [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable the following APIs:
   - Maps SDK for Android
   - Places API
   - Directions API
4. Create credentials (API Key)
5. Restrict the API key to your app's package name and SHA-1 fingerprint

### 2. Configure the API Key
Replace `YOUR_GOOGLE_MAPS_API_KEY` in the following files with your actual API key:
- `app/src/main/AndroidManifest.xml` (line 20)
- `app/src/main/java/com/example/task/MainActivity.kt` (line 48)

### 3. Build and Run
1. Open the project in Android Studio
2. Sync the project with Gradle files
3. Run the app on a device or emulator with Google Play Services

## Usage Instructions

### Drawing a Route
1. Open the app
2. Tap anywhere on the map to set the source location (green marker)
3. Tap another location to set the destination (red marker)
4. The route will automatically appear as a blue line
5. View distance and duration information in the toast message

### Searching for Places
1. Use the search bar at the top of the screen
2. Type at least 3 characters to see suggestions
3. Tap on any suggestion from the dropdown list
4. The map will navigate to the selected place and add a marker

### Other Features
- Tap the location button (bottom-right) to go to your current location
- Tap "Clear" to remove all markers and routes
- Grant location permission when prompted for better functionality

## Technical Implementation

### Dependencies Used
- Google Maps SDK for Android
- Google Places API
- Google Play Services Location
- Retrofit for API calls
- Kotlin Coroutines for async operations

### Architecture
- **MainActivity**: Main activity handling map interactions and UI
- **PlacesAdapter**: RecyclerView adapter for place suggestions
- **DirectionsApiService**: Retrofit interface for Google Directions API
- **DirectionsResponse**: Data models for API responses
- **PolylineDecoder**: Utility for decoding Google polyline strings

### Key Features Implemented
- Real-time place autocomplete search
- Route drawing with polyline decoding
- Location permission handling
- Camera management and bounds adjustment
- Marker customization (different colors for source/destination)

## Permissions Required
- `ACCESS_FINE_LOCATION`: For precise location access
- `ACCESS_COARSE_LOCATION`: For approximate location access
- `INTERNET`: For API calls
- `ACCESS_NETWORK_STATE`: For network status checking

## Note
Make sure to replace the placeholder API key with your actual Google Maps API key before running the application. The app will not function properly without a valid API key.
