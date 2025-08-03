package lpctools.tools.canSpawnDisplay;

public class CanSpawnDisplayData {
    public static final IRenderMethod[] renderMethods = {
        new MinihudStyleRenderMethod(),
        new FullSurfaceRenderMethod(),
        new LineCubeRenderMethod()
    };
    static RenderInstance renderInstance;
}
