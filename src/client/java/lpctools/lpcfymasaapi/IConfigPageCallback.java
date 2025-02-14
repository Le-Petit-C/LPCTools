package lpctools.lpcfymasaapi;

public interface IConfigPageCallback {
    //在该页配置可能被玩家更改，应该刷新配置时会调用这个方法，比如关闭了配置窗口，重新加载了配置文件或者切换到了其他页面
    void onPageRefresh();
}
