package lpctools.tweaks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOption;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptions;
import fi.dy.masa.tweakeroo.gui.GuiConfigs;
import lpctools.LPCTools;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.*;
import lpctools.lpcfymasaapi.interfaces.ILPCConfig;
import lpctools.mixinInterfaces.MASAMixins.IGuiListBaseMixin;
import lpctools.util.DataUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static fi.dy.masa.tweakeroo.config.Configs.Lists.*;

public class LPCfyTweakerooList {
	public static final BooleanConfig lpcfyTweakerooList = new BooleanConfig(TweakConfigs.tweaks, "lpcfyTweakerooList", false);
	public static @Nullable FunctionClass functionClass;
	static {
		if(FabricLoader.getInstance().isModLoaded("tweakeroo")){
			functionClass = new FunctionClass();
			lpcfyTweakerooList.setValueChangeCallback(functionClass::functionCallback);
		}
	}
	
	public static class FunctionClass{
		public static @Nullable GuiConfigs tweakerooGuiConfigs;
		public static Consumer<ImmutableList<IConfigBase>> optionSetter;
		public final ImmutableList<IConfigBase> oldOptions = OPTIONS;
		private final ImmutableMap<ConfigStringList, Function<ConfigStringList, LPCStringListWrapper>> mutingConfigs =
			new ImmutableMap.Builder<ConfigStringList, Function<ConfigStringList, LPCStringListWrapper>>()
				.put(BLOCK_TYPE_BREAK_RESTRICTION_BLACKLIST, BlockListLPCWrapper::new)
				.put(BLOCK_TYPE_BREAK_RESTRICTION_WHITELIST, BlockListLPCWrapper::new)
				.put(ENTITY_TYPE_ATTACK_RESTRICTION_BLACKLIST, EntityTypeListLPCWrapper::new)
				.put(ENTITY_TYPE_ATTACK_RESTRICTION_WHITELIST, EntityTypeListLPCWrapper::new)
				.put(FAST_PLACEMENT_ITEM_BLACKLIST, ItemListLPCWrapper::new)
				.put(FAST_PLACEMENT_ITEM_WHITELIST, ItemListLPCWrapper::new)
				.put(FAST_RIGHT_CLICK_BLOCK_BLACKLIST, BlockListLPCWrapper::new)
				.put(FAST_RIGHT_CLICK_BLOCK_WHITELIST, BlockListLPCWrapper::new)
				.put(FAST_RIGHT_CLICK_ITEM_BLACKLIST, ItemListLPCWrapper::new)
				.put(FAST_RIGHT_CLICK_ITEM_WHITELIST, ItemListLPCWrapper::new)
				.put(HAND_RESTOCK_BLACKLIST, ItemListLPCWrapper::new)
				.put(HAND_RESTOCK_WHITELIST, ItemListLPCWrapper::new)
				.put(SILK_TOUCH_OVERRIDE, BlockListLPCWrapper::new)
				.put(UNSTACKING_ITEMS, ItemListLPCWrapper::new)
				.build();
		private final HashMap<ConfigStringList, LPCStringListWrapper> cachedConfigs = new HashMap<>();
		private boolean updateMarked = false;
		public void refreshCachedConfigs(){
			cachedConfigs.forEach((wrappedConfig, wrapperConfig)->
				wrapperConfig.setValueFromStrings(wrappedConfig.getStrings()));
		}
		private void functionCallback(){
			if(lpcfyTweakerooList.getAsBoolean())
				refreshCachedConfigs();
			updateConfigs(lpcfyTweakerooList.getAsBoolean());
		}
		public void updateConfigs(boolean useLPCStyle){
			if(optionSetter == null) {
				LPCTools.LOGGER.warn("LPCfyTweakerooList: why is Tweakeroo still not prepared?");
				return;
			}
			if(useLPCStyle){
				ImmutableList.Builder<IConfigBase> builder = new ImmutableList.Builder<>();
				for(var config : oldOptions){
					LPCStringListWrapper newConfig;
					if(config instanceof ConfigStringList configStringList){
						if(cachedConfigs.containsKey(configStringList))
							newConfig = cachedConfigs.get(configStringList);
						else if(mutingConfigs.get(configStringList) instanceof Function<ConfigStringList, LPCStringListWrapper> function){
							newConfig = function.apply(configStringList);
							cachedConfigs.put(configStringList, newConfig);
						}
						else newConfig = null;
						if(newConfig != null){
							builder.add(newConfig);
							if(newConfig.isExpanded()){
								for(var subConfig : newConfig.getConfigs())
									builder.add(subConfig);
							}
							continue;
						}
					}
					builder.add(config);
				}
				optionSetter.accept(builder.build());
			}
			else optionSetter.accept(oldOptions);
			updateMarked = false;
		}
		
		@SuppressWarnings("SameParameterValue")
		private static <T> @NotNull T orElse(@Nullable T v, @NotNull T def){
			return v == null ? def : v;
		}
		
		private interface LPCStringListWrapper extends IConfigBase{
			void setValueFromStrings(List<String> strings);
			boolean isExpanded();
			Iterable<? extends ILPCConfig> getConfigs();
		}
		
		private void sendUpdateCurrentScreen(){
			MinecraftClient client = MinecraftClient.getInstance();
			if(client.currentScreen instanceof GuiConfigs configs)
				tweakerooGuiConfigs = configs;
			if(updateMarked) return;
			updateMarked = true;
			client.send(()->{
				updateConfigs(lpcfyTweakerooList.getAsBoolean());
				if(tweakerooGuiConfigs != null)
					tweakerooGuiConfigs.initGui();
			});
			if(tweakerooGuiConfigs != null){
				//noinspection unchecked
				((IGuiListBaseMixin<GuiConfigsBase.ConfigOptionWrapper, WidgetConfigOption, WidgetListConfigOptions>)tweakerooGuiConfigs)
					.invokeGetListWidget().markConfigsModified();
			}
		}
		
		private class BlockListLPCWrapper extends BlockListConfig implements LPCStringListWrapper{
			private final ConfigStringList wrappedConfig;
			public BlockListLPCWrapper(ConfigStringList wrappedConfig) {
				super(TweakConfigs.tweaks, "temp", DataUtils.blockListFromIds(wrappedConfig.getDefaultStrings()), null);
				this.wrappedConfig = wrappedConfig;
				setValueFromStrings(wrappedConfig.getStrings());
			}
			
			@Override public String getName() {return wrappedConfig.getName();}
			@Override public @NotNull String getPrettyName() {return orElse(wrappedConfig.getPrettyName(), "");}
			@Override public @NotNull String getComment() {return orElse(wrappedConfig.getComment(), "");}
			@Override public @NotNull String getTranslatedName() {return orElse(wrappedConfig.getTranslatedName(), "");}
			@Override public void setPrettyName(@NotNull String prettyName) {wrappedConfig.setPrettyName(prettyName);}
			@Override public void setComment(@NotNull String comment) {wrappedConfig.setComment(comment);}
			@Override public void setTranslatedName(@NotNull String translatedName) {wrappedConfig.setTranslatedName(translatedName);}
			@Override public void setValueFromStrings(List<String> strings){
				if(strings.equals(DataUtils.idListFromBlockList(getBlocks()))) return;
				boolean lastExpanded = expanded;
				setBlocks(DataUtils.blockListFromIds(strings));
				expanded = lastExpanded;
			}
			@Override public boolean isExpanded() {return expanded;}
			@Override public void setValueFromJsonElement(@NotNull JsonElement data) {
				wrappedConfig.setValueFromJsonElement(data);
				setValueFromStrings(wrappedConfig.getStrings());
			}
			@Override public void setExpanded(boolean b) {
				if(b != expanded){
					expanded = b;
					sendUpdateCurrentScreen();
				}
			}
			@Override public void setHideOperationButton(boolean b) {
				if(b != hideOperationButton){
					hideOperationButton = b;
					sendUpdateCurrentScreen();
				}
			}
			@Override public void setCondenseOperationButton(boolean b) {
				if(b != condenseOperationButton){
					condenseOperationButton = b;
					sendUpdateCurrentScreen();
				}
			}
			@Override public @Nullable JsonElement getAsJsonElement() {return wrappedConfig.getAsJsonElement();}
			
			@Override public void onValueChanged() {
				super.onValueChanged();
				if(wrappedConfig != null) wrappedConfig.setStrings(DataUtils.idListFromBlockList(getBlocks()));
				sendUpdateCurrentScreen();
			}
		}
		private class EntityTypeListLPCWrapper extends EntityTypeListConfig implements LPCStringListWrapper{
			private final ConfigStringList wrappedConfig;
			public EntityTypeListLPCWrapper(ConfigStringList wrappedConfig) {
				super(TweakConfigs.tweaks, "temp", null);
				this.wrappedConfig = wrappedConfig;
				setEntityTypes(DataUtils.entityTypeListFromIds(wrappedConfig.getDefaultStrings()));
				setCurrentAsDefault(false);
				setValueFromStrings(wrappedConfig.getStrings());
			}
			
			@Override public String getName() {return wrappedConfig.getName();}
			@Override public @NotNull String getPrettyName() {return orElse(wrappedConfig.getPrettyName(), "");}
			@Override public @NotNull String getComment() {return orElse(wrappedConfig.getComment(), "");}
			@Override public @NotNull String getTranslatedName() {return orElse(wrappedConfig.getTranslatedName(), "");}
			@Override public void setPrettyName(@NotNull String prettyName) {wrappedConfig.setPrettyName(prettyName);}
			@Override public void setComment(@NotNull String comment) {wrappedConfig.setComment(comment);}
			@Override public void setTranslatedName(@NotNull String translatedName) {wrappedConfig.setTranslatedName(translatedName);}
			@Override public void setValueFromStrings(List<String> strings){
				if(strings.equals(DataUtils.idListFromEntityTypeList(getEntityTypes()))) return;
				boolean lastExpanded = expanded;
				setEntityTypes(DataUtils.entityTypeListFromIds(strings));
				expanded = lastExpanded;
				sendUpdateCurrentScreen();
			}
			@Override public boolean isExpanded() {return expanded;}
			@Override public void setValueFromJsonElement(@NotNull JsonElement data) {
				wrappedConfig.setValueFromJsonElement(data);
				setValueFromStrings(wrappedConfig.getStrings());
			}
			@Override public void setExpanded(boolean b) {
				if(b != expanded){
					expanded = b;
					sendUpdateCurrentScreen();
				}
			}
			@Override public void setHideOperationButton(boolean b) {
				if(b != hideOperationButton){
					hideOperationButton = b;
					sendUpdateCurrentScreen();
				}
			}
			@Override public void setCondenseOperationButton(boolean b) {
				if(b != condenseOperationButton){
					condenseOperationButton = b;
					sendUpdateCurrentScreen();
				}
			}
			@Override public @Nullable JsonElement getAsJsonElement() {return wrappedConfig.getAsJsonElement();}
			
			@Override public void onValueChanged() {
				super.onValueChanged();
				if(wrappedConfig != null) wrappedConfig.setStrings(DataUtils.idListFromEntityTypeList(getEntityTypes()));
				sendUpdateCurrentScreen();
			}
		}
		
		private class ItemListLPCWrapper extends ItemListConfig implements LPCStringListWrapper{
			private final ConfigStringList wrappedConfig;
			public ItemListLPCWrapper(ConfigStringList wrappedConfig) {
				super(TweakConfigs.tweaks, "temp", DataUtils.itemListFromIds(wrappedConfig.getDefaultStrings()), null);
				this.wrappedConfig = wrappedConfig;
				setValueFromStrings(wrappedConfig.getStrings());
			}
			
			@Override public String getName() {return wrappedConfig.getName();}
			@Override public @NotNull String getPrettyName() {return orElse(wrappedConfig.getPrettyName(), "");}
			@Override public @NotNull String getComment() {return orElse(wrappedConfig.getComment(), "");}
			@Override public @NotNull String getTranslatedName() {return orElse(wrappedConfig.getTranslatedName(), "");}
			@Override public void setPrettyName(@NotNull String prettyName) {wrappedConfig.setPrettyName(prettyName);}
			@Override public void setComment(@NotNull String comment) {wrappedConfig.setComment(comment);}
			@Override public void setTranslatedName(@NotNull String translatedName) {wrappedConfig.setTranslatedName(translatedName);}
			@Override public void setValueFromStrings(List<String> strings){
				if(strings.equals(DataUtils.idListFromItemList(getItems()))) return;
				boolean lastExpanded = expanded;
				setItems(DataUtils.itemListFromIds(strings));
				expanded = lastExpanded;
			}
			@Override public boolean isExpanded() {return expanded;}
			@Override public void setValueFromJsonElement(@NotNull JsonElement data) {
				wrappedConfig.setValueFromJsonElement(data);
				setValueFromStrings(wrappedConfig.getStrings());
			}
			@Override public void setExpanded(boolean b) {
				if(b != expanded){
					expanded = b;
					sendUpdateCurrentScreen();
				}
			}
			@Override public void setHideOperationButton(boolean b) {
				if(b != hideOperationButton){
					hideOperationButton = b;
					sendUpdateCurrentScreen();
				}
			}
			@Override public void setCondenseOperationButton(boolean b) {
				if(b != condenseOperationButton){
					condenseOperationButton = b;
					sendUpdateCurrentScreen();
				}
			}
			
			@Override public @Nullable JsonElement getAsJsonElement() {return wrappedConfig.getAsJsonElement();}
			
			@Override public void onValueChanged() {
				super.onValueChanged();
				if(wrappedConfig != null) wrappedConfig.setStrings(DataUtils.idListFromItemList(getItems()));
				sendUpdateCurrentScreen();
			}
		}
	}
}
