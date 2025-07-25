package lpctools.scripts.runners;

import fi.dy.masa.litematica.util.ToBooleanFunction;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.choosers.BlockPosSupplierChooser;
import lpctools.scripts.choosers.BooleanSupplierChooser;
import lpctools.scripts.choosers.Direction6SupplierChooser;
import lpctools.scripts.choosers.Vector3dSupplierChooser;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class InteractBlock extends WrappedThirdListConfig implements IScriptRunner{
	Vector3dSupplierChooser pos = addConfig(new Vector3dSupplierChooser(parent, "pos", null));
	Direction6SupplierChooser side = addConfig(new Direction6SupplierChooser(parent, "side", null));
	BlockPosSupplierChooser blockPos = addConfig(new BlockPosSupplierChooser(parent, "blockPos", null));
	BooleanSupplierChooser mainHand = addConfig(new BooleanSupplierChooser(parent, "mainHand", null));
	public InteractBlock(ILPCConfigReadable parent) {
		super(parent, nameKey, null);
		setValueChangeCallback(this::notifyScriptChanged);
	}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> pos.openChoose(), ()->fullKey + ".pos", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> side.openChoose(), ()->fullKey + ".side", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> blockPos.openChoose(), ()->fullKey + ".blockPos", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> mainHand.openChoose(), ()->fullKey + ".mainHand", buttonGenericAllocator);
	}
	@Override public @NotNull Consumer<CompiledVariableList> compile(VariableMap variableMap) throws CompileFailedException {
		BiConsumer<CompiledVariableList, Vector3d> pos = this.pos.get().compileToVector3d(variableMap);
		Function<CompiledVariableList, Direction> side = this.side.get().compile(variableMap);
		BiConsumer<CompiledVariableList, BlockPos.Mutable> blockPos = this.blockPos.get().compileToBlockPos(variableMap);
		ToBooleanFunction<CompiledVariableList> mainHand = this.mainHand.get().compileToBoolean(variableMap);
		Vector3d posBuf = new Vector3d();
		BlockPos.Mutable blockPosBuf = new BlockPos.Mutable();
		return list->{
			ClientPlayerInteractionManager itm = MinecraftClient.getInstance().interactionManager;
			ClientPlayerEntity player = MinecraftClient.getInstance().player;
			if(itm != null && player != null){
				pos.accept(list, posBuf);
				Direction sideBuf = side.apply(list);
				blockPos.accept(list, blockPosBuf);
				BlockHitResult hitResult =
					new BlockHitResult(new Vec3d(posBuf.x, posBuf.y, posBuf.z),
						sideBuf, blockPosBuf.toImmutable(), false);
				itm.interactBlock(player, mainHand.applyAsBoolean(list) ? Hand.MAIN_HAND : Hand.OFF_HAND, hitResult);
			}
		};
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "interactBlock";
	public static final String fullKey = IScriptRunner.fullPrefix + nameKey;
	//public static final String interactPosButtonKey = fullKey + ".interactPos";
}
