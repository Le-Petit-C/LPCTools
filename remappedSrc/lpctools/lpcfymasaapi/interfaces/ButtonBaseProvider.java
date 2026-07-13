package lpctools.lpcfymasaapi.interfaces;

public interface ButtonBaseProvider {
    void addButtons(int x, int y, float zLevel, int labelWidth, int configWidth, ButtonConsumer consumer);
}
