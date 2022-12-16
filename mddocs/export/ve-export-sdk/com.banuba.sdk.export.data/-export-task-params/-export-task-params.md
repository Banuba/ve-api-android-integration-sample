//[ve-export-sdk](../../../index.md)/[com.banuba.sdk.export.data](../index.md)/[ExportTaskParams](index.md)/[ExportTaskParams](-export-task-params.md)

# ExportTaskParams

[androidJvm]\
fun [ExportTaskParams](-export-task-params.md)(videoRanges: VideoRangeList, effects: Effects, musicEffects: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;MusicEffect&gt;, videoVolume: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html), coverUri: [Uri](https://developer.android.com/reference/kotlin/android/net/Uri.html) = Uri.EMPTY, coverFrameSize: [Size](https://developer.android.com/reference/kotlin/android/util/Size.html), aspect: AspectRatio, videoResolution: VideoResolution.Exact? = null, additionalExportData: [Parcelable](https://developer.android.com/reference/kotlin/android/os/Parcelable.html)? = null, doOnStart: () -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)? = null)

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
