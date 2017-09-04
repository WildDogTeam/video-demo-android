## Wilddog 视频通话Demo

video-demo-android是一个基于 Wilddog video 2.0.0+ SDK 开发的视频通话demo。包含美颜效果和录像功能。

### 本地运行
首先确认本机已经安装 [Android] (http://developer.android.com/index.html)运行环境,git和 Andriod Studio 开发环境 ，然后执行下列指令：

```
git clone git@github.com:WildDogTeam/video-demo-android.git
cd  video-demo-android
```


## 认证集成

需要在Wilddog官网创建video应用,在身份认证中打开微信登录方式,将本应用中的微信appid和secrect填写到配置中

## 填写 APPID 

将申请的video appId 替换到constant中的WILDDOG_VIDEO_APP_ID
将申请的sync appId 替换到constant中的WILDDOG_SYNC_APP_ID


### 集成美颜

本项目只提供美颜集成代码,如果自己开发需要使用美颜效果:

1.去Camera360官网申请账号并获取KEY;

2.去涂图官网申请账号，并且创建自己的应用，生成KEY;

3.按照Camera 360或者涂图官网文档集成SDK;

4.将Camera360Util或者TuSDKUtil复制到你自己的项目,并且调用美颜处理方法;


## demo 使用的sdk

WilddogVideo SDK 2.0.0


## 注意

Camera 360和TuSDK的key均和包名有关,如需查看美颜效果,需要自己集成或者下载[WilddogVideoDemo](http://fir.im/conversationapp)。

### 更多示例

这里分类汇总了 Wilddog平台上的示例程序和开源应用，　链接地址：[https://github.com/WildDogTeam/wilddog-demos](https://github.com/WildDogTeam/wilddog-demos)。

### 支持
如果在使用过程中有任何问题，请提 [issue](https://github.com/WildDogTeam/video-demo-android/issues) ，我会在 Github 上给予帮助。


### License
MIT
http://wilddog.mit-license.org/