# BlockCanary
A transparent ui-block detection library for Android. App only needs one-line-code to setup and provide application context.

# How it works
See [BlockCanary](http://blog.zhaiyifan.cn/2016/01/16/BlockCanaryTransparentPerformanceMonitor/).

# Usage

```java
public class DemoApplication extends Application {
    @Override
    public void onCreate() {
        ...
        // Do it on main process
        BlockCanary.install(this, new AppBlockCanaryContext()).start();
    }
}
```

Implement application context：
```java
public class AppBlockCanaryContext extends BlockCanaryContext {
    // override to provide context like app qualifier, uid, network type, block threshold, log save path
}
```

# Import

```gradle
dependencies {
    // or you can use compile directly if want to enable it in release package and upload log file to server
    // compile 'com.github.moduth:blockcanary:1.0.0'
    debugCompile 'com.github.moduth:blockcanary:1.0.0'
    releaseCompile 'com.github.moduth:blockcanary-no-op:1.0.0'
}
```