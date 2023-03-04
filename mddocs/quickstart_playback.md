# Quickstart Playback API

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
This guide is aimed to help you quickly integrate Playback API into your project. 
You will learn how to use core features and build use cases and meet your requirements.

## Prerequisites
Please complete [Installation](../README.md#Installation) and [Setup API](overview.md#Setup-API) steps before to proceed.

## Get video player
[VideoPlayer](playback/ve-playback-sdk/com.banuba.sdk.playback/-video-player/index.md) instance is created in Video Editor API module.
You can access to this instance in 2 ways
1. Using [Koin](https://insert-koin.io/) inject in Android Fragment or Activity classes.
```diff
+ import org.koin.android.ext.android.inject

class SampleActivity : AppCompatActivity() {
+ private val videoPlayer: VideoPlayer by inject()
 ...
}
```
2. In Android [ViewModel](../app/src/main/java/com/banuba/example/videoeditor/VideoEditorApiModule.kt#L70) using Koin.
```diff
 viewModel {
   PlaybackViewModel(
+       videoPlayer = get()
   )
 }
```

## Prepare video player
```VideoPlayer``` requires Android ```SurfaceView``` and its ```SurfaceHolder``` to render video.

Use [VideoPlayer.prepare](playback/ve-playback-sdk/com.banuba.sdk.playback/-video-player/prepare.md) method and pass preferred video size.
The size is used to define the aspect ratio. Please keep in mind that the real displaying size is limited by ```SurfaceView``` size.
```prepare``` method returns ```true``` if the player is prepared successfully, otherwise ```false``` and you can find reasons and error message in logs by the tag ```BanubaVideoPlayer```.
```kotlin
videoPlayer.prepare(videoSize)
```

Next, use [VideoPlayer.setSurfaceHolder](playback/ve-playback-sdk/com.banuba.sdk.playback/-video-player/set-surface-holder.md) and pass instance of ```SurfaceHolder``` of
```SurfaceView``` where you want to play video.
```kotlin
videoPlayer.setSurfaceHolder(surfaceHolder)
```

## Set event callback
Use [VideoPlayer.setCallback](playback/ve-playback-sdk/com.banuba.sdk.playback/-video-player/set-callback.md) to get notified about video player changes while playback i.e. play state or position changed.
```kotlin
videoPlayer.setCallback(videoPlayerCallback)
```
Learn more about supported [callbacks](playback/ve-playback-sdk/com.banuba.sdk.playback/-video-player/-callback/index.md)

## Release video player
It is highly recommended to stop and release video player if the user leaves the screen.
For example, you can implement it in ```Activity.onDestroy``` method.

Use [VideoPlayer.clearSurfaceHolder](playback/ve-playback-sdk/com.banuba.sdk.playback/-video-player/clear-surface-holder.md) and
[VideoPlayer.release](playback/ve-playback-sdk/com.banuba.sdk.playback/-video-player/release.md) methods to fully stop and release video player.
```kotlin
videoPlayer.clearSurfaceHolder(surfaceHolder)
videoPlayer.release()
```

## Add video playlist
Use [VideoPlayer.setVideoRanges](playback/ve-playback-sdk/com.banuba.sdk.playback/-video-player/set-video-ranges.md) method and pass ```List<VideoRecordRange>``` 
to add video playlist you want to play. ```VideoRecordRange``` is a core class in Playback API and Export API which is responsible for
describing video source and its capabilities i.e. speed, start and end positions of video to export and so on.   
:exclamation:Important  
```VideoPlayer``` supports playing video stored on the device and the following [media formats](../README.md#Supported-media-formats). 

:bulb: Hint   
You have a list of video sources as ```List<Uri>``` that are stored on the device. You need to convert each  ```Uri``` to ```VideoRecordRange``` 
by providing required properties and especially video playing boundaries.  

```VideoPlayer``` notifies if not supported video is used. We highly recommend to validate each video source you want to play.
Please [check out](../app/src/main/java/com/banuba/example/videoeditor/playback/PlaybackViewModel.kt#L94) full implementation
 of converting ```Uri``` to  ```VideoRecordRange``` and validating it in the sample.

## Manage video player actions
```VideoPlayer``` includes a number of well known player action methods for controlling video playback
- [VideoPlayer.play](playback/ve-playback-sdk/com.banuba.sdk.playback/-video-player/play.md)
- [VideoPlayer.pause](playback/ve-playback-sdk/com.banuba.sdk.playback/-video-player/pause.md)
- [VideoPlayer.isPlaying](playback/ve-playback-sdk/com.banuba.sdk.playback/-video-player/is-playing.md)
- [VideoPlayer.setVolume](playback/ve-playback-sdk/com.banuba.sdk.playback/-video-player/set-volume.md)
- [VideoPlayer.seekTo](playback/ve-playback-sdk/com.banuba.sdk.playback/-video-player/seek-to.md)

Use [VideoPlayer.play](playback/ve-playback-sdk/com.banuba.sdk.playback/-video-player/play.md) to play video when ```VideoPlayer``` is prepared and video playlist is set
```kotlin
videoPlayer.play(shouldRepeat) // true - repeat playing
```

Implementing video trimming or editing features you might need to move playback to a certain position and set start and end 
video playing boundaries. Use [VideoPlayer.seekTo](playback/ve-playback-sdk/com.banuba.sdk.playback/-video-player/seek-to.md) method to move 
playback to a certain position.  
:exclamation:Important  
Position is represented as time in milliseconds. 
```kotlin
videoPlayer.seekTo(3000L) // Will seek to 3rd second in video playlist
```

## Add audio track
```VideoPlayer``` supports adding additional audio track on top of the video's soundtrack. The additional audio track should be stored on the device. ```MusicEffect``` instance represents audio track.  
Use [VideoPlayer.setMusicEffects](playback/ve-playback-sdk/com.banuba.sdk.playback/-video-player/set-music-effects.md) to set the list of additional audio tracks to play in video player.
```kotlin
videoPlayer.setMusicEffects(tracks)
```
:bulb: Hint  
You have an audio track as ```Uri``` and you want to add it to video. You need to convert ```Uri``` to ```MusicEffect``` and set all required properties. 
Managing what audio tracks used added and removed should be implemented in your side as well. 
Please [check out](../app/src/main/java/com/banuba/example/videoeditor/playback/PlaybackViewModel.kt#L180) full implementation
of converting ```Uri``` to  ```MusicEffect``` and adding it to ```VideoPlayer```.

## Manage effects
```VideoPlayer``` supports adding a various number of effects while video playback. Learn more [supported effects](https://github.com/Banuba/ve-sdk-android-integration-sample/blob/main/mddocs/advanced_integration.md#Add-effects).  
Video Editor API differs from Video Editor SDK in that API requires you to implement effect management on your side.
API includes very handy class ```VideoEffectsHelper``` that simplifies effect creation process.  
Our sample includes special class [SampleEffectsProvider](../app/src/main/java/com/banuba/example/videoeditor/SampleEffectsProvider.kt) where the process of effect creation is implemented.

Use [VideoPlayer.setEffects](playback/ve-playback-sdk/com.banuba.sdk.playback/-video-player/set-effects.md) method to pass effects you want to play.
```kotlin
fun setEffects(effects: List<TypedTimedEffect<*>>)
```

Implement effect management on your side by tracking what effects the user adds or removes.
```kotlin
val effects = mutableListOf<TypedTimedEffect<*>>()
```

:exclamation:Important  
The license token includes the list of allowed ```FX``` and ```Speed``` effects. Crash might happen if the not allowed effect is used. 
We will guide you further how to get list of allowed effects.

The following sections explain how to create various effects. Every created effect is represented as ```TypedTimedEffect```. 
You should add it to the list of effects and then use [VideoPlayer.setEffects](playback/ve-playback-sdk/com.banuba.sdk.playback/-video-player/set-effects.md).

### Create color effect
In this example, we create a color effect that is applied to the whole video.
```"color_filter_example.png"``` is the name of the color effect located in [assets](../app/src/main/assets) folder.
```kotlin
  val colorEffectFile = context.copyFromAssetsToExternal("color_filter_example.png")
  val videoSize = Size(1024, 768)
  val effect = VisualTimedEffect(VideoEffectsHelper.createLutEffect(colorEffectFile.path, videoSize))
```

### Create speed effect
In this example, we create ```Rapid``` and ```SlowMo``` speed effects.
```kotlin
val rapidEffect = SpeedTimedEffect(VideoEffectsHelper.createSpeedEffect(2F))
val slowMoEffect = SpeedTimedEffect(VideoEffectsHelper.createSpeedEffect(0.5F))
```

### Create FX effect
In this example, we create a FX effect ```VHS```.  
As mentioned before the list of allowed FX effects is in the license token.
Use ```VideoEffectsHelper.takeAvailableFxEffects``` to get available FX effects.
```kotlin
val fxName = "VHS"
val availableList = VideoEffectsHelper.takeAvailableFxEffects(context)
val vhsDrawable = availableList.find {
  context.getString(it.nameRes) == fxName
}?.provide() ?: throw Exception("VHS video effect is not available!")

if (vhsDrawable !is VisualEffectDrawable) throw TypeCastException("Drawable is not IVisualEffectDrawable type!")
val vhsEffect = VisualTimedEffect(effectDrawable = vhsDrawable)
```

### Create Sticker effect
In this example, we create Sticker effect. It requires ```.gif``` file stored on the device and ```Uri``` to locate it.  

:bulb: Hint      
If you use sticker services as [GIPHY](https://giphy.com/) you should download sticker as ```.gif``` file to the device.

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

### Create Text effect
In this example, we create Text effect.

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

### Create Blur effect
In this example, we create Blur effect.
```kotlin
val (width, height) = viewportSize.width to viewportSize.height
val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8)
val canvas = Canvas(bitmap)
val paint = Paint()
paint.color = Color.WHITE
canvas.drawCircle(width / 2.0f, height / 2.0f, height / 5.0f, paint)
val blurEffect = VisualTimedEffect(effectDrawable = BlurEffectDrawable(bitmap))
```

### Create AR effect
[Face AR SDK](https://www.banuba.com/facear-sdk/face-filters) is required to add AR effects while video playback.  
Please make sure ```"com.banuba.sdk:effect-player-adapter:${banubaSdkVersion}"``` is in [app gradle](../app/build.gradle#L59) and 
[BanubaEffectPlayerKoinModule](../app/src/main/java/com/banuba/example/videoeditor/VideoEditorApiModule.kt#L48) is in the list of Koin modules.  

Normally AR effects are stored in [assets/bnb-resources/effects](../app/src/main/assets/bnb-resources/effects) folder.
Any AR effect should be copied to internal storage of the device before applying.
Use [BanubaEffectHelper](../app/src/main/java/com/banuba/example/videoeditor/utils/BanubaEffectHelper.kt) class to prepare AR effect.
```kotlin
val preparedMaskEffect = BanubaEffectHelper(context).prepareEffect(effectName)

val maskEffect = VisualTimedEffect(
  effectDrawable = VideoEffectsHelper.createMaskEffect(preparedMaskEffect.uri)
)
```

### Adjust effect boundaries
All created effects are ```VisualTimedEffect``` or ```SpeedTimedEffect``` and 
effect is applied to the whole video by default.
```kotlin
class VisualTimedEffect(
    effectDrawable: VisualEffectDrawable,
    startTimeBundle: TimeBundle = TimeBundle(0, 0),
    startTotal: Int = 0,
    endTimeBundle: TimeBundle = TimeBundle(Int.MAX_VALUE, Int.MAX_VALUE),
    endTotal: Int = Int.MAX_VALUE
) : TypedTimedEffect<VisualEffectDrawable>

class SpeedTimedEffect(
    effectDrawable: SpeedEffectDrawable,
    startTimeBundle: TimeBundle = TimeBundle(0, 0),
    startTotal: Int = 0,
    endTimeBundle: TimeBundle = TimeBundle(0, Int.MAX_VALUE),
    endTotal: Int = Int.MAX_VALUE
) : TypedTimedEffect<SpeedEffectDrawable>
```
You can use  ```startTimeBundle```, ```startTotal```, ```endTimeBundle```, ```endTotal``` properties to adjust 
boundaries where effect is applied.