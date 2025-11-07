package lpctools.script;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import lpctools.LPCTools;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ConfigListConfig;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;
import static lpctools.script.ScriptConfigs.*;

public class ScriptsConfig extends ConfigListConfig<ScriptConfig> {
	public static final ScriptsConfig instance = new ScriptsConfig();
	
	public final HashSet<String> existScript = new HashSet<>();
	
	private boolean initialized = false;
	
	public static final String scriptDirectoryName = "LPCScripts";
	
	private ScriptsConfig(){
		super(script, "scripts", config->{
				var res = new ScriptConfig(config);
				int i = 0;
				String id;
				do { id = "script#" + i++;
				} while (instance.existScript.contains(id));
				res.script.setId(id);
				return res;
			}, null);
		setRemoveCallback(config->{
			existScript.remove(config.script.getId());
			try { Files.delete(FileUtils.getConfigDirectoryAsPath().resolve(scriptDirectoryName).resolve(config.script.getId() + ".json"));
			} catch (IOException ignored) {}
		});
	}
	
	@Override public void onAddConfigClicked(int position){
		saveScript(allocateAndAddConfig(position));
	}
	
	public static void saveScript(ScriptConfig script){
		Path scriptDirectoryPath = FileUtils.getConfigDirectoryAsPath().resolve(scriptDirectoryName);
		if(!FileUtils.createDirectoriesIfMissing(scriptDirectoryPath)) return;
		Path scriptPath = scriptDirectoryPath.resolve(script.script.getId() + ".json");
		JsonUtils.writeJsonToFileAsPath(script.getAsJsonElement(), scriptPath);
	}
	
	@Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement data) {
		if(data instanceof JsonArray array){
			for(var scriptIdJson : array){
				if(scriptIdJson instanceof JsonPrimitive primitive)
					loadScript(primitive.getAsString());
				else warnFailedLoadingConfig("script", scriptIdJson);
			}
		}
		loadRestScripts();
		initialized = true;
		return new UpdateTodo().valueChanged();
	}
	
	@Override public @NotNull JsonElement getAsJsonElement() {
		JsonArray res = new JsonArray();
		for(var sub : iterateConfigs())
			res.add(sub.script.getId());
		return res;
	}
	
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		if(!initialized){
			loadRestScripts();
			initialized = true;
		}
		super.getButtonOptions(res);
	}
	
	//加载特定id的script
	void loadScript(String scriptId){
		if(existScript.contains(scriptId)) return;
		Path scriptPath = FileUtils.getConfigDirectoryAsPath().resolve(scriptDirectoryName).resolve(scriptId + ".json");
		JsonElement scriptJson = JsonUtils.parseJsonFileAsPath(scriptPath);
		if(scriptJson != null){
			@SuppressWarnings("resource")
			ScriptConfig config = allocateAndAddConfig();
			config.setValueFromJsonElement(scriptJson);
			config.script.setId(scriptId);
		}
	}
	
	//加载不在列表中但是在文件夹中的Script
	void loadRestScripts(){
		Path scriptDirectoryPath = FileUtils.getConfigDirectoryAsPath().resolve(scriptDirectoryName);
		File scriptDirectory = scriptDirectoryPath.toFile();
		if(scriptDirectory.isDirectory()){
			File[] scriptFiles = scriptDirectory.listFiles();
			if(scriptFiles != null){
				for(File scriptFile : scriptFiles){
					if(scriptFile.isFile()){
						String fileName = scriptFile.getName();
						if(!fileName.endsWith(".json")) continue;
						loadScript(fileName.substring(0, fileName.length() - 5));
					}
				}
			}
			else LPCTools.LOGGER.warn("Unable to access directory {}", scriptDirectoryPath);
		}
	}
}
