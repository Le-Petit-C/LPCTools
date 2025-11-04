package lpctools.script.suppliers.Vec3d;

import com.google.gson.JsonElement;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScript;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.BlockPos.ConstantBlockPos;
import lpctools.script.suppliers.ScriptSupplierLake;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FromBlockPos extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IVec3dSupplier {
	protected final SupplierStorage<BlockPos> blockPos = ofStorage(new ConstantBlockPos(this),
		Text.translatable("lpctools.script.suppliers.Vec3d.fromBlockPos.subSuppliers.blockPos.name"));
	protected final SubSupplierEntry<?>[] subSuppliers = subSupplierBuilder()
		.addEntry(BlockPos.class, blockPos)
		.build();
	
	public FromBlockPos(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SubSupplierEntry<?>[] getSubSuppliers(){ return subSuppliers; }
	
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, Vec3d>
	compile(CompileEnvironment variableMap) {
		var compiledEntitySupplier = blockPos.get().compile(variableMap);
		return map->{
			BlockPos pos = compiledEntitySupplier.scriptApply(map);
			if(pos == null) throw ScriptRuntimeException.nullPointer(this);
			return Vec3d.of(pos);
		};
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {
		return ScriptSupplierLake.getJsonEntryFromSupplier(blockPos.get());
	}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		ScriptSupplierLake.loadSupplierOrWarn(element, BlockPos.class, this, res -> blockPos.set(res), "FlooredVec3d.vec3d");
	}
	
	@Override public @NotNull List<? extends IScript> getSubScripts() {return List.of(blockPos.get());}
}
