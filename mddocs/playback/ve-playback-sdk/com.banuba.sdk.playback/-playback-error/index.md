//[ve-playback-sdk](../../../index.md)/[com.banuba.sdk.playback](../index.md)/[PlaybackError](index.md)

# PlaybackError

[androidJvm]\
sealed class [PlaybackError](index.md)

Available error types that can be received inside [VideoPlayer.Callback.onVideoPlaybackError](../-video-player/-callback/on-video-playback-error.md) callback.

## Types

| Name | Summary |
|---|---|
| [Effects](-effects/index.md) | [androidJvm]<br>data class [Effects](-effects/index.md)(effectId: [UUID](https://developer.android.com/reference/kotlin/java/util/UUID.html), msg: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?) : [PlaybackError](index.md)<br>Error type related to effects processing |
| [Init](-init/index.md) | [androidJvm]<br>object [Init](-init/index.md) : [PlaybackError](index.md)<br>This error type is not thrown through callback, but can be used to pass into UI if [VideoPlayer.prepare](../-video-player/prepare.md) returns false. |
| [Video](-video/index.md) | [androidJvm]<br>sealed class [Video](-video/index.md) : [PlaybackError](index.md)<br>Error type related to video playback. Available Video errors have a [codeName](-video/code-name.md) to figure out the cause of error and optional message with detailed description. |

## Inheritors

| Name |
|---|
| [Video](-video/index.md) |
| [Effects](-effects/index.md) |
| [Init](-init/index.md) |
