# Tree Hole OCR

[English](./readme.md) | [中文](./readme-cn.md)

## Introduction

- Local OCR Recognition: Tree Hole OCR text recognition tool does not require internet connection. It leverages local OCR technology, based on Paddle OCR model and deep learning frameworks such as PyTorch, DJL, to provide fast and accurate text recognition.
- Cross-platform compatibility: Developed with Java 1.8 and JavaFX, it supports operation on different operating systems, including Mac OS X 12.6 and above.
- Powerful functionality: In addition to basic text recognition, it also includes PDF recognition, image text recognition, shortcut key screenshot recognition, and more.

## Dependencies Library

- JDK 1.8
- JavaFX
- DJL
- PyTorch
- ONNX
- Paddle OCR
- OpenCV

## Open Source Address

[gitee](https://gitee.com/ppnt/tools-ocr) | [github](https://github.com/litongjava/tools-ocr)

## Documentation

https://tree-hole-ocr-docs.vercel.app/

## Requirements

- Mac OS X 12.6 due to dependency on DJL 0.25.0

## Installation

> - **Please do not include Chinese characters in the installation path**;
> - This program is developed with JavaFX, and the installation package provided already includes Java.
> - Download the latest version from [release](https://github.com/litongjava/tools-ocr/releases/) and unzip it for installation.

## Using the Program

### Screenshot

- Method one: Click the screenshot button on the main interface of the program;
- Method two: Press the screenshot shortcut key F4.

### Selecting Area

After entering the screenshot interface, press and hold the left mouse button, then drag to select the area you want to capture;
After completing the selection, you can fine-tune the selected area:

- Use **arrow keys** to adjust the right and top borders of the selected area;
- Use **Shift + arrow keys** to adjust the left and bottom borders of the selected area;
- Use **Ctrl + A** to select the entire screen.

### Confirm Selection

After completing the selection, press `Enter` or `Space` key, or double-click the left mouse button to confirm the selection; Once confirmed, the program will automatically perform OCR text recognition on the selected area.

- image

  ![](readme_files/3.jpg)

- result:

  ![](readme_files/4.jpg)

## Local Build

### Download and Unzip the Models

```
wget https://github.com/litongjava/tools-ocr/releases/download/model-ppocr-v4/ch_PP-OCRv4_rec_infer-onnx.zip
wget https://github.com/litongjava/tools-ocr/releases/download/model-ppocr-v4/ch_PP-OCRv4_det_infer-onnx.zip
```

Unzip the models

```
mkdir models/ch_PP-OCRv4_rec_infer
mkdir models/ch_PP-OCRv4_det_infer
unzip /Users/mac/Downloads/ch_PP-OCRv4_rec_infer-onnx.zip -d models/ch_PP-OCRv4_rec_infer
unzip /Users/mac/Downloads/ch_PP-OCRv4_det_infer-onnx.zip -d models/ch_PP-OCRv4_det_infer
```

### Build the Program

You can download the code and build it locally. The build commands are as follows:
windows

```
mkdir target\jfx\app
cp -r models target\jfx\app
mvn jfx:native -DskipTests -f pom.xml
```

macos

```shell script
rm -rf target/jfx/app
mkdir -p target/jfx/app
cp -r models target/jfx/app
mvn jfx:native -DskipTests -f pom.xml
```

## View System Operating Log

cd treehole.app/Contents/java/logs

## Notices

### MAC Permission Settings

Since screenshot shortcuts are monitored, MAC needs appropriate permissions settings, as shown below:

- Settings --> Security and Privacy --> Accessibility
  ![MAC Permission Settings](readme_files/5.jpg)
- Settings --> Security and Privacy --> Screen Recording
  ![2](readme_files/2.jpg)

## Common Directories

- Log directory /Applications/treehole.app/Contents/Java/logs
- Temporary image saving directory /Applications/treehole.app/Contents/Java

## TODO

- [x] PDF Recognition
- [x] Image Text Recognition
  - [x] Recognition result text alignment (multi-column yet to be implemented)
  - [x] Full screen mode screenshot
  - [x] Adding recognition animation
  - [x] Multi-screen support
- [ ] Text Translation
- [ ] Formula Recognition
- [ ] Table Recognition
- [ ] Software Settings
