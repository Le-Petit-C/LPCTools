package lpctools.script.suppliers.Boolean;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptBooleanSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.util.BlockUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class CanBreakInstantly extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IBooleanSupplier {
	protected final SupplierStorage<BlockPos> blockPos = ofStorage(BlockPos.class,
		Text.translatable("lpctools.script.suppliers.boolean.canBreakInstantly.subSuppliers.blockPos.name"), "blockPos");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(blockPos);
	
	public CanBreakInstantly(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @org.jetbrains.annotations.NotNull ScriptBooleanSupplier
	compileBoolean(CompileEnvironment environment) {
		var booleanSupplier = blockPos.get().compileCheckedNotNull(environment);
		return map->{
			ClientPlayerEntity player = MinecraftClient.getInstance().player;
			if(player != null) return BlockUtils.canBreakInstantly(player, booleanSupplier.scriptApply(map));
			else return false;
		};
	}
}
