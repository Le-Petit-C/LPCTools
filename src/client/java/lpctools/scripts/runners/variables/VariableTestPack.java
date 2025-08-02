package lpctools.scripts.runners.variables;

import fi.dy.masa.litematica.util.ToBooleanFunction;

public record VariableTestPack(ToBooleanFunction<Variable<?>> instanceofTest, String targetTypeDescription) {}
