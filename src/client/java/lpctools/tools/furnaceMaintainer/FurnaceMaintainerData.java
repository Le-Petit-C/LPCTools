package lpctools.tools.furnaceMaintainer;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;

public class FurnaceMaintainerData {
    static final HashSet<BlockPos> uncheckedFurnaces = new HashSet<>();
    static final ArrayList<CompletableFuture<ArrayList<BlockPos>>> detectTasks = new ArrayList<>();
    static @Nullable FurnaceMaintainerRunner runner;
}
