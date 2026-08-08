package lpctools.util;

import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.ThrowingComponent;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * 携带 Minecraft {@link Component} 的通用运行时异常。
 * 用于把"可本地化/可显示的失败原因"随异常一起传播，
 * 消费方可通过 {@link #getComponent()} 直接显示，而不丢失翻译与格式信息。
 */
public class RuntimeComponentException extends RuntimeException {
	private final @Nullable Component component;

	public RuntimeComponentException(@Nullable Component component) {
		super(component != null ? component.getString() : null);
		this.component = component;
	}
	public RuntimeComponentException(@Nullable String message) {
		this(message == null ? null : Component.literal(message));
	}
	public RuntimeComponentException() { this((Component) null); }

	/** 失败原因组件（可能为 null，此时仅能用 {@link #getMessage()} 兜底）。 */
	public @Nullable Component getComponent() { return component; }

	public static @NonNull MutableComponent mutableExceptionComponent(Throwable throwable) {
		if(throwable instanceof RuntimeComponentException e)
			return e.getComponent() instanceof Component component ? component.copy() : Component.empty();
		else if(throwable instanceof ThrowingComponent e)
			return e.getComponent() instanceof Component component ? component.copy() : Component.empty();
		else if(throwable.getMessage() instanceof String s) return Component.literal(s);
		else return Component.literal(throwable.toString());
	}

	public static @NonNull Component exceptionComponent(Throwable throwable) {
		Component res;
		if(throwable instanceof RuntimeComponentException e) res = e.getComponent();
		else if(throwable instanceof ThrowingComponent e) res = e.getComponent();
		else if(throwable.getMessage() instanceof String s) res = Component.literal(s);
		else res = Component.literal(throwable.toString());
		return res == null ? CommonComponents.EMPTY : res;
	}
}
