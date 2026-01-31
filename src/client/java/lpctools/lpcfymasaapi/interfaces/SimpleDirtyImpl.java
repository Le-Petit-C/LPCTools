package lpctools.lpcfymasaapi.interfaces;

import fi.dy.masa.malilib.config.IConfigBase;

public interface SimpleDirtyImpl extends IConfigBase, ILPCConfigNotifiable {
	interface IDirtyState {
		boolean isDirty();
		void setDirty(boolean b);
	}
	class DirtyState implements IDirtyState {
		public boolean isDirty = false;
		@Override public boolean isDirty() { return isDirty; }
		@Override public void setDirty(boolean b) { isDirty = b; }
	}
	
	// 获取dirty状态
	IDirtyState getDirty();
	
	default @Override boolean isDirty() { return getDirty().isDirty(); }
	@SuppressWarnings("unused") default void setDirty(boolean b) { getDirty().setDirty(b); }
	default @Override void markDirty() { getDirty().setDirty(true); }
	default @Override void markClean() { getDirty().setDirty(false); }
	default @Override void checkIfClean() {
		if (isDirty()) {
			markClean();
			onValueChanged();
		}
	}
}
