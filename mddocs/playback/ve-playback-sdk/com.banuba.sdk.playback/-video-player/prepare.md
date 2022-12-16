//[ve-playback-sdk](../../../index.md)/[com.banuba.sdk.playback](../index.md)/[VideoPlayer](index.md)/[prepare](prepare.md)

# prepare

[androidJvm]\
abstract fun [prepare](prepare.md)(size: [Size](https://developer.android.com/reference/kotlin/android/util/Size.html)): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)

The starting point into BanubaVideoPlayer. Under the hood it creates a special thread and handler and performs all required allocations for further video and effects playback.

#### Return

true if all required allocations correctly prepared. Means that the thread with its handler are ready to receive message requests.

## Parameters

androidJvm

| | |
|---|---|
| size | preferred size of the video. This size is used to define the aspect ratio, keep in mind that the real displaying size is limited by SurfaceView size. |
