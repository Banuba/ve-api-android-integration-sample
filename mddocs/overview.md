# API Overview

- [Understand core concepts](#Understand-core-concepts)
- [Dependencies](#Dependencies)
- [Setup API](#Setup-API)

## Core concepts
The Video Editor API consists of two primary modules:
- ```Playback API```
- ```Export API```

### Playback API

The [`VideoPlayer`](playback/ve-playback-sdk/com.banuba.sdk.playback/-video-player/index.md) is the core component of the Playback API, implementing a familiar media player pattern.

#### Key Concepts

1. **Playlist Management** - Add video sources for playback
2. **Playback Control** - Play, pause, seek, and volume adjustment
3. **Effects Management** - Apply and control video effects
4. **Event Handling** - Monitor playback state and errors

#### Common Use Cases

| Use Case | Description |
|----------|-------------|
| **Video Trimming** | Trim and merge multiple video sources |
| **Cover Selection** | Extract frames for video thumbnails |
| **Video Editing** | Apply effects, filters, and audio tracks |

👉 See [Playback API Quickstart](quickstart_playback.md) for integration guide.

### Export API

The [`ExportFlowManager`](export/ve-export-sdk/com.banuba.sdk.export.data/-export-flow-manager/index.md) and [`ExportParamsProvider`](export/ve-export-sdk/com.banuba.sdk.export.data/-export-params-provider/index.md) are the core components of the Export API, enabling video rendering with applied effects and audio.

#### Capabilities

- **Multiple Video Sources** - Combine any number of video clips at various resolutions
- **Rich Effects** - Export videos with all applied filters, overlays, and adjustments
- **Audio Integration** - Add separate audio tracks to the final output
- **Slideshow Creation** - Generate videos from a sequence of images
- **Preview Generation** - Create GIF previews of exported videos

👉 See [Export API Quickstart](quickstart_export.md) for integration guide.

## Installation
Add the Banuba repository to your project using **either** Groovy **or** Kotlin DSL:

**Groovy** (in project's build.gradle)

```groovy
...

allprojects {
    repositories {
       ...
       maven {
          name = "nexus"
          url = uri("https://nexus.banuba.net/repository/maven-releases")
       }
    }
}
```
or

**Kotlin** (settings.gradle.kts)
```kotlin
...
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        ...
        maven {
            name = "nexus"
            url = uri("https://nexus.banuba.net/repository/maven-releases")
        }
    }
}
```

Next, add a list of API dependencies in [app/build.gradle](app/build.gradle#L47) file.

```groovy
  def banubaSdkVersion = '1.49.5'
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

## Dependencies
- [Koin](https://insert-koin.io/)
- [ExoPlayer](https://github.com/google/ExoPlayer)
- [Kotlin Coroutines](https://github.com/Kotlin/kotlinx.coroutines)
- [AndroidX](https://developer.android.com/jetpack/androidx) libraries
- [See all](all_dependencies.md)

## What is next?
We highly recommend to learn [Playback API quickstart](quickstart_playback.md) and [Export API quickstart](quickstart_export.md) guides to 
streamline your integration process.