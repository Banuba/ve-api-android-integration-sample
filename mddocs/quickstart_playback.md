# Playback API Quickstart

- [Overview](#Overview)
- [Prerequisites](#Prerequisites)
- [Get video player](#Get-video-player)
- [Prepare video player](#Prepare-video-player)
- [Release video player](#Release-video-player)
- [Set event callback](#Set-event-callback)
- [Add video playlist](#Add-video-playlist)
- [Manage video player actions](#Manage-video-player-actions)
- [Add audio track](#Add-audio-track)
- [Manage effects](#Manage-effects)

## Overview
This guide helps you quickly integrate the Playback API into your project. You'll learn core features and build common use cases for video editing applications.

## Prerequisites
Complete [Installation](../mddocs/overview.md#Installation).

## Get video player
Access [`VideoPlayer`](playback/ve-playback-sdk/com.banuba.sdk.playback/-video-player/index.md) instance from Video Editor API module:

### Option 1: Koin Injection in Activity/Fragment
```kotlin
import org.koin.android.ext.android.inject

class SampleActivity : AppCompatActivity() {
    private val videoPlayer: VideoPlayer by inject()
    // ...
}
```

### Option 2: Koin Injection in ViewModel
```diff
 viewModel {
   PlaybackViewModel(
+       videoPlayer = get()
   )
 }
```

## Prepare video player
```VideoPlayer``` requires a ```SurfaceView``` and its ```SurfaceHolder``` for video rendering.

1. Prepare with video size
```kotlin
videoPlayer.prepare(videoSize) // Returns true if successful
```

2. Set surface holder

```kotlin
videoPlayer.setSurfaceHolder(surfaceHolder)
```
**Note**: Check logs with tag ```BanubaVideoPlayer``` if preparation fails.

## Set event callback
Monitor playback state and position changes:
```kotlin
videoPlayer.setCallback(videoPlayerCallback)
```
See [supported callback methods](playback/ve-playback-sdk/com.banuba.sdk.playback/-video-player/-callback/index.md)

## Release Video Player
Always release resources when leaving the screen (e.g., in Activity.onDestroy):

```kotlin
videoPlayer.clearSurfaceHolder(surfaceHolder)
videoPlayer.release()
```

## Add video playlist
Use ```VideoPlayer.setVideoRanges``` with a list of ```VideoRecordRange``` objects:
```kotlin
videoPlayer.setVideoRanges(videoRangesList)
```

### Convert Uri to VideoRecordRange
```kotlin
// Convert each video Uri to VideoRecordRange with required properties
// Validate each source - player logs unsupported formats
```

:exclamation:Important  
Supports [device-stored media formats](../README.md#Supported-media-formats). 


📌 [Full implementation example](../app/src/main/java/com/banuba/example/videoeditor/playback/PlaybackViewModel.kt#L94)

## Manage video player actions
Core playback controls:

- [videoPlayer.play](playback/ve-playback-sdk/com.banuba.sdk.playback/-video-player/play.md)
- [videoPlayer.pause](playback/ve-playback-sdk/com.banuba.sdk.playback/-video-player/pause.md)
- [videoPlayer.isPlaying](playback/ve-playback-sdk/com.banuba.sdk.playback/-video-player/is-playing.md)
- [videoPlayer.setVolume](playback/ve-playback-sdk/com.banuba.sdk.playback/-video-player/set-volume.md)
- [videoPlayer.seekTo](playback/ve-playback-sdk/com.banuba.sdk.playback/-video-player/seek-to.md)

Use [videoPlayer.play](playback/ve-playback-sdk/com.banuba.sdk.playback/-video-player/play.md) to play video when ```VideoPlayer``` is prepared and video playlist is set
```kotlin
videoPlayer.play(shouldRepeat) // true - repeat playing
```

Example: Seek to position
```kotlin
videoPlayer.seekTo(3000L) // Seek to 3rd second
```

## Add Audio Track
Add external audio tracks on top of video soundtrack:  

### Convert Uri to MusicEffect
```kotlin
// Convert audio Uri to MusicEffect instance
```

### Apply to video player
```kotlin
videoPlayer.setMusicEffects(tracks) // List<MusicEffect>
```

📌 [Full implementation example](../app/src/main/java/com/banuba/example/videoeditor/playback/PlaybackViewModel.kt#L180)

## Manage Effects
The API requires you to implement effect management. Use ```VideoEffectsHelper``` for effect creation.

Effect List Management
```kotlin
val effects = mutableListOf<TypedTimedEffect<*>>()

// Add/remove effects based on user actions
videoPlayer.setEffects(effects)
```

exclamation:Important  
License token determines allowed FX and Speed effects. Using disallowed effects may cause crashes.

### Color Effect
Apply color filter from [assets](../app/src/main/assets):

```kotlin
  val colorEffectFile = context.copyFromAssetsToExternal("color_filter_example.png")
  val videoSize = Size(1024, 768)
  val effect = VisualTimedEffect(VideoEffectsHelper.createLutEffect(colorEffectFile.path, videoSize))
```

### Speed Effect
Create ```Rapid``` or ```SlowMo``` speed effects.
```kotlin
val rapidEffect = SpeedTimedEffect(VideoEffectsHelper.createSpeedEffect(2F)) // 2x speed
val slowMoEffect = SpeedTimedEffect(VideoEffectsHelper.createSpeedEffect(0.5F)) // 0.5x speed
```

### FX Effect
Apply visual effects (check license for availability):

```kotlin
val fxName = "VHS"
val availableList = VideoEffectsHelper.provideVisualEffects(context)
val vhsDrawable = availableList.find {
  context.getString(it.nameRes) == fxName
}?.provide() ?: throw Exception("VHS video effect is not available!")

if (vhsDrawable !is VisualEffectDrawable) throw TypeCastException("Drawable is not IVisualEffectDrawable type!")
val vhsEffect = VisualTimedEffect(effectDrawable = vhsDrawable)
```

### Sticker Effect
Add animated stickers (GIF):

```kotlin
val stickerUri = context.copyFromAssetsToExternal("example.gif").toUri()
val x = 100f
val y = 100f
val width = 100f
val height = 100f
val scale = 1f
val rotation = 20

val rectParams = RectParams().apply { setCoordinates(x, y, width, height, scale, rotation) }

val stickerEffect = VisualTimedEffect(
  effectDrawable = VideoEffectsHelper.createGifEffect(
    UUID.randomUUID(),
    stickerUri,
    rectParams
  )
)
```
:bulb: Hint      
If you use sticker services as [GIPHY](https://giphy.com/) you should download sticker as ```.gif``` file to the device and
then use this file to create the effect.

### Text Effect
Add custom text overlay:

```kotlin
val width = 800
val height = 200

val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

val canvas = Canvas(bitmap)
val paint = Paint(Paint.ANTI_ALIAS_FLAG)
paint.color = Color.WHITE
paint.style = Paint.Style.FILL
paint.textSize = 64f
canvas.drawText(text, 0f, 60f, paint)

val rectParams = RectParams().apply {
  setCoordinates(
    25f /* x position */,
    25f, /* y position */
    bitmap.width.toFloat(),
    bitmap.height.toFloat(),
    0.8f, /* scale */
    0f /* rotation */
  )
}

val textEffect =  VisualTimedEffect(
  effectDrawable = VideoEffectsHelper.createTextEffect(
    UUID.randomUUID(),
    bitmap,
    rectParams
  )
)
```

### Blur Effect
Apply blur mask:

```kotlin
val (width, height) = viewportSize.width to viewportSize.height
val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8)
val canvas = Canvas(bitmap)
val paint = Paint()
paint.color = Color.WHITE
canvas.drawCircle(width / 2.0f, height / 2.0f, height / 5.0f, paint)
val blurEffect = VisualTimedEffect(effectDrawable = BlurEffectDrawable(bitmap))
```

### AR Effect
Requires [Face AR SDK](https://www.banuba.com/facear-sdk/face-filters):

Dependencies:
```gradle
implementation "com.banuba.sdk:effect-player-adapter:${banubaSdkVersion}"
```

Module setup:
```kotlin
// Include BanubaEffectPlayerKoinModule in your Koin modules list
```

Apply AR effect:
```kotlin
val preparedMaskEffect = BanubaEffectHelper(context).prepareEffect(effectName)

val maskEffect = VisualTimedEffect(
  effectDrawable = VideoEffectsHelper.createMaskEffect(preparedMaskEffect.uri)
)
```

### Adjust Effect Boundaries
By default, effects apply to entire video. Customize using timing parameters:

#### VisualTimedEffect constructor
```kotlin
class VisualTimedEffect(
    effectDrawable: VisualEffectDrawable,
    startTimeBundle: TimeBundle = TimeBundle(0, 0),
    startTotal: Int = 0,
    endTimeBundle: TimeBundle = TimeBundle(Int.MAX_VALUE, Int.MAX_VALUE),
    endTotal: Int = Int.MAX_VALUE
) : TypedTimedEffect<VisualEffectDrawable>

```

#### SpeedTimedEffect constructor
```kotlin
class SpeedTimedEffect(
    effectDrawable: SpeedEffectDrawable,
    startTimeBundle: TimeBundle = TimeBundle(0, 0),
    startTotal: Int = 0,
    endTimeBundle: TimeBundle = TimeBundle(0, Int.MAX_VALUE),
    endTotal: Int = Int.MAX_VALUE
) : TypedTimedEffect<SpeedEffectDrawable>
```
Use these parameters to control when effects start and end during playback.

### Next Steps
[Export API Quickstart](../mddocs/quickstart_export.md)
