package lpctools.lpcfymasaapi.render;

public final class BufferState {
	public final BufferType type;
	public final BufferUsage usage;
	private BufferState(BufferType type, BufferUsage usage) {
		this.type = type;
		this.usage = usage;
	}
	public int ordinal() { return getIndex(type, usage); }
	public static BufferState fromOrdinal(int ordinal) { return states[ordinal]; }
	public static BufferState fromOptions(BufferType type, BufferUsage usage) { return states[getIndex(type, usage)]; }

	private static final BufferState[] states = new BufferState[BufferType.values().length * BufferUsage.values().length];
	private static int getIndex(BufferType type, BufferUsage usage) {
		return type.ordinal() * BufferUsage.values().length + usage.ordinal();
	}
	static {
		for(var type : BufferType.values()) {
			for(var usage : BufferUsage.values()) {
				BufferState state = new BufferState(type, usage);
				states[state.ordinal()] = state;
			}
		}
	}
}
