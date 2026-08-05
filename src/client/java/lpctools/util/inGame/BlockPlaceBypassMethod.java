package lpctools.util.inGame;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public enum BlockPlaceBypassMethod implements BlockOperationRunner.CalculatorGenerator<BlockPlacing.StatusCalculator>, BlockPlacing.StatusCalculator {
	NONE{ @Override public boolean isValidPos(InGameManager manager, BlockPos targetPos) { return true; } },
	// 不能airplace，但是不必交互相邻方块
	NO_AIR_PLACE {
		@Override public boolean isValidPos(InGameManager manager, BlockPos targetPos) {
			return ATTACH.isValidPos(manager, targetPos);
		}
	},
	// 不能airplace，必需交互相邻方块
	ATTACH {
		@Override public boolean isValidPos(InGameManager manager, BlockPos targetPos) {
			// 空形状不一定就是airplace，例如水上允许放睡莲，，故还是仅检测是否为空气吧
			//if(!manager.getBlockShape(targetPos.relative(direction)).isEmpty()) return true;
			if(!manager.getBlockState(targetPos).isAir()) return true;
			for (var direction : Direction.values()) {
				//if(!manager.getBlockShape(targetPos.relative(direction)).isEmpty())
				if(!manager.getBlockState(targetPos.relative(direction)).isAir())
					return true;
			}
			return false;
		}
	};

	@Override public BlockPlacing.StatusCalculator createCalculator(InGameManager ignored) { return this; }
}
