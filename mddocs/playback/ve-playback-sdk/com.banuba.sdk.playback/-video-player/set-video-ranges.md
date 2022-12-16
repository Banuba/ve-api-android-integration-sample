//[ve-playback-sdk](../../../index.md)/[com.banuba.sdk.playback](../index.md)/[VideoPlayer](index.md)/[setVideoRanges](set-video-ranges.md)

# setVideoRanges

[androidJvm]\
abstract fun [setVideoRanges](set-video-ranges.md)(videoRanges: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;VideoRecordRange&gt;, seekTotalPositionMs: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = -1)

Sets video playlist containing VideoRecordRange objects into player. Every invocation clears recent playlist and change it to the list provided as parameter.

## Parameters

androidJvm

| | |
|---|---|
| videoRanges | list of video clips |
| seekTotalPositionMs | requested position on the video playback |
