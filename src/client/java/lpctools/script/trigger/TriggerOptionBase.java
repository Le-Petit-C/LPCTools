package lpctools.script.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import lpctools.script.AbstractScript;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

abstract class TriggerOptionBase extends AbstractScript implements TriggerOption {
	protected final TriggerOptionFactory factory;
	
	protected TriggerOptionBase(ScriptTrigger trigger, TriggerOptionFactory factory) {
		super(trigger);
		this.factory = factory;
	}
	
	@Override
	public @Nullable JsonElement getAsJsonElement() {return JsonNull.INSTANCE;}
	
	@Override
	public void setValueFromJsonElement(@Nullable JsonElement element) {}
	
	@Override
	public TriggerOptionFactory getFactory() {return factory;}
	
	@Override
	public @Nullable String getName() {return Text.translatable("lpctools.script.trigger." + getFactory().getKey()).getString();}
	
	@Override public @Nullable ScriptTrigger getParent() {
		return (ScriptTrigger)super.getParent();
	}
}
