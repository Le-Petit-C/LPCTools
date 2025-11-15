package lpctools.script.trigger;

import com.google.common.collect.ImmutableMap;
import lpctools.script.IScript;
import net.minecraft.text.Text;

import java.util.function.BiFunction;

public interface TriggerOption extends IScript, AutoCloseable {
	record TriggerOptionFactory(BiFunction<ScriptTrigger, TriggerOptionFactory, TriggerOption> allocator, boolean allowMulti, String key, Text name, Text comment){
		public TriggerOption allocate(ScriptTrigger trigger){return allocator.apply(trigger, this);}
	}
	void registerScript(boolean b);
	TriggerOptionFactory getFactory();
	@Override default void close() {registerScript(false);}
	
	ImmutableMap<String, TriggerOptionFactory> triggerOptionFactories = new FactoriesBuilder()
		.putFactory(HotkeyOption::new, 			true, 	"hotkey")
		.putFactory(ClientTickStartOption::new, false, 	"clientTickStart")
		.putFactory(ClientTickEndOption::new, 	false, 	"clientTickEnd")
		.build();
	
	class FactoriesBuilder{
		private final ImmutableMap.Builder<String, TriggerOptionFactory> builder = new ImmutableMap.Builder<>();
		private FactoriesBuilder(){}
		private FactoriesBuilder putFactory(BiFunction<ScriptTrigger, TriggerOptionFactory, TriggerOption> allocator, boolean allowMulti, String key){
			TriggerOptionFactory factory = new TriggerOptionFactory(allocator, allowMulti, key,
				Text.translatable("lpctools.script.trigger." + key + ".name"),
				Text.translatable("lpctools.script.trigger." + key + ".comment"));
			builder.put(factory.key, factory);
			return this;
		}
		private ImmutableMap<String, TriggerOptionFactory> build(){return builder.build();}
	}
}

