package lpctools.util;

import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * 携带 Minecraft {@link Component} 的通用运行时异常。
 * 用于把"可本地化/可显示的失败原因"随异常一起传播，
 * 消费方可通过 {@link #getComponent()} 直接显示，而不丢失翻译与格式信息。
 */
public class ComponentException extends RuntimeException {
	private final @Nullable Component component;

	public ComponentException(@Nullable Component component) {
		super(component != null ? component.getString() : null);
		this.component = component;
	}
	public ComponentException(@Nullable String message) {
		this(message == null ? null : Component.literal(message));
	}
	public ComponentException() { this((Component) null); }

	/** 失败原因组件（可能为 null，此时仅能用 {@link #getMessage()} 兜底）。 */
	public @Nullable Component getComponent() { return component; }
}
