package lpctools.util;

import lpctools.LPCTools;
import net.minecraft.client.Minecraft;

public class GameTime {
	public long realTimeMillis, gameTimeMillis, worldTicks, dayTicks;
	/*
	* realTimeMillis:以毫秒为单位的现实世界时间
	* gameTimeMillis:以毫秒为单位的从LPCTools初始化到现在的时间
	* worldTicks:（游戏内）世界游戏刻数
	* dayTicks:（游戏内）计游戏天数的游戏刻数
	*/
	public GameTime(){}
	public GameTime setCurrent(){
		realTimeMillis = System.currentTimeMillis();
		gameTimeMillis = realTimeMillis - LPCTools.startTimeMillis;
		var world = Minecraft.getInstance().level;
		if(world != null) {
			worldTicks = world.getGameTime();
			dayTicks = world.getDefaultClockTime();//world.getDayTime();
			// TODO
		}
		else dayTicks = worldTicks = -1;
		return this;
	}
	public static GameTime ofCurrent(){
		return new GameTime().setCurrent();
	}
}
