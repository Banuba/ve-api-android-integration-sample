//[ve-playback-sdk](../../../../index.md)/[com.banuba.sdk.playback](../../index.md)/[VideoPlayer](../index.md)/[MaskEffectsListener](index.md)

# MaskEffectsListener

[androidJvm]\
interface [MaskEffectsListener](index.md)

Listener which can be used when mask effect is initially activated. Functions are invoked on the background thread.

## Functions

| Name | Summary |
|---|---|
| [onEffectInitialActivationDone](on-effect-initial-activation-done.md) | [androidJvm]<br>@[WorkerThread](https://developer.android.com/reference/kotlin/androidx/annotation/WorkerThread.html)<br>open fun [onEffectInitialActivationDone](on-effect-initial-activation-done.md)(path: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html))<br>This is called when effect initial activation ends. It is invoked on the background thread.s |
| [onEffectInitialActivationStart](on-effect-initial-activation-start.md) | [androidJvm]<br>@[WorkerThread](https://developer.android.com/reference/kotlin/androidx/annotation/WorkerThread.html)<br>open fun [onEffectInitialActivationStart](on-effect-initial-activation-start.md)(path: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html))<br>This is called when effect initial activation starts. It is invoked on the background thread. |
