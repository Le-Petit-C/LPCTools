package lpctools.script.suppliers.ControlFlowIssue;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import static lpctools.util.DataUtils.clientMessage;

public class ClientMessage extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IControlFlowIssueSupplier {
	protected final SupplierStorage<String> message = ofStorage(String.class,
		Text.translatable("lpctools.script.suppliers.ControlFlowIssue.clientMessage.subSuppliers.message.name"), "message");
	protected final SupplierStorage<Boolean> overlay = ofStorage(Boolean.class,
		Text.translatable("lpctools.script.suppliers.ControlFlowIssue.clientMessage.subSuppliers.overlay.name"), "overlay");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(message, overlay);
	
	public ClientMessage(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<ControlFlowIssue>
	compileNotNull(CompileEnvironment environment) {
		var compiledStringSupplier = message.get().compileCheckedNotNull(environment);
		var compiledBooleanSupplier = overlay.get().compileCheckedNotNull(environment);
		return map->{
			clientMessage(Text.literal(compiledStringSupplier.scriptApply(map)), compiledBooleanSupplier.scriptApply(map));
			return ControlFlowIssue.NO_ISSUE;
		};
	}
}
