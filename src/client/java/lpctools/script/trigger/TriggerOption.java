package lpctools.script.trigger;

import lpctools.script.IScript;

import java.util.HashMap;

public interface TriggerOption extends IScript, AutoCloseable {
	interface TriggerOptionFactory{
		TriggerOption allocateOption(ScriptTrigger trigger);
		boolean allowMulti();
		String getKey();
	}
	void registerScript(boolean b);
	TriggerOptionFactory getFactory();
	@Override default void close() {registerScript(false);}
	
	HashMap<String, TriggerOptionFactory> triggerOptionFactories = initTriggerOptionFactories();
	private static HashMap<String, TriggerOptionFactory> initTriggerOptionFactories(){
		HashMap<String, TriggerOptionFactory> res = new HashMap<>();
		putFactory(res, new HotkeyOption.HotkeyOptionFactory());
		return res;
	}
	private static void putFactory(HashMap<String, TriggerOptionFactory> map, TriggerOptionFactory factory){
		map.put(factory.getKey(), factory);
	}
}

