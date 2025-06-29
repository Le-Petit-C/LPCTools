package lpctools.util.javaex;

public interface AutoCloseableNoExcept extends AutoCloseable{
    @Override void close();
}
