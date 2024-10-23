# Quickstart Export API

- [Overview](#Overview)
- [Prepare export flow](#Prepare-export-flow)
- [Prepare effects](#Prepare-effects)
- [Get export flow manager](#Get-export-flow-manager)
- [Manage export execution](#Manage-export-execution)
- [Handle export result](#Handle-export-result)
- [Create slideshow](#Create-slideshow)
- [FAQ](#FAQ)

## Overview
This guide is aimed to help you quickly to integrate Export API into your project.
You will learn how to export a number of media files i.e. video, audio, gif with various effects and in various resolutions.  

Export API produces video as ```.mp4``` file.

:exclamation: Important  
Export is a very heavy CPU and GPU intensive computational task.
Execution time depends on
1. Video duration - the longer video the longer execution time.
2. Number of video and audio sources - the more sources the longer execution time.
3. Number of effects and their usage in video - the more effects and their usage the longer execution time.
4. Number of exported video - the more video and audio you want to export the longer execution time it takes.
5. Device hardware - the most powerful devices can execute export much quicker.

Export supports 2 modes:
- ```Foreground``` - the user has to wait on progress screen until processing is done.
- ```Background``` - the user can be taken to your screens. A notification will be sent when processing is done.

[ForegroundExportFlowManager](export/ve-export-sdk/com.banuba.sdk.export.data/-foreground-export-flow-manager/index.md) and
[BackgroundExportFlowManager](export/ve-export-sdk/com.banuba.sdk.export.data/-background-export-flow-manager/index.md) are correspondent implementations.

Visit [export guide](https://github.com/Banuba/ve-sdk-android-integration-sample/blob/main/mddocs/guide_export.md) to learn more 
about export in Video Editor SDK.

### Prepare export flow
You can use Export API to export a number of media files to meet all your requirements.   

Implement [ExportParamsProvider](export/ve-export-sdk/com.banuba.sdk.export.data/-export-params-provider/index.md) 
and provide ```List<ExportParams>``` where every [ExportParams](export/ve-export-sdk/com.banuba.sdk.export.data/-export-params/index.md) is a media file i.e. video or audio 
that will be produced in export.  

First, create new class ```CustomExportParamsProvider``` and implement [ExportParamsProvider](export/ve-export-sdk/com.banuba.sdk.export.data/-export-params-provider/index.md).
Method ```provideExportParams``` returns ```List<ExportParams>``` which is a list of media content you want to export.

In this sample, an implementation that exports 1 video HD(720p) quality with watermark and some effects and 
audio tracks that might not be empty.

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
where you set name to exported file,
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

Next, specify this implementation in [VideoEditorApiModule](../app/src/main/java/com/banuba/example/videoeditor/VideoEditorApiModule.kt#L77)

```kotlin
factory<ExportParamsProvider> {
    CustomExportParamsProvider(
        exportDir = get(named("exportDir")),
        mediaFileNameHelper = get(),
        watermarkBuilder = get()
    )
}
```

Finally, use the most suitable export mode for your application - ```ForegroundExportFlowManager``` or ```BackgroundExportFlowManager``` in [VideoEditorApiModule](../app/src/main/java/com/banuba/example/videoeditor/VideoEditorApiModule.kt#L106).
```kotlin
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
```

Full implementation of [CustomExportParamsProvider](../app/src/main/java/com/banuba/example/videoeditor/VideoEditorApiModule.kt#L136) is available in the sample.

## Prepare effects
You have at least 2 options how to prepare video effects and audio tracks for export:
1. The user adds video effects and audio tracks on the screens implemented using Playback API and next you pass these effects to export.
2. You prepare video effects and audio tracks in isolation and pass it to export.

Please visit [Manage effects](quickstart_playback.md#Manage-effects) guide where we fully explained how to create effects for playback. 
The same approach works for export as well.

### Get export flow manager
Instance of [ExportFlowManager](export/ve-export-sdk/com.banuba.sdk.export.data/-export-flow-manager/index.md) is created in [VideoEditorApiModule](../app/src/main/java/com/banuba/example/videoeditor/VideoEditorApiModule.kt#L59).
You can access to this instance in 2 ways
1. Using [Koin](https://insert-koin.io/) inject in Android Fragment or Activity classes.
``` diff
+ import org.koin.android.ext.android.inject

class SampleActivity : AppCompatActivity() {
+ private val exportFlowManager: ExportFlowManager by inject(named("foregroundExportFlowManager"))
 ...
}
```
2. In Android [ViewModel](../app/src/main/java/com/banuba/example/VideoEditorApiModule.kt#L59) using Koin .
```diff
 viewModel {
   ExportViewModel(
+       foregroundExportFlowManager = get(named("foregroundExportFlowManager"))
   )
  }
```
:exclamation: Important  
Please keep im mind that if you use 2 instances of [ExportFlowManager](export/ve-export-sdk/com.banuba.sdk.export.data/-export-flow-manager/index.md) 
you should differ them by using Koin ```named``` function. For example, ```named("foregroundExportFlowManager")```.

### Manage export execution
[ForegroundExportFlowManager.startExport](export/ve-export-sdk/com.banuba.sdk.export.data/-foreground-export-flow-manager/start-export.md) and
[BackgroundExportFlowManager.startExport](export/ve-export-sdk/com.banuba.sdk.export.data/-background-export-flow-manager/start-export.md) are methods
you can use to start export execution. Each method requires instance of [ExportTaskParams](export/ve-export-sdk/com.banuba.sdk.export.data/-export-task-params)
to start execution that describes a number of properties for produced media file.

:bulb: Hint    
You have ```List<Uri>``` video that stored on the device. Next step is to convert ```List<Uri>``` to
```VideoRangeList``` where every ```Uri``` is converted to ```VideoRecordRange```.
Instance of ```VideoRecordRange``` describes video source and its capabilities for the export i.e. speed, start and end positions of video to export etc.
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
```DurationExtractor``` is used to get a duration of video file.  

You can use [ForegroundExportFlowManager.stopExport](export/ve-export-sdk/com.banuba.sdk.export.data/-foreground-export-flow-manager/stop-export.md) and
[BackgroundExportFlowManager.stopExport](export/ve-export-sdk/com.banuba.sdk.export.data/-background-export-flow-manager/stop-export.md) methods to stop export execution.

## Handle export result
[ExportResult](export/ve-export-sdk/com.banuba.sdk.export.data/-export-result/index.md) is main class that describes export result.
[ExportFlowManager.resultData](export/ve-export-sdk/com.banuba.sdk.export.data/-export-flow-manager/result-data.md) is a handy wrapper 
as ```LiveData<ExportResult>``` that you can use to observe export result in your Android lifecycle components.  

```kotlin
exportFlowmanager.resultData.observe(this, exportResultObserver)
```

where ```exportResultObserver```
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

Full sample if available in [ExportActivity](../app/src/main/java/com/banuba/example/videoeditor/export/ExportActivity.kt#L35)

## Create slideshow
Slideshow is a video made by a number of images with an animation. Animation can be disabled.
To create a slideshow you need a list of images stored on the device.  

:exclamation: Important  
Slideshow does not support adding audio tracks or other video effects.

```SlideShowTask``` is a main class that is responsible for making slideshow video. ```SlideShowTask.makeVideo``` is
the method to make and requires ```SlideShowTask.Params```. You can assign specific duration in milliseconds of image in slideshow video 
by using ```SlideShowSource.File.durationMs``` property. Total duration of slideshow
video is sum of ```SlideShowSource.File.durationMs```.

In this sample, slideshow video with resolution FHD(1080p) is made by a list of images. Total duration will be a number of images multiply 3 seconds.
```kotlin
val videoFHD = Size(1080, 1920)

val sources = imageUriList.map { uri ->
    SlideShowSource.File(
        durationMs = 3_000L, // each image takes 3 seconds in video
        source = uri
    )
}

val params = SlideShowTask.Params.create(
    context = context,
    size = videoFHD,
    destFile = ..., // File where to store video 
    sources = sources
)
SlideShowTask.makeVideo(params)
```

```SlideShowTask.makeVideo``` method does not return any result. The execution completes successfully if no ```Exception``` thrown. 

Our [sample](../app/src/main/java/com/banuba/example/videoeditor/export/ExportViewModel.kt#L83) includes implementation of making slideshow video based on image from Android [assets](../app/src/main/assets) folder 
for simplicity. You can use any image stored on the device.

## FAQ

#### 1. I want to create video from image with audio track and effects
Slideshow does not support adding audio and effects on top. But you can use the following workaround
   1. Create a slideshow video
   2. Use created slideshow video as source and export new video with audio tracks and video effects.