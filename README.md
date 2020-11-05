
## 一个可折叠伸展的TextView

效果图如下：当TextView文本大于指定最大行数，自动实现重叠或伸展功能。

![效果图](https://github.com/Android-XXM/ExtendTextView/blob/master/app/1.gif)

## 如何使用：

1、项目根目录添加如下依赖(已经有了就不用添加啦)
```
allprojects {
    repositories {
	...
	maven { url 'https://jitpack.io' }
    }
}
  
```
2、需要使用的组件添加如下依赖：
```
dependencies {
    implementation 'com.github.Android-XXM:ExtendTextView:1.0.1'
}
```
3、在XML文件
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <com.zhangws.ExtendTextView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:text="我是新小梦，这个一款支持折叠和伸展的TextView,常用在TextView文本较多，需要折叠显示，具有很强的自定义性，欢迎大家使用和Star~
\n欢迎大家使用我开发记账软件：快速记账\nAPP没有广告、社区、也不需要付费，单纯记账和一些实用功能，欢迎开发者加入一起开发" />
</LinearLayout>
```
