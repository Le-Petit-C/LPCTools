package fi.dy.masa.malilib.interfaces;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.class_1799;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_276;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3695;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4599;
import net.minecraft.class_4604;
import net.minecraft.class_9958;
import net.minecraft.class_1792.class_9635;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;

public interface IRenderer {
    default void onRenderGameOverlayLastDrawer(class_332 drawContext, float partialTicks, class_3695 profiler, class_310 mc) {
    }

    default void onRenderGameOverlayPostAdvanced(class_332 drawContext, float partialTicks, class_3695 profiler, class_310 mc) {
    }

    default void onRenderGameOverlayPost(class_332 drawContext) {
    }

    default void onRenderWorldPostDebugRender(class_4587 matrices, class_4604 frustum, class_4598 immediate, class_243 camera, class_3695 profiler) {
    }

    default void onRenderWorldPreWeather(
        class_276 fb, Matrix4f posMatrix, Matrix4f projMatrix, class_4604 frustum, class_4184 camera, class_9958 fog, class_4599 buffers, class_3695 profiler
    ) {
    }

    default void onRenderWorldLastAdvanced(
        class_276 fb, Matrix4f posMatrix, Matrix4f projMatrix, class_4604 frustum, class_4184 camera, class_9958 fog, class_4599 buffers, class_3695 profiler
    ) {
    }

    default void onRenderWorldLast(Matrix4f posMatrix, Matrix4f projMatrix) {
    }

    default void onRenderTooltipComponentInsertFirst(class_9635 context, class_1799 stack, Consumer<class_2561> list) {
    }

    default void onRenderTooltipComponentInsertMiddle(class_9635 context, class_1799 stack, Consumer<class_2561> list) {
    }

    default void onRenderTooltipComponentInsertLast(class_9635 context, class_1799 stack, Consumer<class_2561> list) {
    }

    default void onRenderTooltipLast(class_332 drawContext, class_1799 stack, int x, int y) {
    }

    default Supplier<String> getProfilerSectionSupplier() {
        return () -> this.getClass().getName();
    }
}
