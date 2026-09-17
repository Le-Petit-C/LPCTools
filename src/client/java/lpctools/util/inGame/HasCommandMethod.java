package lpctools.util.inGame;

//lpctools.generic.GenericUtils#hasCommand 判断玩家是否拥有某个命令的方式
public enum HasCommandMethod {
	//查询服务器下发的命令树中是否存在该命令（原版行为）
	COMMAND_TREE,
	//仅检测玩家是否拥有 COMMANDS_GAMEMASTER 权限
	PERMISSION
}
