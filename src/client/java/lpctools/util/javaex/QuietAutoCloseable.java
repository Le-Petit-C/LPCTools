package lpctools.util.javaex;

public interface QuietAutoCloseable extends AutoCloseable {
	@Override void close();
}
