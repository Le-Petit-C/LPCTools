package lpctools.util.javaex;

public interface QuietAutoCloseable extends AutoCloseable {
	@Override void close();
	static void closeIfNotNull(QuietAutoCloseable closeable) { if(closeable != null) closeable.close(); }
	static <T extends AutoCloseable> T close(T v) {
		if(v != null) {
			try {
				v.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}
}
