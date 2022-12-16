//[ve-playback-sdk](../../../../index.md)/[com.banuba.sdk.playback](../../index.md)/[PlaybackError](../index.md)/[Video](index.md)

# Video

[androidJvm]\
sealed class [Video](index.md) : [PlaybackError](../index.md)

Error type related to video playback. Available Video errors have a [codeName](code-name.md) to figure out the cause of error and optional message with detailed description.

## Types

| Name | Summary |
|---|---|
| [Other](-other/index.md) | [androidJvm]<br>data class [Other](-other/index.md)(codeName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), msg: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?) : [PlaybackError.Video](index.md) |
| [Remote](-remote/index.md) | [androidJvm]<br>data class [Remote](-remote/index.md)(codeName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), msg: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?) : [PlaybackError.Video](index.md) |
| [Renderer](-renderer/index.md) | [androidJvm]<br>data class [Renderer](-renderer/index.md)(codeName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), msg: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?) : [PlaybackError.Video](index.md) |
| [Source](-source/index.md) | [androidJvm]<br>data class [Source](-source/index.md)(codeName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), msg: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?) : [PlaybackError.Video](index.md) |
| [Unexpected](-unexpected/index.md) | [androidJvm]<br>data class [Unexpected](-unexpected/index.md)(codeName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), msg: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?) : [PlaybackError.Video](index.md) |

## Properties

| Name | Summary |
|---|---|
| [codeName](code-name.md) | [androidJvm]<br>abstract val [codeName](code-name.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? |

## Inheritors

| Name |
|---|
| [Source](-source/index.md) |
| [Remote](-remote/index.md) |
| [Renderer](-renderer/index.md) |
| [Unexpected](-unexpected/index.md) |
| [Other](-other/index.md) |
