package lpctools.scripts.suppliers;

import com.google.common.collect.ImmutableMap;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.suppliers.blockPos.PlayerBlockPos;
import lpctools.scripts.suppliers.interfaces.IScriptSupplier;
import lpctools.scripts.suppliers.staticSuppliers.StaticBlockPos;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.function.Consumer;
import java.util.function.Function;

import static lpctools.lpcfymasaapi.screen.ChooseScreen.*;

public class Choosers {
	public static void chooseBlockPos(Consumer<Function<ILPCConfigReadable, IScriptSupplier<BlockPos>>> callback) {
		openChooseScreen(
			Text.translatable("lpctools.configs.scripts.choosers.blockPos.title").getString(),
			true, blockPosOptionTree, blockPosChooseTree, callback
		);
	}
	public static final ImmutableMap<String, OptionCallback<Consumer<Function<ILPCConfigReadable, IScriptSupplier<BlockPos>>>>> blockPosOptionTree
		= ImmutableMap.<String, OptionCallback<Consumer<Function<ILPCConfigReadable, IScriptSupplier<BlockPos>>>>>builder()
		.put(StaticBlockPos.fullKey, (button, mouseButton, callback)->callback.accept(StaticBlockPos::new))
		.put(PlayerBlockPos.fullKey, (button, mouseButton, callback)->callback.accept(PlayerBlockPos::new))
		.build();
	public static final ImmutableMap<String, ?> blockPosChooseTree = ImmutableMap.<String, Object>builder()
		.put(StaticBlockPos.fullKey, StaticBlockPos.fullKey)
		.put(PlayerBlockPos.fullKey, PlayerBlockPos.fullKey)
		.build();
}
