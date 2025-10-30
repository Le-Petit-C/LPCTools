package lpctools.util;

import com.google.gson.JsonElement;
import lpctools.LPCTools;
import org.apache.logging.log4j.Logger;

public class LoggerUtils {
	public static Logger logger(){return LPCTools.LOGGER;}
	public static void logFailedLoadingConfig(Object configInstance, JsonElement configData){
		logger().warn("Failed loading config for class \"{}\" with value \"{}\"", configInstance.getClass().getName(), configData);
	}
}
