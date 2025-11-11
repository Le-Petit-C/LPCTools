package lpctools.script.suppliers.Boolean;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptNullableFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.BlockPos.ConstantBlockPos;
import lpctools.util.BlockUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class CanBreakInstantly extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IBooleanSupplier {
	protected final SupplierStorage<BlockPos> blockPos = ofStorage(BlockPos.class, new ConstantBlockPos(this),
		Text.translatable("lpctools.script.suppliers.Boolean.canBreakInstantly.subSuppliers.blockPos.name"), "blockPos");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(blockPos);
	
	public CanBreakInstantly(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @org.jetbrains.annotations.NotNull ScriptNullableFunction<CompileEnvironment.RuntimeVariableMap, Boolean>
	compile(CompileEnvironment variableMap) {
		var booleanSupplier = blockPos.get().compile(variableMap);
		return map->{
			ClientPlayerEntity player = MinecraftClient.getInstance().player;
			if(player != null) {
				var pos = booleanSupplier.scriptApply(map);
				if(pos == null) throw ScriptRuntimeException.nullPointer(this);
				return BlockUtils.canBreakInstantly(player, pos);
			}
			else return false;
		};
	}
}
