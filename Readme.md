# Android Soundboard Sample

This repository contains a sample soundboard project.  
It is a template containing:
- custom recycle view adapter
- user permission request at first launch
- a zip extractor
- a mediaplayer

## Guide
At first launch, the app will ask for user permission, then it will extract the files in *raw/sample.zip* in a folder inside device's internal memory.
Then it will update the main view with a list of playable files.
If you want to customize it, simply change the zip file inside *app\src\main\res\raw*
