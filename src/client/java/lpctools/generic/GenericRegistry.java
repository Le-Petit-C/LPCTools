package lpctools.generic;

import lpctools.lpcfymasaapi.UnregistrableRegistry;

public class GenericRegistry {
    public static final UnregistrableRegistry<SpawnConditionChanged> SPAWN_CONDITION_CHANGED = UnregistrableRegistry.fanOut(SpawnConditionChanged.class);
    public interface SpawnConditionChanged{ void onSpawnConditionChanged();}
}
