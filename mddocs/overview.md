# API Overview

- [Understand core concepts](#Understand-core-concepts)
- [Dependencies](#Dependencies)
- [Setup API](#Setup-API)

## Understand core concepts
Video Editor API includes 2 main core modules
- ```Playback API```
- ```Export API```

### Playback API
[VideoPlayer](playback/ve-playback-sdk/com.banuba.sdk.playback/-video-player/index.md) is a core of Playback API.  ```VideoPlayer``` is implemented in a similar way like other media players.
Main concepts
1. Add video playlist you want to play
2. Manage actions i.e. play, pause, change volume etc.
3. Manage effects
4. Handle events 

Understanding these concepts can help you to implement any number of use cases. For example, 
1. Video trimming - allow the user to trim, merge any number of video sources
2. Cover image selection - allow the user to select a video frame as a preview.
3. Video editing -  allow the user to edit video by adding various number of effects, audio  

Learn how to quickly integrate [Playback API](quickstart_playback.md) into your project.

### Export API
[ExportFlowManager](export/ve-export-sdk/com.banuba.sdk.export.data/-export-flow-manager) amd [ExportParamProvider](export/ve-export-sdk/com.banuba.sdk.export.data/-export-params-provider) 
are core of Export API. With Export API you can easily to make any number of video files with various effects and audio.

Supported Features
1. Any number of video in various resolutions
2. Video with any number of various effects
3. A separate audio file
4. Slideshow - video made of images
5. A GIF preview of a video

Learn how to quickly integrate [Export API](quickstart_export.md) into your project.

## Dependencies
- [Koin](https://insert-koin.io/)
- [ExoPlayer](https://github.com/google/ExoPlayer)
- [Kotlin Coroutines](https://github.com/Kotlin/kotlinx.coroutines)
- [AndroidX](https://developer.android.com/jetpack/androidx) libraries
- [Learn all](all_dependencies.md)

### Setup API
GitHub packages is used for getting Android Video Editor API modules.

Add repositories to your [project gradle](../settings.gradle#L18) file.
```groovy
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Banuba/banuba-ve-sdk")
            credentials {
                username = "Banuba"
                password = ""
            }
        }
    }
```
Next, add a list of API dependencies in [app/build.gradle](app/build.gradle#L47) file.

```groovy
  def banubaSdkVersion = '1.26.5.1'
  implementation "com.banuba.sdk:ffmpeg:5.1.3"
  implementation "com.banuba.sdk:banuba-token-storage-sdk:${banubaSdkVersion}"
  implementation "com.banuba.sdk:core-sdk:${banubaSdkVersion}"
  implementation "com.banuba.sdk:ve-sdk:${banubaSdkVersion}"
  implementation "com.banuba.sdk:ve-playback-sdk:${banubaSdkVersion}"
  implementation "com.banuba.sdk:ve-export-sdk:${banubaSdkVersion}"
  implementation "com.banuba.sdk:ve-effects-sdk:${banubaSdkVersion}"
  
  // Only if you use Banuba Face AR
  implementation 'com.banuba.sdk:effect-player:1.5.3.1'
```

Custom behavior of Video Editor API is implemented by using dependency injection framework [Koin](https://insert-koin.io/).

Next, create new class [VideoEditorApiModule](../app/src/main/java/com/banuba/example/videoeditor/VideoEditorApiModule.kt) 
for implementing Video Editor API and add all required API modules.
```kotlin
   class VideoEditorApiModule {
    fun initialize(application: Application) {
        startKoin {
            androidContext(application)
            allowOverride(true)

            modules(
                VeSdkKoinModule().module,
                VeExportKoinModule().module,
                VePlaybackSdkKoinModule().module,
                TokenStorageKoinModule().module,
                // Module is required for applying Face AR masks
                BanubaEffectPlayerKoinModule().module,
                SampleModule().module // 
            )
        }
    }
}
```

Create new class [SampleModule](../app/src/main/java/com/banuba/example/videoeditor/VideoEditorApiModule.kt#L55) to provide 
custom implementation for API in module variable.

```diff
private class SampleModule {

+    val module = module {
        ...
    }
}
```

Finally, initialize ```VideoEditorApiModule``` in your [Application](../app/src/main/java/com/banuba/example/videoeditor/SampleApp.kt#L23) class.
```BanubaVideoEditor``` is a core class of API and SDK for initializing the product with the license token.
Instance ```videoEditor``` is ```null``` when the license token is incorrect i.e. empty, truncated.
```kotlin
class SampleApp : Application() {
 override fun onCreate() {
        super.onCreate()

        val videoEditor = BanubaVideoEditor.initialize(LICENSE_TOKEN)
        if (videoEditor == null) {
            // Token is not correct. Please check the license token
        } else {
            // Initialize API modules
            VideoEditorApiModule().initialize(this@SampleApp)
        }
    }
}
```

### Check license state
It is highly recommended to check your license state before using API functionalities.  
Use ```BanubaVideoEditor.getLicenseState``` method for checking the license state in your Activity or Fragment.
```kotlin
videoEditor.getLicenseState { isValid ->
   if (isValid) {
      // ✅ License is active, all good
   } else {
      // ❌ Use of Video Editor is restricted. License is revoked or expired.
   }
}
```

## What is next?
We highly recommend to visit [Playback API](quickstart_playback.md) and [Export API](quickstart_export.md) quickstart guides to 
streamline your integration process.