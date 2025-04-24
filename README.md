# Auralis
Auralis is a premium Android music player with high-fidelity playback and seamless lyrics synchronization.

Here’s a quick “from zero to APK in Android Studio” checklist for your Auralis project:

1. Install & launch Android Studio  
   • Make sure you have Android Studio Arctic Fox (or newer) installed.  
   • JDK 11+ is recommended—Studio should bundle this by default.

2. Open your project  
   • On the Welcome screen choose **“Open”** and navigate to Auralis (the root of your repo).  
   • Studio will detect the Gradle settings and automatically start a **Gradle sync**.

3. Verify SDK & Gradle settings  
   • In **File > Project Structure**  
     – Confirm **Android Gradle Plugin** version matches your `build.gradle` (e.g. `7.x.x`).  
     – Make sure **Compile SDK** and **Target SDK** (in “Modules > app > Flavors”) are installed on your machine.  
   • If you see any “SDK not installed” errors in the sync log, click the quick‑fix to download the missing APIs.

4. Sync, Clean & Rebuild  
   • Click the **“Sync Project with Gradle Files”** toolbar button (the elephant icon).  
   • Once that finishes, go to **Build > Clean Project**, then **Build > Rebuild Project**.  
   • Watch the Build window for errors—address any missing dependencies or Kotlin compile errors (they’ll point to file/line).

5. Set up an emulator or device  
   • Open the **AVD Manager** (🏠 toolbar icon) and create a Pixel 3 (API 33+) virtual device.  
   • Or connect a USB device with **USB debugging** enabled.

6. Run your app  
   • In the top toolbar choose the “app” run configuration and select your emulator/device.  
   • Click **Run ▶** (or Shift+F10).  
   • Android Studio will install the APK and launch Auralis on your emulator/device.

7. Inspect logs & verify  
   • Open **Logcat** (bottom pane) and filter by your package (`com.humblebeeai.auralis`).  
   • Look for any runtime errors or missing‑permission warnings.  
   • Grant **READ_EXTERNAL_STORAGE** (or `READ_MEDIA_AUDIO` on Android 13+) if prompted.

8. (Optional) Generate a debug APK  
   • **Build > Build Bundle(s) / APK(s) > Build APK(s)**  
   • When it completes, click the notification to locate your `.apk` in `app/build/outputs/apk/debug`.

Once you see your Now Playing screen with lyrics on a device or emulator, your v0 MVP is officially built! Let me know if you hit any Gradle sync or runtime hiccups.