package lpctools.script.suppliers.ControlFlowIssue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.editScreen.WidthAutoAdjustButtonGeneric;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.Integer.ConstantInteger;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class ClickSlot extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IControlFlowIssueSupplier {
	protected final SupplierStorage<Integer> slotId = ofStorage(Integer.class, new ConstantInteger(this),
		Text.translatable("lpctools.script.suppliers.ControlFlowIssue.clickSlot.subSuppliers.slotId.name"), "slotId");
	protected final SupplierStorage<Integer> button = ofStorage(Integer.class, new ConstantInteger(this),
		Text.translatable("lpctools.script.suppliers.ControlFlowIssue.clickSlot.subSuppliers.button.name"), "button");
	protected SlotActionType slotActionType = SlotActionType.PICKUP;
	protected @Nullable WidthAutoAdjustButtonGeneric actionTypeButton;
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(slotId, button);
	
	public static final String slotActionTypeJsonKey = "slotActionType";
	
	public ClickSlot(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override protected ArrayList<Object> buildWidgets(ArrayList<Object> res) {
		super.buildWidgets(res);
		if(actionTypeButton == null){
			var finalButton = new WidthAutoAdjustButtonGeneric(
				getDisplayWidget(), 0, 0, 20, slotActionType.name(), null);
			finalButton.setActionListener((button, mouseButton)->{
				int idx = slotActionType.getIndex();
				var arr = SlotActionType.values();
				if(mouseButton == 0) slotActionType = (idx >= arr.length - 1) ? arr[0] : arr[idx + 1];
				else slotActionType = (idx <= 0) ? arr[arr.length - 1] : arr[idx - 1];
				finalButton.setDisplayString(slotActionType.name());
			});
			actionTypeButton = finalButton;
		}
		res.add(actionTypeButton);
		return res;
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {
		JsonObject res = getASWTDSSAsJsonElement(this);
		res.addProperty(slotActionTypeJsonKey, slotActionType.name());
		return res;
	}
	
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element == null) return;
		if (!(element instanceof JsonObject object)) {
			warnFailedLoadingConfig(className, element);
			return;
		}
		if(object.get(slotActionTypeJsonKey) instanceof JsonElement element1){
			if(element1 instanceof JsonPrimitive primitive){
				try {
					slotActionType = SlotActionType.valueOf(SlotActionType.class, primitive.getAsString());
				} catch (IllegalArgumentException ignored){
					warnFailedLoadingConfig("ClickSlot.slotActionType", primitive);
				}
			}
			else warnFailedLoadingConfig("ClickSlot.slotActionType", element1);
		}
		setASWTDSSValueFromJsonObject(this, object);
	}
	
	@Override public @NotNull ScriptNotNullSupplier<ControlFlowIssue>
	compileNotNull(CompileEnvironment environment) {
		var compiledSlotIdSupplier = slotId.get().compileCheckedNotNull(environment);
		var compiledButtonSupplier = button.get().compileCheckedNotNull(environment);
		return map->{
			var client = MinecraftClient.getInstance();
			var player = client.player;
			var itm = client.interactionManager;
			if (itm != null && player != null)
				itm.clickSlot(player.currentScreenHandler.syncId,
					compiledSlotIdSupplier.scriptApply(map),
					compiledButtonSupplier.scriptApply(map),
					slotActionType,
					player);
			return ControlFlowIssue.NO_ISSUE;
		};
	}
}
