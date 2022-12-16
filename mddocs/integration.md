## Video Editor API Integration on Android

### Add dependencies
Add Video Editor API dependencies in [app/build.gradle](app/build.gradle) file.

``` groovy
  def banubaSdkVersion = '1.25.0'
  implementation "com.banuba.sdk:ffmpeg:4.4"
  implementation "com.banuba.sdk:banuba-token-storage-sdk:${banubaSdkVersion}"
  implementation "com.banuba.sdk:core-sdk:${banubaSdkVersion}"
  implementation "com.banuba.sdk:ve-sdk:${banubaSdkVersion}"
  implementation "com.banuba.sdk:ve-playback-sdk:${banubaSdkVersion}"
  implementation "com.banuba.sdk:ve-export-sdk:${banubaSdkVersion}"
  implementation "com.banuba.sdk:ve-effects-sdk:${banubaSdkVersion}"
  
  // Only if you use Banuba Face AR
  implementation 'com.banuba.sdk:effect-player:1.5.0'
```

:exclamation: __Important__  
Project **libs** directory contains `banuba_sdk-release.aar` for integrating Camera functionality into this sample 
to demonstrate an optional flow where the user starts from camera and can navigate to the next screens where 
actual Video Editor API is integrated.  
You can omit this flow and Camera functionality as well.  

### Supported media formats
| Audio      | Video      |
| ---------- | ---------  | 
|mp3, aac, wav, m4a, flac, aiff |mp4, mov, m4v|

### API features
Video Editor API includes
- Playback API
- Export API

### Playback API
With this API you can cover the following use cases:
1. Video trimming
2. Cover selection
3. Video post-processing i.e. applying effects

Follow [our guide](playback/playback.md) to integrate Playback API into your Android project.

### Export API
With this API you can implement creating a number of video files with:
1. Various resolution
2. Various effects
3. Separate audio file

Follow [our guide](export/export.md) to integrate Export API into your Android project.




