# Frolomuse
Music player for Android

![Post_dlya_reklamy_14](https://user-images.githubusercontent.com/61359788/155957028-3ec42a90-28e1-4ae7-8ad9-e567af3ede7b.png)

## How to compile the release variant?

The release variant is signed in a special way. You need to create a file called `signing.properties` and place it in the `app` folder.
The content of the file must be like:
```
storePassword=pass1243
keyPassword=pass1243
keyAlias=debug
storeFile=../app/signing/debug/debugjks.jks
```
This is just a stub and of course you can use you own keystore.

## What needs improvements

- The code is divided into modules, but there is still a lot of code in the `app` module. At least implementations from the `di` package could be moved. 
- Change the player implementation: currently `android.media.MediaPlayer` is used as the engine, which has many issues and limitations. 
Worth a try ExoPlayer or something.
- Application consumes a lot of memory at runtime.
- Headsets are just... argh!!! Just need to implement a normal handler that will handle clicks for all headsets.
- A lot of the code is still in Java. Need to rewrite in Kotlin.
