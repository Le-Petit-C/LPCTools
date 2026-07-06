import lpctools.lpcfymasaapi.UnregistrableRegistry;
import lpctools.lpcfymasaapi.interfaces.IterableEx;

interface Callback {
	void callback(String str);
}

static int result = 0;
static int expected_times = 0;

void main() {
	LinkedHashSet<Callback> _callbacks = new LinkedHashSet<>();
	IterableEx<Callback> callbacksIterableEx = _callbacks::iterator;
	Function<IterableEx<Callback>, Callback> lambda, fanout;
	lambda = callbacks -> str -> callbacks.forEach(callback -> callback.callback(str));
	fanout = UnregistrableRegistry.fanOutFunction(Callback.class);
	Callback lambdaCallback = lambda.apply(callbacksIterableEx);
	Callback fanoutCallback = fanout.apply(callbacksIterableEx);
	String testStr = "test";
	TestMethod lambdaMethod = new TestMethod(() -> lambdaCallback.callback(testStr), "lambda");
	TestMethod fanoutMethod = new TestMethod(() -> fanoutCallback.callback(testStr), "fanout");
	TestMethod[] methods = {lambdaMethod, fanoutMethod};
	// 填充callbacks
	_callbacks.add(str -> result += str.length());
	_callbacks.add(str -> result += str.lastIndexOf('s'));
	// 预热
	int maxTimes = 65536 * 16;
	test(methods, maxTimes, false);
	// 测试
	for (int i = 1024; i <= maxTimes; i *= 2) {
		test(methods, i, true);
		IO.println();
	}
	IO.println("result: " + result);
	IO.println("expected result: " + expected_times * (testStr.length() + testStr.lastIndexOf('s')));
}

record TestMethod(Runnable method, String name) {
}

static void test(TestMethod[] methods, int times, boolean output) {
	for (TestMethod method : methods) test(method, times, output);
	expected_times += methods.length * times;
}

static void test(TestMethod method, int times, boolean output) {
	System.gc();
	try {
		Thread.sleep(100);  // 等 GC 完成
	} catch (InterruptedException _) {
	}
	long startTime = System.nanoTime();
	for (int i = 0; i < times; i++) method.method.run();
	long endTime = System.nanoTime();
	if (output) IO.println((endTime - startTime) + " ns for " + times + " iterations, method " + method.name);
}
