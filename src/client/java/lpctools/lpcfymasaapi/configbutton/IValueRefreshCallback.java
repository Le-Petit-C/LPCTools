package lpctools.lpcfymasaapi.configbutton;

public interface IValueRefreshCallback {
    //提示你配置的储存值可能发生了改变，需要更新一下
    //比如：onValueChange被调用时，玩家切换页面时，重新从配置文件里加载了内容后
    void valueRefreshCallback();
}
