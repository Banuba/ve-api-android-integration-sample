//[ve-playback-sdk](../../../../index.md)/[com.banuba.sdk.playback](../../index.md)/[VideoPlayer](../index.md)/[Callback](index.md)

# Callback

[androidJvm]\
interface [Callback](index.md)

Callback which is used to listen events from the VideoPlayer. All functions are invoked on the background thread.

## Functions

| Name | Summary |
|---|---|
| [onPlayStateChanged](on-play-state-changed.md) | [androidJvm]<br>@[WorkerThread](https://developer.android.com/reference/kotlin/androidx/annotation/WorkerThread.html)<br>open fun [onPlayStateChanged](on-play-state-changed.md)(state: [PlayerState](../../-player-state/index.md))<br>This is called on playback state updates. It is invoked on the background thread. |
| [onScreenshotTaken](on-screenshot-taken.md) | [androidJvm]<br>@[WorkerThread](https://developer.android.com/reference/kotlin/androidx/annotation/WorkerThread.html)<br>open fun [onScreenshotTaken](on-screenshot-taken.md)(bmp: [Bitmap](https://developer.android.com/reference/kotlin/android/graphics/Bitmap.html))<br>Function to receive the result after of [VideoPlayer.takeScreenshot](../take-screenshot.md) invocation. It is invoked on the background thread. |
| [onVideoPlaybackError](on-video-playback-error.md) | [androidJvm]<br>@[WorkerThread](https://developer.android.com/reference/kotlin/androidx/annotation/WorkerThread.html)<br>open fun [onVideoPlaybackError](on-video-playback-error.md)(error: [PlaybackError](../../-playback-error/index.md))<br>This is called in case of error. It is invoked on the background thread. |
| [onVideoPositionChanged](on-video-position-changed.md) | [androidJvm]<br>@[WorkerThread](https://developer.android.com/reference/kotlin/androidx/annotation/WorkerThread.html)<br>open fun [onVideoPositionChanged](on-video-position-changed.md)(positionMs: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html))<br>This is called on playback position updates. It is invoked on the background thread. |
| [onViewportChanged](on-viewport-changed.md) | [androidJvm]<br>@[WorkerThread](https://developer.android.com/reference/kotlin/androidx/annotation/WorkerThread.html)<br>open fun [onViewportChanged](on-viewport-changed.md)(viewport: GlViewport)<br>This is called on every changes of viewport displaying the video. It is invoked on the background thread. |
