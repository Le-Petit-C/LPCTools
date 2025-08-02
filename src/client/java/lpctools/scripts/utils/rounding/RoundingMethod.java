package lpctools.scripts.utils.rounding;

import lpctools.scripts.IScriptBase;

public enum RoundingMethod {
	FLOOR("floor") {
		@Override public int round(double value) {
			return (int)Math.floor(value);
		}
	},
	CEILING("ceiling") {
		@Override public int round(double value) {
			return (int)Math.ceil(value);
		}
	},
	TOWARDS_ZERO("towards_zero") {
		@Override public int round(double value) {
			return (int)value;
		}
	},
    HALF_UP("half_up") {
		@Override public int round(double value) {
			return (int)Math.round(value);
		}
	};
	public final String fullKey;
	RoundingMethod(String key){fullKey = IScriptBase.fullPrefix + "roundingMethod." + key;}
	public abstract int round(double value);
}
