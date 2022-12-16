//[ve-export-sdk](../../../index.md)/[com.banuba.sdk.export.data](../index.md)/[ExportTaskParams](index.md)

# ExportTaskParams

[androidJvm]\
data class [ExportTaskParams](index.md)(videoRanges: VideoRangeList, effects: Effects, musicEffects: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;MusicEffect&gt;, videoVolume: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html), coverUri: [Uri](https://developer.android.com/reference/kotlin/android/net/Uri.html), coverFrameSize: [Size](https://developer.android.com/reference/kotlin/android/util/Size.html), aspect: AspectRatio, videoResolution: VideoResolution.Exact?, additionalExportData: [Parcelable](https://developer.android.com/reference/kotlin/android/os/Parcelable.html)?, doOnStart: () -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)?)

Data class which contains video, effects, music and cover params utilizing on export. It is passed into [ExportFlowManager.startExport](../-export-flow-manager/start-export.md) function.

## Parameters

androidJvm

| | |
|---|---|
| videoRanges | VideoRangeList object containing video clips |
| effects | Effects visual and time effects to apply to the exported video |
| musicEffects | list of music effects applied to the exported video |
| videoVolume | volume of exported video specified in float number from 0 to 1 |
| coverUri | Uri of the cover image file |
| coverFrameSize | size of the cover image |
| aspect | aspect ratio applied to the exported video |
| videoResolution | optional VideoResolution value applied to the exported video. By default the optimal size will be set automatically (it is calculated with taking into account capabilities of the device) |
| additionalExportData | any Parcelable object that may be received in [ExportResult.Success.additionalExportData](../-export-result/-success/additional-export-data.md) parameter |
| doOnStart | lambda the will be invoked in the very beginning of the export process |

## Constructors

| | |
|---|---|
| [ExportTaskParams](-export-task-params.md) | [androidJvm]<br>fun [ExportTaskParams](-export-task-params.md)(videoRanges: VideoRangeList, effects: Effects, musicEffects: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;MusicEffect&gt;, videoVolume: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html), coverUri: [Uri](https://developer.android.com/reference/kotlin/android/net/Uri.html) = Uri.EMPTY, coverFrameSize: [Size](https://developer.android.com/reference/kotlin/android/util/Size.html), aspect: AspectRatio, videoResolution: VideoResolution.Exact? = null, additionalExportData: [Parcelable](https://developer.android.com/reference/kotlin/android/os/Parcelable.html)? = null, doOnStart: () -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)? = null) |

## Properties

| Name | Summary |
|---|---|
| [additionalExportData](additional-export-data.md) | [androidJvm]<br>var [additionalExportData](additional-export-data.md): [Parcelable](https://developer.android.com/reference/kotlin/android/os/Parcelable.html)? = null |
| [aspect](aspect.md) | [androidJvm]<br>val [aspect](aspect.md): AspectRatio |
| [coverFrameSize](cover-frame-size.md) | [androidJvm]<br>val [coverFrameSize](cover-frame-size.md): [Size](https://developer.android.com/reference/kotlin/android/util/Size.html) |
| [coverUri](cover-uri.md) | [androidJvm]<br>var [coverUri](cover-uri.md): [Uri](https://developer.android.com/reference/kotlin/android/net/Uri.html) |
| [doOnStart](do-on-start.md) | [androidJvm]<br>var [doOnStart](do-on-start.md): () -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)? = null |
| [effects](effects.md) | [androidJvm]<br>val [effects](effects.md): Effects |
| [musicEffects](music-effects.md) | [androidJvm]<br>val [musicEffects](music-effects.md): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;MusicEffect&gt; |
| [videoRanges](video-ranges.md) | [androidJvm]<br>val [videoRanges](video-ranges.md): VideoRangeList |
| [videoResolution](video-resolution.md) | [androidJvm]<br>val [videoResolution](video-resolution.md): VideoResolution.Exact? = null |
| [videoVolume](video-volume.md) | [androidJvm]<br>val [videoVolume](video-volume.md): [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html) |
