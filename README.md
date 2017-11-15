## Wilddog 视频通话 Demo

`video-demo-android` 是一个基于 `WilddogVideo SDK 2.0.0+` 开发的视频通话 Demo，包含美颜效果和录像功能。

### 本地运行
首先确认本机已经安装 [Android](http://developer.android.com/index.html) 运行环境， `Git` 以及 `Andriod Studio` 。
然后，在任意工作目录中执行以下指令获取 Demo 源码：

```
git clone git@github.com:WildDogTeam/video-demo-android.git
cd  video-demo-android
```
最后，在 `Android Studio` 中导入 `video-demo-android` 工程。

## 认证集成

需要在 [Wilddog](https://www.wilddog.com/) 官网创建 Video 应用，在 `身份认证` 中打开微信个人帐号登录方式，将本应用中的微信 `appid` 和 `secrect` 填写到配置中。


## 填写 APPID 

替换 `Constants.java` 中的 `WILDDOG_VIDEO_APP_ID` 为申请的 `VideoAppID` ，
替换 `Constants.java` 中的 `WILDDOG_SYNC_APP_ID` 为 `Sync App Id` 。


### 集成美颜

本项目只提供美颜集成代码，如需使用需要前往美颜 SDK 官网申请帐号。

#### 集成 Camera360 SDK

1.在 Camera360 官网申请账号并获取 KEY ;

2.按照 Camera360 或者涂图官网文档集成美颜 SDK;

3.将 Camera360Util 复制到你自己的项目，并且调用美颜处理方法。

#### 集成 TuSDK

1.在涂图官网申请账号，并且创建自己的应用，生成 KEY;

2.按照涂图官网文档集成美颜 SDK;

3.将 TuSDKUtil 复制到你自己的项目，并且调用美颜处理方法。


## Demo 使用的 SDK

WilddogVideo SDK 2.0.0 +


## 注意

Camera360 和 TuSDK 的 KEY 均和包名有关，如需查看美颜效果，需要自己集成或者下载[WilddogVideoDemo](http://fir.im/conversationapp)。

### 更多示例

这里分类汇总了 Wilddog 平台上的示例程序和开源应用，[链接地址](https://github.com/WildDogTeam/wilddog-demos)。

### 支持
如果在使用过程中有任何问题，请提 [issue](https://github.com/WildDogTeam/video-demo-android/issues) ，我会在 Github 上给予帮助。


### License
MIT
http://wilddog.mit-license.org/
