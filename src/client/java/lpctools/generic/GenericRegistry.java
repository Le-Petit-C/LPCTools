package lpctools.generic;

import lpctools.lpcfymasaapi.UnregistrableRegistry;

public class GenericRegistry {
    public static final UnregistrableRegistry<SpawnConditionChanged> SPAWN_CONDITION_CHANGED = new UnregistrableRegistry<>(
        callbacks->()->callbacks.forEach(SpawnConditionChanged::onSpawnConditionChanged));
    public interface SpawnConditionChanged{ void onSpawnConditionChanged();}
}
