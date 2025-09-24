package lpctools.script.editScreen;

import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.util.position.Vec2i;
import lpctools.script.IScript;
import lpctools.script.ISubScriptMutable;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class ScriptExtraOptionButton extends ButtonGeneric {
	private final ScriptEditScreen scriptEditScreen;
	private boolean click;
	private @Nullable Vec2i holdingPos;
	private long clickTime;
	private final @NotNull IScript script;
	@Nullable ISubScriptMutable parent;
	int index;
	
	public ScriptExtraOptionButton(ScriptEditScreen scriptEditScreen, @NotNull IScript script) {
		super(0, 0, 20, 20, "≡");
		this.scriptEditScreen = scriptEditScreen;
		this.script = script;
		this.parent = null;
		this.index = -1;
	}
	
	@Override public void onMouseReleasedImpl(int mouseX, int mouseY, int mouseButton) {
		if (holdingPos != null && mouseButton == 0) {
			if (click) {
				scriptEditScreen.extendedExtra = this;
				this.mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
				scriptEditScreen.markNeedUpdate();
			} else if (scriptEditScreen.holdingMutableScript == parent)
				scriptEditScreen.holdingMutableScript = null;
		}
		super.onMouseReleasedImpl(mouseX, mouseY, mouseButton);
	}
	
	@Override
	public void render(DrawContext drawContext, int mouseX, int mouseY, boolean selected) {
		super.render(drawContext, mouseX, mouseY, selected);
		if (holdingPos != null && click && (isMouseOver() || System.currentTimeMillis() - clickTime >= 250)) {
			click = false;
			scriptEditScreen.holdingMutableScript = parent;
			scriptEditScreen.holdingSubScriptIndex = index;
		}
	}
	
	@Override
	protected boolean onMouseClickedImpl(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 0) {
			holdingPos = new Vec2i(mouseX - getX(), mouseY - getY());
			clickTime = System.currentTimeMillis();
			click = true;
		}
		if (this.actionListener != null)
			this.actionListener.actionPerformedWithButton(this, mouseButton);
		return true;
	}
}
