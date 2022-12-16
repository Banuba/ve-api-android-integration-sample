//[ve-playback-sdk](../../../index.md)/[com.banuba.sdk.playback](../index.md)/[VideoPlayer](index.md)

# VideoPlayer

[androidJvm]\
interface [VideoPlayer](index.md)

An interface defining the core functionality of the Banuba Playback API.

All interactions with the player should be performed after the function [prepare](prepare.md) returns true.

## Types

| Name | Summary |
|---|---|
| [Callback](-callback/index.md) | [androidJvm]<br>interface [Callback](-callback/index.md)<br>Callback which is used to listen events from the VideoPlayer. All functions are invoked on the background thread. |
| [MaskEffectsListener](-mask-effects-listener/index.md) | [androidJvm]<br>interface [MaskEffectsListener](-mask-effects-listener/index.md)<br>Listener which can be used when mask effect is initially activated. Functions are invoked on the background thread. |

## Functions

| Name | Summary |
|---|---|
| [clearSurfaceHolder](clear-surface-holder.md) | [androidJvm]<br>abstract fun [clearSurfaceHolder](clear-surface-holder.md)(surfaceHolder: [SurfaceHolder](https://developer.android.com/reference/kotlin/android/view/SurfaceHolder.html))<br>Clears internal reference on the SurfaceHolder to prevent any memory leaks. |
| [enableObjectEffects](enable-object-effects.md) | [androidJvm]<br>abstract fun [enableObjectEffects](enable-object-effects.md)(enable: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html))<br>Switch applying of object effects (Stickers and Text). It allows to disable/enable object effects without changing the internal effects list. |
| [enableSpeedEffects](enable-speed-effects.md) | [androidJvm]<br>abstract fun [enableSpeedEffects](enable-speed-effects.md)(enable: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html))<br>Switch applying of speed effects. It allows to disable/enable speed effects without changing the internal effects list. |
| [isPlaying](is-playing.md) | [androidJvm]<br>abstract fun [isPlaying](is-playing.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Use this function to check if the video playback is active. |
| [isPrepared](is-prepared.md) | [androidJvm]<br>abstract fun [isPrepared](is-prepared.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Shows if the BanubaVideoPlayer is ready for further interactions. It may be used as a helper function in case of some asynchronous work during player preparation. |
| [pause](pause.md) | [androidJvm]<br>abstract fun [pause](pause.md)()<br>Pauses the video playback |
| [play](play.md) | [androidJvm]<br>abstract fun [play](play.md)(isRepeat: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html))<br>Starts the video playback |
| [prepare](prepare.md) | [androidJvm]<br>abstract fun [prepare](prepare.md)(size: [Size](https://developer.android.com/reference/kotlin/android/util/Size.html)): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>The starting point into BanubaVideoPlayer. Under the hood it creates a special thread and handler and performs all required allocations for further video and effects playback. |
| [release](release.md) | [androidJvm]<br>abstract fun [release](release.md)(wait: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false)<br>Releases underlying resources. Closes the VideoPlayerThread and clears references. |
| [seekTo](seek-to.md) | [androidJvm]<br>abstract fun [seekTo](seek-to.md)(positionMs: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html))<br>Seeks to a position specified in milliseconds in the video playback. |
| [setCallback](set-callback.md) | [androidJvm]<br>abstract fun [setCallback](set-callback.md)(callback: [VideoPlayer.Callback](-callback/index.md)?)<br>Adds a callback to listen events from the BanubaVideoPlayer. Pass null as an argument to clear reference to recent callback instance. |
| [setEffects](set-effects.md) | [androidJvm]<br>abstract fun [setEffects](set-effects.md)(effects: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;TypedTimedEffect&lt;*&gt;&gt;)<br>Sets visual and speed effects as a list of TypedTimedEffect objects. Every invocation clears the recent effects list and change it to the list provided as a parameter. |
| [setMaskEffectsListener](set-mask-effects-listener.md) | [androidJvm]<br>abstract fun [setMaskEffectsListener](set-mask-effects-listener.md)(listener: [VideoPlayer.MaskEffectsListener](-mask-effects-listener/index.md)?)<br>Adds a mask effects listener. |
| [setMusicEffects](set-music-effects.md) | [androidJvm]<br>abstract fun [setMusicEffects](set-music-effects.md)(effects: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;MusicEffect&gt;)<br>Sets music effects containing MusicEffect objects into player. Every invocation clears the recent music effects list and change it to the list provided as a parameter. To clear existing music effects just pass an empty list. |
| [setScaleType](set-scale-type.md) | [androidJvm]<br>abstract fun [setScaleType](set-scale-type.md)(scaleType: [PlayerScaleType](../-player-scale-type/index.md))<br>Changes video scaling inside displaying viewport. |
| [setSurfaceHolder](set-surface-holder.md) | [androidJvm]<br>abstract fun [setSurfaceHolder](set-surface-holder.md)(surfaceHolder: [SurfaceHolder](https://developer.android.com/reference/kotlin/android/view/SurfaceHolder.html))<br>Sets the SurfaceHolder object into player to receive updates on the SurfaceView inside the player. Should be cleared by [clearSurfaceHolder](clear-surface-holder.md) function when it is no longer needed. |
| [setVideoRanges](set-video-ranges.md) | [androidJvm]<br>abstract fun [setVideoRanges](set-video-ranges.md)(videoRanges: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;VideoRecordRange&gt;, seekTotalPositionMs: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = -1)<br>Sets video playlist containing VideoRecordRange objects into player. Every invocation clears recent playlist and change it to the list provided as parameter. |
| [setVideoSize](set-video-size.md) | [androidJvm]<br>abstract fun [setVideoSize](set-video-size.md)(size: [Size](https://developer.android.com/reference/kotlin/android/util/Size.html))<br>Sets the video size |
| [setVolume](set-volume.md) | [androidJvm]<br>abstract fun [setVolume](set-volume.md)(@[FloatRange](https://developer.android.com/reference/kotlin/androidx/annotation/FloatRange.html)(from = 0.0, to = 1.0)volume: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html))<br>Sets the volume to video playback. It does not affect to music effects volume. Valid values are between 0 (silence) and 1 (unity gain), inclusive. |
| [takeScreenshot](take-screenshot.md) | [androidJvm]<br>abstract fun [takeScreenshot](take-screenshot.md)()<br>Makes a request to take a screenshot. After a while resulting bitmap will be passed into [VideoPlayer.Callback.onScreenshotTaken](-callback/on-screenshot-taken.md) callback. |
