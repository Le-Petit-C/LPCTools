package lpctools.tools.canSpawnDisplay;

//索引使用GL_UNSIGNED_BYTE
public interface IRenderMethod {
    String getNameKey();
    ICSShapeRegister getShapeRegister(boolean xrays);
}
