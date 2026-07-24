package lpctools.tools.tradeReroller;

import com.google.common.collect.ImmutableList;
import lpctools.lpcfymasaapi.LPCConfigPage;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.*;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanThirdListConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ButtonThirdListConfig;
import lpctools.mixin.client.accessors.ConfigStringListAccessor;
import lpctools.tools.ToolUtils;
import lpctools.util.inGame.InGameUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

public class TradeReroller {
	public static final BooleanHotkeyThirdListConfig TRConfig = ToolUtils.configBuilder("TR").withToolRunner(TradeRerollRunner::new).build();
	static {listStack.push(TRConfig);}
	public static final StringListConfig targetEnchantments = addStringListConfig("targetEnchantments", ImmutableList.of());
	public static final ButtonThirdListConfig defaultListSettings = addConfigEx(l->new ButtonThirdListConfig(l, "defaultListSettings", (_, _)->updateEnchantments()));
	static {listStack.push(defaultListSettings);}
	public static final BooleanConfig testTradeable = addBooleanConfig("testTradeable", true);
	public static final DoubleConfig costFactor = addDoubleConfig("costFactor", 3, 0, 64);
	public static final DoubleConfig costConstant = addDoubleConfig("costConstant", 2, 0, 64);
	public static final BooleanConfig doubleBeforeRounding = addBooleanConfig("doubleBeforeRounding", false);
	static {listStack.pop();}
	public static final BooleanConfig testDoublePrice = addBooleanConfig("testDoublePrice", true);
	public static final BooleanThirdListConfig reserveCheaperTraders = addBooleanThirdListConfig("reserveCheaperTraders", false, null);
	public static final BooleanConfig pursueBelowMinPrice = addBooleanConfig(reserveCheaperTraders, "pursueBelowMinPrice", false, null);
	public static final BooleanThirdListConfig displayRolls = addBooleanThirdListConfig("displayRolls", true, null);
	public static final BooleanConfig onlyDisplaySucceededRolls = addBooleanConfig(displayRolls, "onlyDisplaySucceededRolls", false, null);
	public static final IntegerConfig timeOutTicks = addIntegerConfig("timeOutTicks", 20 * 60 * 60); // default for 60 minutes (an hour or 3 minecraft days)
	static {listStack.pop();}
	static {ClientPlayConnectionEvents.JOIN.register((_, _, _)->updateEnchantments());}
	private static void updateEnchantments() {
		ArrayList<String> defaultStrings = new ArrayList<>();
		Registry<Enchantment> enchantments = InGameUtils.getRegistry(Registries.ENCHANTMENT);
		if(enchantments != null) {
			for(var enchantmentEntry : enchantments.entrySet()) {
				Holder<Enchantment> holder = enchantments.get(enchantmentEntry.getKey()).orElseThrow();
				if(testTradeable.getBooleanValue() && !holder.is(EnchantmentTags.TRADEABLE)) continue;
				int maxLevel = holder.value().getMaxLevel();
				boolean shouldDoublePrice = testDoublePrice.getBooleanValue() && holder.is(EnchantmentTags.DOUBLE_TRADE_PRICE);
				boolean doubleBeforeRounding = TradeReroller.doubleBeforeRounding.getBooleanValue();
				double rawCost = costConstant.getDoubleValue() + maxLevel * costFactor.getDoubleValue();
				int cost = (int)Math.round(rawCost * (doubleBeforeRounding && shouldDoublePrice ? 2 : 1)) * (!doubleBeforeRounding && shouldDoublePrice ? 2 : 1);
				EnchantmentTradeOption option = new EnchantmentTradeOption(enchantmentEntry.getKey().identifier(), enchantmentEntry.getValue().getMaxLevel(), cost);
				defaultStrings.add(option.toJsonString());
			}
			defaultStrings.sort(String::compareTo);
		}
		((ConfigStringListAccessor)targetEnchantments).setDefaultValue(ImmutableList.copyOf(defaultStrings));
		Minecraft.getInstance().schedule(()->targetEnchantments.getPage().applyToPageInstanceIfNotNull(LPCConfigPage.ConfigPageInstance::initGui));
	}
}
