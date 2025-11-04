package lpctools.script.suppliers.BlockPos;

import com.google.gson.JsonElement;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScript;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.ScriptSupplierLake;
import lpctools.script.suppliers.Vec3d.ConstantVec3d;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FlooredVec3d extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IBlockPosSupplier {
	protected final SupplierStorage<Vec3d> vec3d = ofStorage(new ConstantVec3d(this),
		Text.translatable("lpctools.script.suppliers.BlockPos.flooredVec3d.subSuppliers.vec3d.name"));
	protected final SubSupplierEntry<?>[] subSuppliers = subSupplierBuilder()
		.addEntry(Vec3d.class, vec3d)
		.build();
	
	public FlooredVec3d(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SubSupplierEntry<?>[] getSubSuppliers(){ return subSuppliers; }
	
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, BlockPos>
	compile(CompileEnvironment variableMap) {
		var compiledEntitySupplier = vec3d.get().compile(variableMap);
		return map->{
			Vec3d vec = compiledEntitySupplier.scriptApply(map);
			if(vec == null) throw ScriptRuntimeException.nullPointer(this);
			return BlockPos.ofFloored(vec);
		};
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {
		return ScriptSupplierLake.getJsonEntryFromSupplier(vec3d.get());
	}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		ScriptSupplierLake.loadSupplierOrWarn(element, Vec3d.class, this, res -> vec3d.set(res), "FlooredVec3d.vec3d");
	}
	
	@Override public @NotNull List<? extends IScript> getSubScripts() {return List.of(vec3d.get());}
}
