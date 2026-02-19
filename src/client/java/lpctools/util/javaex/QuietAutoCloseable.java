package lpctools.util.javaex;

public interface QuietAutoCloseable extends AutoCloseable {
	@Override void close();
	static void closeIfNotNull(QuietAutoCloseable closeable) {
		if(closeable != null) closeable.close();
	}
}
