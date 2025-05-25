package lpctools.generic;

import lpctools.lpcfymasaapi.utils.RegistryEx;

public class GenericRegistry {
    public static void runSpawnConditionChanged(){SPAWN_CONDITION_CHANGED.run(SpawnConditionChanged::onSpawnConditionChanged);}
    public static RegistryEx<SpawnConditionChanged> SPAWN_CONDITION_CHANGED = new RegistryEx<>();
    public interface SpawnConditionChanged{ void onSpawnConditionChanged();}
}
