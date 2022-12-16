## Video Editor Playback API Integration on Android

- [Dependencies](#dependencies)
- [Getting Started](#Getting-Started)
    + [Add dependencies](#Add-dependencies)
    + [Configure DI](#Configure-DI)
    + [Prepare VideoEditorPlayer](#Prepare-VideoEditorPlayer)
    + [Playback functions](#Playback-functions)
    + [Playback with effects](#Playback-with-effects)
    + [How to prepare effects for playback](#How-to-prepare-effects-for-playback)
    + [VideoEditorPlayer.Callback](#VideoEditorPlayer.Callback)
    + [Releasing the player](#Releasing-the-player)

## Dependencies
- [Koin](https://insert-koin.io/)
- [ExoPlayer](https://github.com/google/ExoPlayer)
- [Kotlin Coroutines](https://github.com/Kotlin/kotlinx.coroutines)
- [AndroidX](https://developer.android.com/jetpack/androidx) libraries

[Please see all used dependencies](all_dependencies.md)

## Getting Started
### Add dependencies
Please, specify a list of dependencies as in [app/build.gradle](app/build.gradle) file to integrate playback functionality of Playback API.

``` groovy
    def banubaSdkVersion = '1.25.0'
    implementation "com.banuba.sdk:ffmpeg:4.4"
    implementation "com.banuba.sdk:banuba-token-storage-sdk:${banubaSdkVersion}"
    implementation "com.banuba.sdk:core-sdk:${banubaSdkVersion}"
    implementation "com.banuba.sdk:ve-sdk:${banubaSdkVersion}"
    implementation "com.banuba.sdk:ve-playback-sdk:${banubaSdkVersion}"
    implementation "com.banuba.sdk:ve-effects-sdk:${banubaSdkVersion}"
```

### Configure DI
To provide all required for playback classes you should initialize the Koin modules in your [Application.onCreate](app/src/main/java/com/banuba/example/playback/PlaybackApp.kt#L12) callback.
``` kotlin
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@PlaybackApp)
            allowOverride(true)
            modules(
                TokenStorageKoinModule().module,
                MainKoinModule().module,
                VeSdkKoinModule().module,
                VePlaybackSdkKoinModule().module
            )
        }
    }
```

### Prepare VideoPlayer
Before playing any videos in Video Player you should prepare the player.

To do it just invoke

```kotlin
fun prepare(size: Size): Boolean
```
function. It requires preferred size of the video. This size is used to define the aspect ratio.
Keep in mind that the real displaying size is limited by SurfaceView size.

If you want to setup VideoPlayer in any moment in the future, but not right after prepare() invocation,
there is helper function:

```kotlin
fun isPrepared(): Boolean
```

To setup the SurfaceView for the playback you should pass its holder inside

```kotlin
fun setSurfaceHolder(surfaceHolder: SurfaceHolder)
```

To setup preferable video size utilize

```kotlin
fun setVideoSize(size: Size)
```

function. It requires video size as an argument. This size will be scaled inside the SurfaceView keeping video's aspect ratio.

To define how the video will be scaled inside the SurfaceView you can provide PlayerScaleType:

```kotlin
fun setScaleType(scaleType: PlayerScaleType)
```

In case of any error ```prepare()``` function returns "false" as a result. More detailed information can be found in terminal by the "BanubaVideoPlayer" tag.

Playback API consumes VideoRecordRanges objects as video playlist.
There is a convenient function inside VideoEditorUtils that allows you to create VideoRecordRange from video uri:

```kotlin
fun createVideoRecordRange(
    sourceUri: Uri,
    context: Context,
    playFromMs: Long = 0,
    playToMs: Long? = null,
    rotation: Rotation = Rotation.ROTATION_0
): VideoRecordRange?
```

### Playback functions

VideoPlayer has a bulk of standard functions which organize playback:

```kotlin
fun play(isRepeat: Boolean)
fun pause()
fun isPlaying(): Boolean
fun setVolume(volume: Float)
fun seekTo(positionMs: Int)
```

### Playback with effects

The main feature of the BanubaVideoPlayer is an ability to apply visual, speed and music effects on videos.
This sample provides you an example of how to prepare effects of different types before applying inside BanubaVideoPlayer.

You have to track any changes in effects list on your own and just pass the list of effects into VideoPlayer:

```kotlin
fun setEffects(effects: List<TypedTimedEffect<*>>)
```

And the same is with music effects:

```kotlin
fun setMusicEffects(effects: List<MusicEffect>)
```

In this sample you may notice ```appliedEffects``` list inside ```MainViewModel``` that is used to provide visual and speed effects into BanubaVideoPlayer.

To remove effects from playback pass an empty list in corresponding function:

```kotlin
setEffects(emptyList())

setMusicEffects(emptyList())
```

There is an option to disable speed (Rapid, Slow Motion) and object (Gif, Text) effects by utilizing corresponding fuctions:

```kotlin
fun enableSpeedEffects(enable: Boolean)
fun enableObjectEffects(enable: Boolean)
```

### How to prepare effects for playback

By default Visual and Time effects are applied on the whole video playback duration. Use ```VideoEffectsHelper``` to create effects.
For example, a color filter effect (lut) can be created next way:

```kotlin
val colorEffectFile = context.copyFromAssetsToExternal("color_filter_example.png")
VisualTimedEffect(VideoEffectsHelper.createLutEffect(colorEffectFile.path, Size(1024, 768)))
```

Video (Fx) effect can be created next way:

```kotlin
val fxDrawable = VideoEffectsHelper.createFxEffect(
    context = context,
    resourceIdentifier = "vhs"
) ?: throw Exception("Video effect is not available!")
val fxEffect = VisualTimedEffect(effectDrawable = fxDrawable)
```

Here ```resourceIdentifier``` is a string identifier of video effect. All avaiable string identifiers can be find [here](https://github.com/Banuba/ve-sdk-android-integration-sample/blob/main/mddocs/editor_styles.md#effects-customization).

Also client can create some custom effect. To do this you need to implement ```IVisualEffectDrawable``` interface with ```DrawType.CUSTOM``` type and to implement ```EffectRenderer``` interface. See the sample of [CustomEffectDrawable](./app/src/main/java/com/banuba/example/playback/CustomEffectDrawable.kt ) and [CustomEffectRenderer](./app/src/main/java/com/banuba/example/playback/CustomEffectRenderer.kt) for more details.

However effects can start and finish from any position on the playback.
To setup time borders for effects you should use ```TimeBundle(window: Int, time: Int)``` data class for start and finish points.
Here ```window``` is an index of video inside playback which will be the first video where this effect is applied.
```time``` is a position on that video to start effect from.

For example, you have 3 videos in playback with every duration of 1000 ms and going to setup effect to start from the beginning of the second video and finish in the end of playback.
In this case you should create it as follows (note, that here end position is not required to be set up, because the effect ends in the end of playback automatically):

```kotlin
VisualTimedEffect(
  effectDrawable = effectDrawable,
  startTimeBundle = TimeBundle(1, 0),
  startTotal = 1000)
```

If you want to play effect only during the second video playback, you need to setup finish position:

```kotlin
VisualTimedEffect(
  effectDrawable = effectDrawable,
  startTimeBundle = TimeBundle(1, 0),
  startTotal = 1000,
  endTimeBundle = TimeBundle(1, 1000),
  endTotal = 2000)
```

**Note** ```startTimeBundle``` and ```endTimeBundle``` define position within certain video in playback, ```startTotal``` and ```endTotal``` define position on the whole video playback.

Preparing of music effects are described [here](app/src/main/java/com/banuba/example/playback/MainViewModel.kt#L130).

### VideoPlayer.Callback

To receive updates from VideoPlayer you have to setup callback:

```kotlin
fun setCallback(callback: Callback?)
```

The VideoPlayer.Callback allows to track the recent playback position:

```kotlin
fun onVideoPositionChanged(positionMs: Int)
```

to handle any errors:

```kotlin
fun onVideoPlaybackError(error: PlaybackError)
```

to get the recent frame as a screenshot (may be used to receive video preview):

```kotlin
fun onScreenshotTaken(bmp: Bitmap)
```

to track changes with the video size:

```kotlin
fun onViewPortChanged(viewport: GlViewPort)
```

```GLViewPort``` is a helper object containing top left coordinates (x and y) and the real size of the video view port on the device.
You can use it to create more precise placement for Text and Gif effects that required coordinates while creating and applying on the video.

All callback functions has an empty body as a default implementation. So you can omit any function if you do not need it.

### Getting screenshot

To get a recent frame as a screenshot there is a function in BanubaVideoPlayer:

```kotlin
fun takeScreenshot()
```

After a while the resulting bitmap passed into ```onScreenshotTaken(bmp: Bitmap)``` callback of VideoPlayer.Callback.
The size of the bitmap is the same as the size of the video shown in VideoPlayer.

### Releasing the player

To freed up resources after using VideoPlayer use:

```kotlin
fun release()
```

Do not forget to clear SurfaceHolder also:

```kotlin
fun clearSurfaceHolder(surfaceHolder: SurfaceHolder)
```

## API Reference

```ve-playback-sdk``` module contains interfaces and classes for managing video playback with applied music, visual and time effects.

[API reference](index.md)
