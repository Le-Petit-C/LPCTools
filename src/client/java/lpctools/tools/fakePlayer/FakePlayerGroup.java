package lpctools.tools.fakePlayer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.IConfigValue;
import fi.dy.masa.malilib.config.gui.ConfigOptionChangeListenerTextField;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.LeftRight;
import fi.dy.masa.malilib.gui.MaLiLibIcons;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.ConfigButtonKeybind;
import fi.dy.masa.malilib.gui.interfaces.ISliderCallback;
import fi.dy.masa.malilib.gui.widgets.WidgetKeybindSettings;
import fi.dy.masa.malilib.gui.widgets.WidgetSlider;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import lpctools.LPCTools;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.*;
import lpctools.lpcfymasaapi.interfaces.*;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase.ButtonOption;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase.ButtonOptionArrayList;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase.ButtonWeightType;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase.IButtonAllocator;
import lpctools.util.DataUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

// 假批量管理：通过配置假人组，批量操作 carpet 的 /player 命令
public class FakePlayerGroup {
    public static final FPGroupTab FPConfig = new FPGroupTab(LPCTools.page, "fakePlayer");
    static { listStack.push(FPConfig); }
    public static final FPGroupList groups = addConfigEx(l -> new FPGroupList(l, "groups", GroupEntry::new));
    static { listStack.pop(); }

    //------ 假人组选项卡：直接渲染各组条目，不显示最外层"假玩家组"容器行 ------
    public static class FPGroupTab extends LPCConfigList {
        public FPGroupTab(@NotNull ILPCConfigReadable parent, @NotNull String nameKey){
            super(parent, nameKey);
        }
        @Override public ArrayList<GuiConfigsBase.ConfigOptionWrapper> buildConfigWrappers(ToIntFunction<String> getStringWidth, ArrayList<GuiConfigsBase.ConfigOptionWrapper> wrapperList) {
            int indent = 0;
            for (ILPCConfig config : getConfigs()) {
                if (config == groups) {
                    //跳过 groups 容器自身那一行，直接渲染其内部条目和添加按钮
                    groups.buildConfigWrappers(getStringWidth, wrapperList);
                    indent = Math.max(indent, groups.getAlignedIndent());
                } else {
                    config.refreshName();
                    wrapperList.add(new GuiConfigsBase.ConfigOptionWrapper(config));
                    if (config instanceof ILPCConfigReadable list)
                        list.buildConfigWrappers(getStringWidth, wrapperList);
                    indent = Math.max(indent, getStringWidth.applyAsInt(config.getConfigGuiDisplayName()));
                }
            }
            setAlignedIndent(indent);
            return wrapperList;
        }
    }

    //------ 假人组列表容器：对齐层级透明(不额外缩进)，始终展开 ------
    public static class FPGroupList extends FPListConfig<GroupEntry> {
        public FPGroupList(@NotNull ILPCConfigReadable parent, @NotNull String nameKey,
                           @NotNull Function<? super ConfigListConfig<GroupEntry>, ? extends GroupEntry> supplier) {
            super(parent, nameKey, supplier);
            expanded = true;
        }
        //透明对齐：让组条目与选项卡顶层条目处于同一缩进层级
        @Override public int getAlignLevel() { return getParent().getAlignLevel(); }
        @Override public ArrayList<GuiConfigsBase.ConfigOptionWrapper> buildConfigWrappers(ToIntFunction<String> getStringWidth, ArrayList<GuiConfigsBase.ConfigOptionWrapper> wrapperList) {
            expanded = true;
            return super.buildConfigWrappers(getStringWidth, wrapperList);
        }
        @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
            UpdateTodo todo = super.setValueFromJsonElementEx(element);
            expanded = true;
            return todo;
        }
    }

    // 批量执行：对组内所有假玩家执行行为中的命令（根据过滤器跳过特定玩家）
    public static void runBatch(@NotNull GroupEntry group, @NotNull BehaviorEntry behavior) {
        Minecraft client = Minecraft.getInstance();
        ArrayList<String> names = new ArrayList<>();
        group.playersList.iterateConfigs().forEach(entry -> entry.expandNames(names));
        names.removeIf(name -> !behavior.filter.shouldInclude(name));
        if (names.isEmpty()) {
            DataUtils.clientMessage(Component.translatable("lpctools.configs.fakePlayer.noPlayers"), true);
            return;
        }
        String action = behavior.action.getStringValue();
        if (action.isEmpty()) {
            DataUtils.clientMessage(Component.translatable("lpctools.configs.fakePlayer.noCommand"), true);
            return;
        }
        int delay = behavior.delay.getIntegerValue();
        DataUtils.clientMessage(Component.translatable("lpctools.configs.fakePlayer.batchStart", names.size(), action), false);
        new Thread(() -> {
            for (String name : names) {
                client.execute(() -> {
                    LocalPlayer p = client.player;
                    if (p != null && p.connection != null)
                        p.connection.sendCommand("player " + name + " " + action);
                });
                if (delay > 0) {
                    try { Thread.sleep(delay); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                }
            }
        }, "LPCTools-FakePlayerBatch").start();
    }

    //------ 工具方法：带提示的按钮分配器(翻译不存在时不显示) ------
    static List<String> tooltipLines(String key) {
        String translated = Component.translatable(key).getString();
        if (translated.isEmpty() || translated.equals(key)) return new ArrayList<>();
        return Arrays.asList(translated.split("\n"));
    }
    //带悬浮提示的文本按钮
    static IButtonAllocator tooltipButtonAllocator(String hoverKey) {
        return (x, y, w, h, str, listener, consumer, reset) -> {
            ButtonGeneric btn = new ButtonGeneric(x, y, w, h, str);
            List<String> lines = tooltipLines(hoverKey);
            if (!lines.isEmpty()) btn.setHoverStrings(lines);
            consumer.addButton(btn, listener);
        };
    }
    //带悬浮提示的图标按钮
    static IButtonAllocator tooltipIconAllocator(MaLiLibIcons icon, LeftRight align, String hoverKey) {
        return (x, y, w, h, str, listener, consumer, reset) -> {
            ButtonGeneric btn = new ButtonGeneric(x, y, w, h, str, icon);
            btn.setIconAlignment(align);
            List<String> lines = tooltipLines(hoverKey);
            if (!lines.isEmpty()) btn.setHoverStrings(lines);
            consumer.addButton(btn, listener);
        };
    }
    //带悬浮提示的文本框
    static ButtonOption tooltipTextField(float weight, IConfigValue config, String hoverKey) {
        return new ButtonOption(weight, null, null, (x, y, w, h, str, listener, consumer, reset) -> {
            GuiTextFieldGeneric field = new GuiTextFieldGeneric(x + 2, y + 1, w - 4, h - 3, consumer.getTextRenderer()) {
                @Override public void setFocused(boolean focused) {
                    super.setFocused(focused);
                    if (!focused) {
                        try { config.setValueFromString(getValue()); } catch (NumberFormatException ignored) {}
                        setValue(config.getStringValue());
                    }
                }
            };
            field.setMaxLength(consumer.getMaxTextFieldTextLength());
            field.setValue(config.getStringValue());
            String translated = Component.translatable(hoverKey).getString();
            if (!translated.isEmpty() && !translated.equals(hoverKey))
                field.setHoverTooltip(hoverKey);
            ConfigOptionChangeListenerTextField listenerChange = new ConfigOptionChangeListenerTextField(config, field, reset) {
                @Override public boolean onTextChange(GuiTextFieldGeneric textField) {
                    if (buttonReset != null) buttonReset.setEnabled(this.config.isModified(this.textField.getValue()));
                    return false;
                }
            };
            consumer.addExtraTextField(field, listenerChange);
        });
    }
    //带悬浮提示的快捷键按钮 + 设置按钮
    static ButtonOption tooltipKeybind(float weight, IHotkey hotkey, String hoverKey) {
        return new ButtonOption(weight, null, null, (x, y, w, h, str, listener, consumer, reset) -> {
            ConfigButtonKeybind btn = new ConfigButtonKeybind(x, y, w - h - 2, h, hotkey.getKeybind(), consumer.getKeybindHost());
            List<String> lines = tooltipLines(hoverKey);
            if (!lines.isEmpty()) btn.setHoverStrings(lines);
            consumer.addButton(btn, listener);
            consumer.addWidget(new WidgetKeybindSettings(x + w - h, y, h, h, hotkey.getKeybind(), hotkey.getName(), consumer.getWidgetListConfigOptionsBase(), consumer.getKeybindHost().getDialogHandler()));
        });
    }

    //------ 带提示的整型配置选项(滑动条/文本框 + 切换图标) ------
    static void addTooltipIntegerOptions(ButtonOptionArrayList res, UniqueIntegerConfig config, String hoverKey, String toggleHoverKey) {
        if (config.allowSlider && config.useSlider) {
            res.add(new ButtonOption(1, null, null, (x, y, w, h, str, listener, consumer, reset) -> {
                ISliderCallback cb = new ISliderCallback() {
                    @Override public int getMaxSteps() { return config.maxInteger - config.minInteger > 0 ? Integer.MAX_VALUE : 0; }
                    @Override public double getValueRelative() { return ((double)config.intValue - (double)config.minInteger) / ((double)config.maxInteger - (double)config.minInteger); }
                    @Override public void setValueRelative(double v) { config.setIntegerValue((int)Math.round(((double)config.maxInteger - (double)config.minInteger) * v + config.minInteger)); }
                    @Override public String getFormattedDisplayValue() { return String.valueOf(config.intValue); }
                };
                consumer.addWidget(new WidgetSlider(x, y, w, h, cb));
            }));
        } else {
            res.add(tooltipTextField(1, config, hoverKey));
        }
        if (config.allowSlider) {
            res.add(new ButtonOption(-1, (b, mb) -> { config.useSlider = !config.useSlider; config.getPage().markNeedUpdate(); }, null,
                tooltipIconAllocator(config.useSlider ? MaLiLibIcons.BTN_TXTFIELD : MaLiLibIcons.BTN_SLIDER, LeftRight.CENTER, toggleHoverKey)));
        }
    }

    //------ 可展开列表：添加按钮在条目上方，条目自带操作按钮(移动/删除) ------
    public static class FPListConfig<T extends ILPCUniqueConfigBase> extends ConfigListConfig<T> {
        AddButton addButton;
        public FPListConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey,
                            @NotNull Function<? super ConfigListConfig<T>, ? extends T> supplier) {
            super(parent, nameKey, supplier, null);
            hideOperationButton = false;
            addButton = new AddButton(this, () -> allocateAndAddConfig());
        }
        @Override public void getButtonOptions(ButtonOptionArrayList res) {
            res.add(new ButtonOption(-1, (b, mb) -> setExpanded(!expanded), null,
                tooltipIconAllocator(expanded ? MaLiLibIcons.ARROW_UP : MaLiLibIcons.ARROW_DOWN, LeftRight.CENTER, "lpctools.configs.fakePlayer.toggleExpand")));
            res.add(new ButtonOption(1, (b, mb) -> setExpanded(!expanded), () -> getFullNameTranslationKey(), tooltipButtonAllocator("lpctools.configs.fakePlayer.listName.hover")));
        }
        @Override public ArrayList<GuiConfigsBase.ConfigOptionWrapper> buildConfigWrappers(ToIntFunction<String> getStringWidth, ArrayList<GuiConfigsBase.ConfigOptionWrapper> wrapperList) {
            int indent = 0;
            if (expanded) {
                addButton.refreshName();
                wrapperList.add(new GuiConfigsBase.ConfigOptionWrapper(addButton));
                //将添加按钮的标签宽度计入缩进，为后面的交互框预留位置
                indent = Math.max(indent, getStringWidth.applyAsInt(addButton.getConfigGuiDisplayName()));
            }
            for (ILPCConfig config : getConfigs()) {
                config.refreshName();
                if (expanded) {
                    wrapperList.add(new GuiConfigsBase.ConfigOptionWrapper(config));
                    if (config instanceof ILPCConfigReadable list)
                        list.buildConfigWrappers(getStringWidth, wrapperList);
                }
                indent = Math.max(indent, getStringWidth.applyAsInt(config.getConfigGuiDisplayName()));
            }
            setAlignedIndent(indent);
            return wrapperList;
        }
        public void removeEntry(ILPCUniqueConfigBase entry) {
            for (int i = 0; i < subConfigs.size(); i++) {
                if (subConfigs.get(i).wrappedConfig == entry) {
                    subConfigs.remove(i).close();
                    onValueChanged();
                    getPage().markNeedUpdate();
                    return;
                }
            }
        }
    }

    //------ 添加按钮(单独一行，在条目上方)，附带操作按钮显示/折叠开关 ------
    static class AddButton extends ButtonConfig {
        AddButton(@NotNull ILPCConfigReadable parent, @NotNull Runnable onClick) {
            super(parent, "add", (b, mb) -> onClick.run());
        }
        //返回固定宽度的空格作为标签，为后面的交互框预留对齐位置
        @Override public String getConfigGuiDisplayName() { return "                               "; }
        @Override public void getButtonOptions(ButtonOptionArrayList res) {
            //添加按钮本身：使用buttonName(= .add.title) 作为显示文本，由addButtons机制翻译
            res.add(new ButtonOption(1, this, () -> buttonName, (x, y, w, h, str, listener, consumer, reset) -> {
                ButtonGeneric btn = new ButtonGeneric(x, y, w, h, str);
                List<String> lines = tooltipLines(getFullTranslationKey() + ".hover");
                if (!lines.isEmpty()) btn.setHoverStrings(lines);
                consumer.addButton(btn, listener);
            }));
            ConfigListConfig<?> list = (ConfigListConfig<?>) getParent();
            //显示/隐藏操作按钮开关
            res.add(new ButtonOption(ButtonWeightType.WIDTH, 20,
                (b, mb) -> { list.setHideOperationButton(!list.doHideOperationButton()); list.getPage().markNeedUpdate(); },
                null,
                (x, y, w, h, str, listener, consumer, reset) -> {
                    ButtonGeneric btn = new ButtonGeneric(x, y, w, h, list.doHideOperationButton() ? "<" : ">");
                    List<String> hoverLines = tooltipLines("lpctools.configs.fakePlayer.toggleOp.hover");
                    if (!hoverLines.isEmpty()) btn.setHoverStrings(hoverLines);
                    consumer.addButton(btn, listener);
                }));
            //折叠/展开操作按钮开关(仅在显示操作按钮时出现)
            if(!list.doHideOperationButton()) {
                res.add(new ButtonOption(ButtonWeightType.WIDTH, 20,
                    (b, mb) -> { list.setCondenseOperationButton(!list.doCondenseOperationButton()); list.getPage().markNeedUpdate(); },
                    null,
                    (x, y, w, h, str, listener, consumer, reset) -> {
                        ButtonGeneric btn = new ButtonGeneric(x, y, w, h, list.doCondenseOperationButton() ? "<>" : "><");
                        List<String> hoverLines = tooltipLines("lpctools.configs.fakePlayer.toggleCondense.hover");
                        if (!hoverLines.isEmpty()) btn.setHoverStrings(hoverLines);
                        consumer.addButton(btn, listener);
                    }));
            }
        }
    }

    //------ 带标签的字符串配置(子条目，标签显示在输入框前) ------
    public static class LabeledStringConfig extends UniqueStringConfig {
        final String hoverKey;
        public LabeledStringConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, @NotNull String defaultStr, @NotNull String hoverKey) {
            super(parent, nameKey, defaultStr, null);
            this.hoverKey = hoverKey;
        }
        @Override public void getButtonOptions(ButtonOptionArrayList res) {
            res.add(tooltipTextField(1, this, hoverKey));
        }
    }

    //------ 带标签的整数配置(子条目，标签显示在输入框前，支持滑动条) ------
    public static class LabeledIntegerConfig extends UniqueIntegerConfig {
        final String hoverKey, toggleHoverKey;
        public LabeledIntegerConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, int defaultVal, int min, int max, @NotNull String hoverKey, @NotNull String toggleHoverKey) {
            super(parent, nameKey, defaultVal, min, max, null);
            this.hoverKey = hoverKey;
            this.toggleHoverKey = toggleHoverKey;
        }
        @Override public void getButtonOptions(ButtonOptionArrayList res) {
            addTooltipIntegerOptions(res, this, hoverKey, toggleHoverKey);
        }
    }

    //------ 名单条目：仅显示文本输入框(无标签)，带悬浮提示 ------
    public static class NameEntry extends UniqueStringConfig {
        public NameEntry(@NotNull ILPCConfigReadable parent) {
            super(parent, "name", "", null);
        }
        @Override public String getConfigGuiDisplayName() { return ""; }
        @Override public void getButtonOptions(ButtonOptionArrayList res) {
            res.add(tooltipTextField(1, this, "lpctools.configs.fakePlayer.groups.group.behaviors.behavior.filter.names.name.comment"));
        }
    }

    //------ 透明名单列表：始终展开，对齐层级与父级一致(不额外缩进) ------
    public static class FPNameList extends FPListConfig<NameEntry> {
        public FPNameList(@NotNull ILPCConfigReadable parent, @NotNull String nameKey,
                          @NotNull Function<? super ConfigListConfig<NameEntry>, ? extends NameEntry> supplier) {
            super(parent, nameKey, supplier);
            expanded = true;
        }
        @Override public int getAlignLevel() { return getParent().getAlignLevel(); }
        @Override public ArrayList<GuiConfigsBase.ConfigOptionWrapper> buildConfigWrappers(ToIntFunction<String> getStringWidth, ArrayList<GuiConfigsBase.ConfigOptionWrapper> wrapperList) {
            expanded = true;
            return super.buildConfigWrappers(getStringWidth, wrapperList);
        }
        @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
            UpdateTodo todo = super.setValueFromJsonElementEx(element);
            expanded = true;
            return todo;
        }
    }

    //------ 假人组：可展开，行显示组名标签+可编辑组名，展开后含假玩家列表和行为列表 ------
    public static class GroupEntry extends ThirdListConfig {
        public final UniqueStringConfig groupName;
        public final FPListConfig<PlayerEntry> playersList;
        public final FPListConfig<BehaviorEntry> behaviorsList;

        public GroupEntry(@NotNull ILPCConfigReadable parent) {
            super(parent, "group", null);
            groupName = new UniqueStringConfig(this, "groupName", "假玩家组", null);
            playersList = new FPListConfig<>(this, "players", PlayerEntry::new);
            behaviorsList = new FPListConfig<>(this, "behaviors", list -> new BehaviorEntry(list, this));
            addConfig(playersList);
            addConfig(behaviorsList);
        }
        //始终计算alignedIndent，无论展开与否，确保交互区域位置稳定
        @Override public ArrayList<GuiConfigsBase.ConfigOptionWrapper> buildConfigWrappers(ToIntFunction<String> getStringWidth, ArrayList<GuiConfigsBase.ConfigOptionWrapper> wrapperList) {
            int indent = 0;
            for (ILPCConfig config : getConfigs()) {
                config.refreshName();
                if (expanded) {
                    wrapperList.add(new GuiConfigsBase.ConfigOptionWrapper(config));
                    if (config instanceof ILPCConfigReadable list)
                        list.buildConfigWrappers(getStringWidth, wrapperList);
                }
                indent = Math.max(indent, getStringWidth.applyAsInt(config.getConfigGuiDisplayName()));
            }
            setAlignedIndent(indent);
            return wrapperList;
        }
        @Override public void getButtonOptions(ButtonOptionArrayList res) {
            res.add(new ButtonOption(-1, (b, mb) -> setExpanded(!isExpanded()), null,
                tooltipIconAllocator(expanded ? MaLiLibIcons.ARROW_UP : MaLiLibIcons.ARROW_DOWN, LeftRight.CENTER, "lpctools.configs.fakePlayer.toggleExpand")));
            res.add(tooltipTextField(1, groupName, "lpctools.configs.fakePlayer.groups.group.groupName.comment"));
        }
        @Override public @NotNull JsonObject getAsJsonElement() {
            JsonObject object = super.getAsJsonElement();
            object.add("groupName", groupName.getAsJsonElement());
            return object;
        }
        @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
            UpdateTodo todo = super.setValueFromJsonElementEx(element);
            if (element instanceof JsonObject object && object.get("groupName") instanceof JsonElement e)
                todo.combine(groupName.setValueFromJsonElementEx(e).apply(groupName));
            return todo;
        }
    }

    //------ 假玩家条目：可展开，行显示名字标签+可编辑名字，展开后含变量名/起始值/结束值 ------
    public static class PlayerEntry extends ThirdListConfig {
        public final UniqueStringConfig name;
        public final LabeledStringConfig varName;
        public final LabeledIntegerConfig varStart;
        public final LabeledIntegerConfig varEnd;
        public PlayerEntry(@NotNull ILPCConfigReadable parent) {
            super(parent, "player", null);
            name = new UniqueStringConfig(this, "name", "", null);
            varName = new LabeledStringConfig(this, "varName", "", "lpctools.configs.fakePlayer.groups.group.players.player.varName.comment");
            varStart = new LabeledIntegerConfig(this, "varStart", 1, 0, Integer.MAX_VALUE, "lpctools.configs.fakePlayer.groups.group.players.player.varStart.comment", "lpctools.configs.fakePlayer.delayToggle.hover");
            varEnd = new LabeledIntegerConfig(this, "varEnd", 10, 0, Integer.MAX_VALUE, "lpctools.configs.fakePlayer.groups.group.players.player.varEnd.comment", "lpctools.configs.fakePlayer.delayToggle.hover");
            varStart.allowSlider = false;
            varEnd.allowSlider = false;
            addConfig(varName);
            addConfig(varStart);
            addConfig(varEnd);
        }
        //子条目额外缩进一级，确保展开后变量配置明显缩进
        @Override public int getAlignLevel() { return super.getAlignLevel() + 1; }
        //始终计算alignedIndent，无论展开与否，确保交互区域位置稳定
        @Override public ArrayList<GuiConfigsBase.ConfigOptionWrapper> buildConfigWrappers(ToIntFunction<String> getStringWidth, ArrayList<GuiConfigsBase.ConfigOptionWrapper> wrapperList) {
            int indent = 0;
            for (ILPCConfig config : getConfigs()) {
                config.refreshName();
                if (expanded) {
                    wrapperList.add(new GuiConfigsBase.ConfigOptionWrapper(config));
                    if (config instanceof ILPCConfigReadable list)
                        list.buildConfigWrappers(getStringWidth, wrapperList);
                }
                indent = Math.max(indent, getStringWidth.applyAsInt(config.getConfigGuiDisplayName()));
            }
            setAlignedIndent(indent);
            return wrapperList;
        }
        // 展开变量生成所有假玩家名字：变量名输入 t，名字中使用 $t$
        public void expandNames(@NotNull List<String> out) {
            String n = name.getStringValue();
            if (n.isEmpty()) return;
            String var = varName.getStringValue();
            if (var.isEmpty()) { out.add(n); return; }
            String token = "$" + var + "$";
            int start = varStart.getIntegerValue();
            int end = varEnd.getIntegerValue();
            for (int i = start; i <= end; i++) out.add(n.replace(token, String.valueOf(i)));
        }
        @Override public void getButtonOptions(ButtonOptionArrayList res) {
            res.add(new ButtonOption(-1, (b, mb) -> setExpanded(!isExpanded()), null,
                tooltipIconAllocator(expanded ? MaLiLibIcons.ARROW_UP : MaLiLibIcons.ARROW_DOWN, LeftRight.CENTER, "lpctools.configs.fakePlayer.toggleExpand")));
            res.add(tooltipTextField(1, name, "lpctools.configs.fakePlayer.groups.group.players.player.name.comment"));
        }
        @Override public @NotNull JsonObject getAsJsonElement() {
            JsonObject object = super.getAsJsonElement();
            object.add("name", name.getAsJsonElement());
            return object;
        }
        @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
            UpdateTodo todo = super.setValueFromJsonElementEx(element);
            if (element instanceof JsonObject object && object.get("name") instanceof JsonElement e)
                todo.combine(name.setValueFromJsonElementEx(e).apply(name));
            return todo;
        }
    }

    //------ 过滤器配置：可展开，行显示展开按钮+模式切换，展开后直接显示名单列表 ------
    public static class FilterConfig extends ThirdListConfig {
        public final UniqueStringConfig mode;
        public final FPNameList nameList;
        public FilterConfig(@NotNull ILPCConfigReadable parent) {
            super(parent, "filter", null);
            mode = new UniqueStringConfig(this, "mode", "blacklist", null);
            nameList = new FPNameList(this, "names", NameEntry::new);
            addConfig(nameList);
        }
        public boolean isWhitelist() { return "whitelist".equals(mode.getStringValue()); }
        // 判断该假玩家是否应执行此行为
        // 黑名单：名单中的玩家不执行（空名单=全部执行）
        // 白名单：只有名单中的玩家执行（空名单=全部不执行）
        public boolean shouldInclude(String name) {
            boolean inList = false;
            for (NameEntry entry : nameList.iterateConfigs()) {
                if (entry.getStringValue().equals(name)) { inList = true; break; }
            }
            return isWhitelist() == inList;
        }
        @Override public void getButtonOptions(ButtonOptionArrayList res) {
            res.add(new ButtonOption(-1, (b, mb) -> setExpanded(!isExpanded()), null,
                tooltipIconAllocator(expanded ? MaLiLibIcons.ARROW_UP : MaLiLibIcons.ARROW_DOWN, LeftRight.CENTER, "lpctools.configs.fakePlayer.toggleExpand")));
            res.add(new ButtonOption(1, (b, mb) -> {
                mode.setValueFromString(isWhitelist() ? "blacklist" : "whitelist");
            }, () -> Component.translatable(isWhitelist()
                ? "lpctools.configs.fakePlayer.groups.group.behaviors.behavior.filter.mode.whitelist"
                : "lpctools.configs.fakePlayer.groups.group.behaviors.behavior.filter.mode.blacklist").getString(),
                tooltipButtonAllocator("lpctools.configs.fakePlayer.groups.group.behaviors.behavior.filter.mode.hover")));
        }
        //展开后跳过名单列表容器行，直接渲染添加按钮和名字输入框；始终计算indent确保位置稳定
        @Override public ArrayList<GuiConfigsBase.ConfigOptionWrapper> buildConfigWrappers(ToIntFunction<String> getStringWidth, ArrayList<GuiConfigsBase.ConfigOptionWrapper> wrapperList) {
            if (expanded)
                nameList.buildConfigWrappers(getStringWidth, wrapperList);
            else
                nameList.buildConfigWrappers(getStringWidth, new ArrayList<>());
            setAlignedIndent(nameList.getAlignedIndent());
            return wrapperList;
        }
        @Override public @NotNull JsonObject getAsJsonElement() {
            JsonObject object = super.getAsJsonElement();
            object.add("mode", mode.getAsJsonElement());
            return object;
        }
        @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
            UpdateTodo todo = super.setValueFromJsonElementEx(element);
            if (element instanceof JsonObject object && object.get("mode") instanceof JsonElement e)
                todo.combine(mode.setValueFromJsonElementEx(e).apply(mode));
            return todo;
        }
    }

    //------ 行为条目：可展开，行显示触发按钮+快捷键，展开后含命令、延迟、过滤器 ------
    public static class BehaviorEntry extends ThirdListConfig {
        public final LabeledStringConfig action;
        public final LabeledIntegerConfig delay;
        public final FilterConfig filter;
        public final ButtonHotkeyConfig trigger;
        public BehaviorEntry(@NotNull ILPCConfigReadable parent, @NotNull GroupEntry group) {
            super(parent, "behavior", null);
            action = new LabeledStringConfig(this, "action", "spawn", "lpctools.configs.fakePlayer.groups.group.behaviors.behavior.action.comment");
            delay = new LabeledIntegerConfig(this, "delay", 0, 0, 60000, "lpctools.configs.fakePlayer.groups.group.behaviors.behavior.delay.comment", "lpctools.configs.fakePlayer.delayToggle.hover");
            filter = new FilterConfig(this);
            addConfig(action);
            addConfig(delay);
            addConfig(filter);
            trigger = new ButtonHotkeyConfig(this, "trigger", null, () -> FakePlayerGroup.runBatch(group, this));
            trigger.buttonName = "lpctools.configs.fakePlayer.trigger";
        }
        //子条目额外缩进一级，确保展开后命令/延迟/过滤器明显缩进
        @Override public int getAlignLevel() { return super.getAlignLevel() + 1; }
        //始终计算alignedIndent，无论展开与否，确保交互区域位置稳定
        @Override public ArrayList<GuiConfigsBase.ConfigOptionWrapper> buildConfigWrappers(ToIntFunction<String> getStringWidth, ArrayList<GuiConfigsBase.ConfigOptionWrapper> wrapperList) {
            int indent = 0;
            for (ILPCConfig config : getConfigs()) {
                config.refreshName();
                if (expanded) {
                    wrapperList.add(new GuiConfigsBase.ConfigOptionWrapper(config));
                    if (config instanceof ILPCConfigReadable list)
                        list.buildConfigWrappers(getStringWidth, wrapperList);
                }
                indent = Math.max(indent, getStringWidth.applyAsInt(config.getConfigGuiDisplayName()));
            }
            setAlignedIndent(indent);
            return wrapperList;
        }
        @Override public void getButtonOptions(ButtonOptionArrayList res) {
            res.add(new ButtonOption(-1, (b, mb) -> setExpanded(!isExpanded()), null,
                tooltipIconAllocator(expanded ? MaLiLibIcons.ARROW_UP : MaLiLibIcons.ARROW_DOWN, LeftRight.CENTER, "lpctools.configs.fakePlayer.toggleExpand")));
            res.add(new ButtonOption(1, trigger, () -> trigger.buttonName, tooltipButtonAllocator("lpctools.configs.fakePlayer.trigger.hover")));
            res.add(tooltipKeybind(1, trigger, "lpctools.configs.fakePlayer.keybind.hover"));
        }
        @Override public @NotNull JsonObject getAsJsonElement() {
            JsonObject object = super.getAsJsonElement();
            object.add("hotkey", trigger.keybind.getAsJsonElement());
            return object;
        }
        @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
            UpdateTodo todo = super.setValueFromJsonElementEx(element);
            if (element instanceof JsonObject object && object.get("hotkey") instanceof JsonElement e)
                trigger.keybind.setValueFromJsonElement(e);
            return todo;
        }
        @Override public void close() throws Exception {
            trigger.close();
            super.close();
        }
    }
}
