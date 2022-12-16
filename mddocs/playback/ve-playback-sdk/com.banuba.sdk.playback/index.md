//[ve-playback-sdk](../../index.md)/[com.banuba.sdk.playback](index.md)

# Package com.banuba.sdk.playback

## Types

| Name | Summary |
|---|---|
| [PlaybackError](-playback-error/index.md) | [androidJvm]<br>sealed class [PlaybackError](-playback-error/index.md)<br>Available error types that can be received inside [VideoPlayer.Callback.onVideoPlaybackError](-video-player/-callback/on-video-playback-error.md) callback. |
| [PlayerScaleType](-player-scale-type/index.md) | [androidJvm]<br>enum [PlayerScaleType](-player-scale-type/index.md) : [Enum](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-enum/index.html)&lt;[PlayerScaleType](-player-scale-type/index.md)&gt; <br>Video scaling types. |
| [PlayerState](-player-state/index.md) | [androidJvm]<br>enum [PlayerState](-player-state/index.md) : [Enum](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-enum/index.html)&lt;[PlayerState](-player-state/index.md)&gt; <br>[IDLE](-player-state/-i-d-l-e/index.md) The player is idle; [LOADING](-player-state/-l-o-a-d-i-n-g/index.md) The player is not able to immediately play the media, but is doing work toward being able to do so. This state typically occurs when the player needs to buffer more data before playback can start; [PLAYING](-player-state/-p-l-a-y-i-n-g/index.md) The player is playing; [FINISHED](-player-state/-f-i-n-i-s-h-e-d/index.md) The player is finished; |
| [PlayerType](-player-type/index.md) | [androidJvm]<br>enum [PlayerType](-player-type/index.md) : [Enum](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-enum/index.html)&lt;[PlayerType](-player-type/index.md)&gt; |
| [VideoPlayer](-video-player/index.md) | [androidJvm]<br>interface [VideoPlayer](-video-player/index.md)<br>An interface defining the core functionality of the Banuba Playback API. |
| [VideoPlayerFactory](-video-player-factory/index.md) | [androidJvm]<br>interface [VideoPlayerFactory](-video-player-factory/index.md) |
| [VideoPlayerProvider](-video-player-provider/index.md) | [androidJvm]<br>class [VideoPlayerProvider](-video-player-provider/index.md)(playerFactory: [VideoPlayerFactory](-video-player-factory/index.md)) |
