[![](https://www.banuba.com/hubfs/Banuba_November2018/Images/Banuba%20SDK.png)](https://www.banuba.com/video-editor-sdk)

# Video Editor SDK. VE API Integration sample for Android.

- [API Reference](#API-Reference)
    + [FAR Camera](#FAR-Camera)
    + [Playback API](#Playback-API)
    + [Export API](#Export-API)
    + [Core API](#Core-API)
- [Requirements](#Requirements)
- [VE API size](#ve-api-size)
- [Starting a free trial](#Starting-a-free-trial)
- [Token](#Token)
- [Getting Started](#Getting-Started)
    + [Add dependencies](#Add-dependencies)

Sample is a basic integration of VE API. Navigation flow consists of camera, editor, gallery screens with Facade API entities, funcs implementations.

See how to use basic screen settings with the API Entites. All of API referencies you could find out here:

## API Reference
### FAR Camera

Camera is representation of BanubaSDK and BanubaEffectPlayer. All relevant information and docs is [here](https://docs.banuba.com/face-ar-sdk/android/android_overview).

### Playback API

```ve-playback-sdk``` module contains interfaces and classes for managing video playback with applied music, visual and time effects.

[API reference](https://github.com/Banuba/ve-sdk-android-playback-sample/blob/master/mddocs/playback/index.md)\
[Playback Sample](https://github.com/Banuba/ve-sdk-android-playback-sample)

### Export API

```ve-export-sdk``` module contains interfaces and classes for handling and configuring export process.

[API Reference](https://github.com/Banuba/ve-sdk-android-export-sample/blob/master/mddocs/index.md)\
[Export Sample](https://github.com/Banuba/ve-sdk-android-export-sample)

### Core API

```banuba-token-storage-sdk``` module providing the functionality of receiving and decoding token.

[API reference](https://github.com/Banuba/ve-sdk-android-playback-sample/blob/master/mddocs/tokenStorage/index.md)

## Requirements
This is what you need to run the Export API
- Java 1.8+
- Kotlin 1.4+
- Android Studio 4+
- Android OS 6.0 or higher
- OpenGL ES 3.0  

## VE API size

| Options | Mb      | Note |
| -------- | --------- | ----- |
| VE API  | 38.1 | AR effect sizes are not included. AR effect takes 1-3 MB in average.

## Starting a free trial

You should start with getting a trial token. It will grant you **14 days** to freely play around with the AI Video Editor SDK and test its entire functionality the way you see fit.

There is nothing complicated about it - [contact us](https://www.banuba.com/video-editor-sdk) or send an email to sales@banuba.com and we will send it to you. We can also send you a sample app so you can see how it works “under the hood”.


## Token
We offer а free 14-days trial for you could thoroughly test and assess Export API functionality in your app. To get access to your trial, please, get in touch with us by [filling a form](https://www.banuba.com/video-editor-sdk) on our website. Our sales managers will send you the trial token.

Banuba token should be put [here](https://github.com/Banuba/ve-api-android-integration-sample/blob/main/app/src/main/res/values/strings.xml#L3).

## Getting Started
### Add dependencies
Please, specify a list of dependencies as in [app/build.gradle](app/build.gradle) file to integrate export functionality of Export API.

``` groovy
def banubaSdkVersion = '1.24.0'
implementation "com.banuba.sdk:ffmpeg:4.4"
implementation "com.banuba.sdk:banuba-token-storage-sdk:${banubaSdkVersion}"
implementation "com.banuba.sdk:core-sdk:${banubaSdkVersion}"
implementation "com.banuba.sdk:ve-sdk:${banubaSdkVersion}"
implementation "com.banuba.sdk:ve-playback-sdk:${banubaSdkVersion}"
implementation "com.banuba.sdk:ve-export-sdk:${banubaSdkVersion}"
implementation "com.banuba.sdk:ve-effects-sdk:${banubaSdkVersion}"

implementation 'com.banuba.sdk:effect-player:0.38.5'
```

Also create **libs** directory in your project and add `banuba_sdk-release.aar`. Then open build.gradle (Module: app) and add Banuba SDK dependencies for your project:

``` groovy
// Banuba Face AR SDK dependencies
implementation fileTree(dir: '../libs', include: ['*.aar'])
```

## Migration guides
[1.23.0](https://vebanuba.notion.site/1-23-d91a638b6e714141a2ba53dfa1823918)\
[1.23.1](https://vebanuba.notion.site/1-23-1-3e683d15f95642528a02ce59b866b3da)\
[1.24.0](https://vebanuba.notion.site/1-24-08daf21e88d64e0cae3747eb5a4b0d08)\
[1.24.1](https://vebanuba.notion.site/1-24-1-0550e0880f38434bb45f928f7035a568)\
[1.24.2](https://vebanuba.notion.site/1-24-2-fffb57ad78b246af9a0903be8626967a)
