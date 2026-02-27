# Export API Quickstart

- [Overview](#Overview)
- [Prepare export flow](#Prepare-export-flow)
- [Prepare effects](#Prepare-effects)
- [Get export flow manager](#Get-export-flow-manager)
- [Manage export execution](#Manage-export-execution)
- [Handle export result](#Handle-export-result)
- [Create slideshow](#Create-slideshow)

## Overview
This guide helps you quickly integrate the Export API into your project. You'll learn to export media files (video, audio, GIF) with various effects and resolutions.

Export API produces video as `.mp4` files.

> **Important Performance Considerations**
> Export is CPU/GPU intensive. Execution time depends on:
> - Video duration
> - Number of video/audio sources
> - Number and complexity of effects
> - Number of exported files
> - Device hardware capabilities  


### Export Modes

| Mode | Description |
|------|-------------|
| **Foreground** | User waits on progress screen until completion |
| **Background** | User can navigate away; notification sent when done |

Corresponding implementations:
- [`ForegroundExportFlowManager`](export/ve-export-sdk/com.banuba.sdk.export.data/-foreground-export-flow-manager/index.md)
- [`BackgroundExportFlowManager`](export/ve-export-sdk/com.banuba.sdk.export.data/-background-export-flow-manager/index.md)  
 [Detailed export guide](https://github.com/Banuba/ve-sdk-android-integration-sample/blob/main/mddocs/guide_export.md)

---

## Prepare Export Flow
### Implement ExportParamsProvider

Create a class implementing [`ExportParamsProvider`](export/ve-export-sdk/com.banuba.sdk.export.data/-export-params-provider/index.md) to define what media files to export.

**Example:** Export one HD video (720p) with watermark and effects:

```kotlin
private class CustomExportParamsProvider(
  private val exportDir: Uri,
  private val mediaFileNameHelper: MediaFileNameHelper,
  private val watermarkBuilder: WatermarkBuilder
) : ExportParamsProvider {

  override fun provideExportParams(
    effects: Effects,
    videoRangeList: VideoRangeList,
    musicEffects: List<MusicEffect>,
    videoVolume: Float
  ): List<ExportParams> {
    val exportSessionDir = exportDir.toFile().apply {
      // Export dir must be created
      mkdirs()
    }

    // Specify name for your exported video. Do not use ext i.e. .mp4
    val exportVideoFileName = mediaFileNameHelper.generateExportName() + "_watermark"

    val paramsHdWithWatermark =
      ExportParams.Builder(VideoResolution.Exact.HD) // Video Quality resolution
        .effects(effects.withWatermark(watermarkBuilder, WatermarkAlignment.BottomRight(marginRightPx = 16.toPx)))
        .fileName(exportVideoFileName)
        .debugEnabled(true)
        .videoRangeList(videoRangeList)
        .destDir(exportSessionDir)
        .musicEffects(musicEffects)
        .volumeVideo(videoVolume)
        .build()

    return listOf(paramsHdWithWatermark)
  }
}
``` 
set file name,
```diff
ExportParams.Builder(VideoResolution.Exact.HD) // Video Quality resolution
        .effects(effects.withWatermark(watermarkBuilder, WatermarkAlignment.BottomRight(marginRightPx = 16.toPx)))
+        .fileName(exportVideoFileName)
        .debugEnabled(true)
        .videoRangeList(videoRangeList)
        .destDir(exportSessionDir)
        .musicEffects(musicEffects)
        .volumeVideo(videoVolume)
        .build()
``` 

set watermark to bottom right of the video,
```diff
ExportParams.Builder(VideoResolution.Exact.HD) // Video Quality resolution
+        .effects(effects.withWatermark(watermarkBuilder, WatermarkAlignment.BottomRight(marginRightPx = 16.toPx)))
        .fileName(exportVideoFileName)
        .debugEnabled(true)
        .videoRangeList(videoRangeList)
        .destDir(exportSessionDir)
        .musicEffects(musicEffects)
        .volumeVideo(videoVolume)
        .build()
``` 

Set video effects and audio tracks as ```MusicEffect```  to [ExportParams](export/ve-export-sdk/com.banuba.sdk.export.data/-export-params/index.md).
Then when export starts these values are passed to implementation of [ExportParamsProvider](export/ve-export-sdk/com.banuba.sdk.export.data/-export-params-provider/index.md).
```diff
private class CustomExportParamsProvider(
  private val exportDir: Uri,
  private val mediaFileNameHelper: MediaFileNameHelper,
  private val watermarkBuilder: WatermarkBuilder
) : ExportParamsProvider {

  override fun provideExportParams(
+    effects: Effects,
    videoRangeList: VideoRangeList,
+    musicEffects: List<MusicEffect>,
    videoVolume: Float
  ): List<ExportParams> {
   ...
    val paramsHdWithWatermark =
      ExportParams.Builder(VideoResolution.Exact.HD) // Video Quality resolution
+        .effects(effects.withWatermark(watermarkBuilder, WatermarkAlignment.BottomRight(marginRightPx = 16.toPx)))
+        .musicEffects(musicEffects)
        ...
        .build()
...
  }
}
``` 

Other properties
``` ExportParams.Builder``` class requires just single parameter ```resolution```, others are optional:
- `fileName(fileName: String)` - name of exported video file.
- `effects(effects: Effects)` - list of effects to export in video.
- `videoRangeList(videoRangeList: VideoRangeList)` - video sources to export target video
- `musicEffects(musicEffects: List<MusicEffect>)` - music effects for export video i.e. audio tracks you want to apply on top of video
- `destDir(destDir: File)` -  where to store exported video
- `extraAudioFile(extraAudioTrack: Uri)` - where to store extra audio file from video
- `volumeVideo(volume: Float)` - set audio volume in video

### Register in VideoEditorApiModule

Add your provider in [VideoEditorApiModule](../app/src/main/java/com/banuba/example/videoeditor/VideoEditorApiModule.kt#L77)

```kotlin
factory<ExportParamsProvider> {
    CustomExportParamsProvider(
        exportDir = get(named("exportDir")),
        mediaFileNameHelper = get(),
        watermarkBuilder = get()
    )
}
```

### Configure Export Mode

Choose and configure export mode in [VideoEditorApiModule](../app/src/main/java/com/banuba/example/videoeditor/VideoEditorApiModule.kt#L106).
```kotlin
// Foreground export
single<ExportFlowManager>(named("foregroundExportFlowManager")) {
    ForegroundExportFlowManager(
        exportDataProvider = get(),
        sessionParamsProvider = get(),
        exportSessionHelper = get(),
        exportDir = get(named("exportDir")),
        publishManager = get(),
        errorParser = get(),
        mediaFileNameHelper = get(),
        exportBundleProvider = get()
    )
}

// Background export similarly with BackgroundExportFlowManager
```

📌 [Full CustomExportParamsProvider implementation](../app/src/main/java/com/banuba/example/videoeditor/VideoEditorApiModule.kt#L136)

## Prepare effects
Two approaches to prepare effects for export:
1. **From Playback API** - User adds effects during editing; pass same effects to export
2. **Isolated preparation** - Create effects programmatically without UI

See [Manage effects](quickstart_playback.md#Manage-effects) guide for detailed effect creation.


### Get Export Flow Manager

Access [ExportFlowManager](export/ve-export-sdk/com.banuba.sdk.export.data/-export-flow-manager/index.md) instance:

#### Option 1: Koin Injection in Activity/Fragment

``` diff
+ import org.koin.android.ext.android.inject

class SampleActivity : AppCompatActivity() {
+ private val exportFlowManager: ExportFlowManager by inject(named("foregroundExportFlowManager"))
 ...
}
```

#### Option 2: Koin Injection in ViewModel
```diff
 viewModel {
   ExportViewModel(
+       foregroundExportFlowManager = get(named("foregroundExportFlowManager"))
   )
  }
```
:exclamation: Important  
Use Koin's named qualifier when using multiple ExportFlowManager instances.

### Manage export execution

#### Start Export
Convert video URIs to ExportTaskParams:

```kotlin
fun startExport(
  context: Context,
  videosUri: List<Uri>
) {
  exportFlowManager.startExport(createExportParams(context, videosUri))
}

fun createExportParams(
  context: Context,
  videosUri: List<Uri>
): ExportTaskParams {
  val videoRanges = prepareVideoRages(videosUri)

  val totalVideoDuration = videoRanges.data.sumOf { it.durationMs }
  val coverFrameSize = Size(720, 1280) // Cover size resolution
  val effects = ... // Provide list of effects to export
  val musicEffects = ... // Provide list of audio tracks to export

  return ExportTaskParams(
    videoRanges = videoRanges,
    effects = effects,
    musicEffects = musicEffects,
    videoVolume = 1F,
    coverFrameSize = coverFrameSize,
    aspect = AspectRatio(9.0 / 16)
  )  
}

fun prepareVideoRages(
  context: Context,
  videosUri: List<Uri>
): VideoRangeList {
  val videoRecords = videosUri.map { fileUri ->
    val videoDuration = DurationExtractor().extractDurationMilliSec(context, fileUri)
    val videoSpeed = 1f
    VideoRecordRange(
      sourceUri = fileUri,            //mandatory, uri of video file
      durationMs = videoDuration,     //mandatory, duration of video
      speed = videoSpeed,             //mandatory, video playback speed
      playFromMs = 0,                 //optional, by default equals 0
      playToMs = videoDuration,       //optional, by default equals duration of video,
      rotation = Rotation.ROTATION_0,  //optional, by default ROTATION_0
      type = VideoSourceType.GALLERY,  //mandatory, type of video source (gallery, camera, slideshow)
      pipApplied = false
    )
  }
  return VideoRangeList(videoRecords)
}
```

#### Stop Export
```kotlin
// Foreground
foregroundExportFlowManager.stopExport()

// Background  
backgroundExportFlowManager.stopExport()
```

#### Handle Export Result

Observe export results using LiveData<ExportResult>:

```kotlin
exportFlowmanager.resultData.observe(this, exportResultObserver)
```

Implement observer:
```kotlin
val exportResultObserver = Observer<ExportResult> { exportResult ->
  when (exportResult) {
    is ExportResult.Progress -> {
      // Export execution is in progress
    }

    is ExportResult.Success -> {
      // Export execution completed successfully
      // For example, let's take the first exported video. The number of exported video depends on your implementation in ExportParamsProvider
      val exportedVideo = exportResult.videoList.first().sourceUri
      Log.d(TAG, "Export video finished successfully = $exportedVideo")
    }

    is ExportResult.Error -> {
      // Export execution failed
    }

    is ExportResult.Inactive, is ExportResult.Stopped -> {
      // Export execution stopped or inactive
    }
  }
}
```
📌 [Complete example in ExportActivity](../app/src/main/java/com/banuba/example/videoeditor/export/ExportActivity.kt#L35)

## Create slideshow
Generate video from images (with optional animation).. 

:exclamation: Important  
Slideshows don't support audio tracks or video effects.

### Basic Slideshow Creation
```kotlin
val videoFHD = Size(1080, 1920)

val sources = imageUriList.map { uri ->
    SlideShowSource.File(
        durationMs = 3000L, // Each image: 3 seconds
        source = uri
    )
}

val params = SlideShowTask.Params.create(
    context = context,
    size = videoFHD,
    destFile = File(...), // Output location
sources = sources
)

try {
    SlideShowTask.makeVideo(params)
    // Success - no exception thrown
} catch (e: Exception) {
    // Handle error
}
```

📌 [Sample implementation](../app/src/main/java/com/banuba/example/videoeditor/export/ExportViewModel.kt#L83)
