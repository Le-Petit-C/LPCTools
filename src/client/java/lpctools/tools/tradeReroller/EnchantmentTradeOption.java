package lpctools.tools.tradeReroller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

public record EnchantmentTradeOption(EnchantmentWithLevel enchantment, int cost) {
	public EnchantmentTradeOption(Identifier id, int level, int maxCost) {
		this(new EnchantmentWithLevel(id, level), maxCost);
	}
	public record EnchantmentWithLevel(Identifier id, int level) {
		int minCost(Registry<Enchantment> registry) {
			int rawCost = 2 + level * 3;
			if(TradeReroller.testDoublePrice.getBooleanValue()) {
				var o = registry.get(id);
				if(o.isPresent() && o.get().is(EnchantmentTags.DOUBLE_TRADE_PRICE)) return rawCost * 2;
			}
			return rawCost;
		}
	}
	public @NonNull JsonArray toJson() {
		JsonArray res = new JsonArray();
		res.add(enchantment.id.toShortString());
		res.add(enchantment.level);
		res.add(cost);
		return res;
	}
	public String toJsonString() {
		return toJson().toString();
	}
	public static @Nullable EnchantmentTradeOption fromJson(JsonElement element) {
		return fromJsonRaw(element, e->e);
	}
	public static @Nullable EnchantmentTradeOption fromJsonString(String json) {
		return fromJsonRaw(json, JsonParser::parseString);
	}
	private static <T> @Nullable EnchantmentTradeOption fromJsonRaw(T raw, Function<T, JsonElement> translator) {
		try {
			JsonArray object = translator.apply(raw).getAsJsonArray();
			return new EnchantmentTradeOption(
				new EnchantmentWithLevel(Identifier.parse(object.get(0).getAsString()), object.get(1).getAsInt()),
				object.get(2).getAsInt());
		} catch (Exception e) {
			// TODO msg("Failed to parse raw into enchantment-level pair")
			return null;
		}
	}
}
