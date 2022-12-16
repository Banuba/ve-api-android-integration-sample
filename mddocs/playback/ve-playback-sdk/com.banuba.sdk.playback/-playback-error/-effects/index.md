//[ve-playback-sdk](../../../../index.md)/[com.banuba.sdk.playback](../../index.md)/[PlaybackError](../index.md)/[Effects](index.md)

# Effects

[androidJvm]\
data class [Effects](index.md)(effectId: [UUID](https://developer.android.com/reference/kotlin/java/util/UUID.html), msg: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?) : [PlaybackError](../index.md)

Error type related to effects processing

## Parameters

androidJvm

| | |
|---|---|
| effectId | id of the effect that created an error |
| msg | optional message with error description |

## Constructors

| | |
|---|---|
| [Effects](-effects.md) | [androidJvm]<br>fun [Effects](-effects.md)(effectId: [UUID](https://developer.android.com/reference/kotlin/java/util/UUID.html), msg: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?) |

## Properties

| Name | Summary |
|---|---|
| [effectId](effect-id.md) | [androidJvm]<br>val [effectId](effect-id.md): [UUID](https://developer.android.com/reference/kotlin/java/util/UUID.html) |
| [msg](msg.md) | [androidJvm]<br>val [msg](msg.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? |
