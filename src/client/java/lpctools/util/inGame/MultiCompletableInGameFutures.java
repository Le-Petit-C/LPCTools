package lpctools.util.inGame;

import java.util.concurrent.ConcurrentSkipListSet;

public class MultiCompletableInGameFutures {
	final ConcurrentSkipListSet<InGameFuture<?>> futures = new ConcurrentSkipListSet<>();

	public <T> InGameFuture<T> createCompleted(T val) {
		var res = new InGameFuture<T>(this);
		res.complete(val);
		return res;
	}

	public InGameFuture<Void> createCompleted() {
		var res = new InGameFuture<Void>(this);
		res.complete(null);
		return res;
	}

	public void cancelAll() {
		InGameFuture<?>[] futures = this.futures.toArray(new InGameFuture<?>[0]);
		for(var future : futures) future.cancel(false);
	}
}
