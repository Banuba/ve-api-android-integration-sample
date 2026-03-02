# Overview

- [Core Concepts](#Core-concepts)
- [Installation](#Installation)
- [Launch](#Launch)

## Core Concepts
The Video Editor API consists of two primary modules:
- ```Playback API```
- ```Export API```

### Playback API

The [`VideoPlayer`](playback/ve-playback-sdk/com.banuba.sdk.playback/-video-player/index.md) is the core component of the Playback API, implementing a familiar media player pattern.

#### Key Concepts

1. **Playlist Management** - Add video sources for playback
2. **Playback Control** - Play, pause, seek, and volume adjustment
3. **Effects Management** - Apply and control video effects

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

Add ```packagingOptions``` to your app's [gradle](../app/build.gradle#L35-L42)
```groovy
android {
...
   packagingOptions {
       pickFirst '**/*.so'

      jniLibs {
         useLegacyPackaging = true
      }
   }
...
}
```

Add dependencies to your app's [gradle](../app/build.gradle#L55)

```groovy
  def banubaSdkVersion = '1.50.0'
  implementation "com.banuba.sdk:ffmpeg:5.3.0"
  implementation "com.banuba.sdk:core-sdk:${banubaSdkVersion}"
  implementation "com.banuba.sdk:ve-sdk:${banubaSdkVersion}"
  implementation "com.banuba.sdk:ve-playback-sdk:${banubaSdkVersion}"
  implementation "com.banuba.sdk:ve-export-sdk:${banubaSdkVersion}"
  implementation "com.banuba.sdk:ve-effects-sdk:${banubaSdkVersion}"

  // Only if you use Banuba Face AR
  implementation "com.banuba.sdk:effect-player-adapter:${banubaSdkVersion}"

```

## Koin Module Setup
1. Create [VideoEditorModule](../app/src/main/java/com/banuba/example/videoeditor/VideoEditorApiModule.kt) to initialize and customize the Video Editor SDK.
2. Inside it, add [SampleModule](../app/src/main/java/com/banuba/example/videoeditor/VideoEditorApiModule.kt#L53) with your customizations:

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
                // Module is required for applying Face AR masks
                BanubaEffectPlayerKoinModule().module,
                SampleModule().module
            )
        }
    }
}

private class SampleModule {

    val module = module {
        ...
    }
}
```

### Launch

Initialize `VideoEditorApiModule` in your [Application](../app/src/main/java/com/banuba/example/videoeditor/SampleApp.kt#L23) class. 
The `BanubaVideoEditor` is the core class responsible for initializing the product with your license token.

```kotlin
class SampleApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val videoEditor = BanubaVideoEditor.initialize(LICENSE_TOKEN)
        if (videoEditor == null) {
            // Token is invalid. Verify your license token
        } else {
            // Initialize API modules
            VideoEditorApiModule().initialize(this@SampleApp)
        }
    }
}
```

:exclamation: Important
1. Returns ```null```l if the license token is invalid – verify your token
2. [Check license activation](../app/src/main/java/com/banuba/example/videoeditor/SampleActivity.kt#L48) before starting the editor.

## Dependencies
- [Koin](https://insert-koin.io/)
- [ExoPlayer](https://github.com/google/ExoPlayer)
- [Kotlin Coroutines](https://github.com/Kotlin/kotlinx.coroutines)
- [AndroidX](https://developer.android.com/jetpack/androidx) libraries

## Next Steps

Get started with our comprehensive integration guides:

| Guide | Description |
|-------|-------------|
| [Playback API Quickstart](quickstart_playback.md) | Learn to implement video playback, trimming, and effects preview |
| [Export API Quickstart](quickstart_export.md) | Master video rendering and output generation |

These guides provide step-by-step instructions to accelerate your integration.