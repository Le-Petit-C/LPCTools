package lpctools.lpcfymasaapi;

import lpctools.lpcfymasaapi.interfaces.IUnregistrableRegistry;
import lpctools.lpcfymasaapi.interfaces.IterableEx;
import net.fabricmc.fabric.api.event.Event;

import java.lang.classfile.ClassFile;
import java.lang.classfile.Label;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.function.Function;

import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.CD_boolean;
import static java.lang.constant.ConstantDescs.CD_void;

public class UnregistrableRegistry<T> extends UnregistrableRegistryBase<T, T> implements IUnregistrableRegistry<T> {
    public UnregistrableRegistry(Function<IterableEx<T>, T> runner){
	    super(runner);
    }
    public UnregistrableRegistry(Function<IterableEx<T>, T> runner, Event<T> autoRegisterEvent){
        super(runner, autoRegisterEvent);
    }

    private static final HashMap<Class<?>, Function<? extends IterableEx<?>, ?>> FAN_OUT_2_CACHE = new HashMap<>();

    // Iterator / Iterable 的常量描述符
    private static final ClassDesc CD_Iterable = ClassDesc.of("java.lang.Iterable");
    private static final ClassDesc CD_Iterator = ClassDesc.of("java.util.Iterator");

    public static <T> UnregistrableRegistry<T> fanOut(Class<T> type) {
        return new UnregistrableRegistry<>(fanOutFunction(type));
    }

    public static <T> UnregistrableRegistry<T> fanOut(Class<T> type, Event<T> autoRegisterEvent) {
        return new UnregistrableRegistry<>(fanOutFunction(type), autoRegisterEvent);
    }

    @SuppressWarnings("unchecked")
    public static synchronized <T> Function<IterableEx<T>, T> fanOutFunction(Class<T> type) {
        // 缓存命中
        if (FAN_OUT_2_CACHE.containsKey(type)) return (Function<IterableEx<T>, T>) FAN_OUT_2_CACHE.get(type);

        Method sam = findSingleAbstractMethod(type);
        Class<?> returnType = sam.getReturnType();
        if(returnType != void.class) throw new IllegalArgumentException("fanOutFunction requires void return type, but " + type.getName() + " returns " + returnType);
        Class<?>[] paramTypes = sam.getParameterTypes();

        ClassDesc fiDesc = type.describeConstable().orElseThrow();
        // 隐藏类必须和 Lookup 所属类同包 → 作为接口内部类
        ClassDesc selfDesc = fiDesc.nested(UnregistrableRegistry.class.getCanonicalName().replace('.', '_') + "_fanOutLambda");
        ClassDesc callbacksDesc = IterableEx.class.describeConstable().orElseThrow();

        MethodTypeDesc samDesc = MethodTypeDesc.ofDescriptor(
            MethodType.methodType(returnType, paramTypes).descriptorString());

        // iterator() → Iterator, hasNext() → boolean, next() → Object
        MethodTypeDesc iteratorDesc  = MethodTypeDesc.of(CD_Iterator);
        MethodTypeDesc hasNextDesc   = MethodTypeDesc.of(CD_boolean);
        MethodTypeDesc nextDesc      = MethodTypeDesc.of(CD_Object);

        byte[] bytes = ClassFile.of().build(selfDesc, clb -> {
            clb.withFlags(ClassFile.ACC_FINAL | ClassFile.ACC_SYNTHETIC)
                .withInterfaceSymbols(fiDesc)
                .withField("callbacks", callbacksDesc,
                    ClassFile.ACC_PRIVATE | ClassFile.ACC_FINAL);

            // ── 构造器: (IterableEx)V ──
            clb.withMethodBody("<init>",
                MethodTypeDesc.of(CD_void, callbacksDesc),
                ClassFile.ACC_PUBLIC, cob -> {
                    // super()
                    cob.aload(0).invokespecial(CD_Object, "<init>",
                            MethodTypeDesc.of(CD_void))
                        // this.callbacks = arg
                        .aload(0).aload(1)
                        .putfield(selfDesc, "callbacks", callbacksDesc)
                        .return_();
                });

            // ── SAM 方法体: for (T cb : this.callbacks) cb.xxx(args) ──
            clb.withMethodBody(sam.getName(), samDesc,
                ClassFile.ACC_PUBLIC, cob -> {
                    // this.callbacks.iterator()
                    cob.aload(0)
                        .getfield(selfDesc, "callbacks", callbacksDesc)
                        .invokeinterface(CD_Iterable, "iterator", iteratorDesc);

                    int iterSlot = cob.allocateLocal(TypeKind.REFERENCE);
                    cob.astore(iterSlot);

                    Label loopStart = cob.newLabel();
                    Label loopEnd   = cob.newLabel();

                    // ──── loop header ────
                    cob.labelBinding(loopStart);

                    // if (!iterator.hasNext()) break
                    cob.aload(iterSlot)
                        .invokeinterface(CD_Iterator, "hasNext", hasNextDesc);
                    cob.ifeq(loopEnd);

                    // T callback = (T) iterator.next()
                    cob.aload(iterSlot)
                        .invokeinterface(CD_Iterator, "next", nextDesc)
                        .checkcast(fiDesc);

                    // 加载 SAM 的各个参数（slot 0 = this）
                    for (int i = 0; i < paramTypes.length; i++) {
                        Class<?> pType = paramTypes[i];
                        int slot = cob.parameterSlot(i);
                        cob.loadLocal(TypeKind.from(pType), slot);
                    }

                    // callback.sam(args)
                    cob.invokeinterface(fiDesc, sam.getName(), samDesc);

					// continue loop
                    cob.goto_(loopStart);

                    // ──── loop end ────
                    cob.labelBinding(loopEnd);
                    cob.return_();
                });
        });

        // 定义隐藏类 + 工厂函数
        try {
            // 获取对目标接口有访问权的 Lookup（否则隐藏类拿不到接口定义）
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(type, MethodHandles.lookup());
            lookup = lookup.defineHiddenClass(bytes, true);
            Class<?> genClass = lookup.lookupClass();
            MethodHandle ctor = lookup.findConstructor(genClass,
                MethodType.methodType(void.class, IterableEx.class));

            Function<IterableEx<T>, T> func = callbacks -> {
                try {
                    return (T) ctor.invoke(callbacks);
                } catch (RuntimeException | Error e) {
                    throw e;
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            };
            FAN_OUT_2_CACHE.put(type, func);
            return func;
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException("Failed to generate fan-out for " + type, e);
        }
    }

    private static <T> Method findSingleAbstractMethod(Class<T> type) {
        if (!type.isInterface()) {
            throw new IllegalArgumentException(type + " is not an interface");
        }
        Method found = null;
        for (Method m : type.getMethods()) {
            if (m.isDefault()) continue;                      // 跳过 default 方法
            if (Modifier.isStatic(m.getModifiers())) continue; // 跳过 static 方法
            if (m.getDeclaringClass() == Object.class) continue;                // 跳过 Object 方法

            if (found != null) {
                throw new IllegalArgumentException(
                    type + " is not a @FunctionalInterface: has multiple abstract methods ("
                        + found.getName() + " and " + m.getName() + ")");
            }
            found = m;
        }
        if (found == null) {
            throw new IllegalArgumentException(type + " has no abstract method");
        }
        return found;
    }
}
