package top.mrxiaom.itisme;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import net.mamoe.mirai.utils.MiraiLogger;

public class ConfigManager {
	public File configFile;
	JSONObject json;
	MiraiLogger logger;
	public ConfigManager(Natsuko main) {
		logger = MiraiLogger.Companion.create("NatsukoConfigManager");
		configFile = new File(main.getDataFolder(), "config.json");
	}
	
	public long getBotId() {
		return json.getLong("qq");
	}
	
	public boolean isAutoAccessFriendRequest() {
		return json.getBoolean("auto-access-friend-request");
	}
	
	public JSONArray getBlackList() {
		return json.getJSONArray("blacklist");
	}
	
	public boolean isBlackList(long qq) {
		JSONArray array = json.getJSONArray("blacklist");
		return array.contains(qq);
	}
	
	public void addBlackList(long qq) {
		JSONArray array = json.getJSONArray("blacklist");
		array.add(qq);
		json.put("blacklist", array);
		this.saveConfig();
	}
	
	public void removeBlackList(long qq) {
		JSONArray array = json.getJSONArray("blacklist");
		array.remove(qq);
		json.put("blacklist", array);
		this.saveConfig();
	}
	
	public void reloadConfig() {
		if(!configFile.exists()) {
			logger.info("配置文件不存在，正在导出");
			
			InputStream is = this.getClass().getResourceAsStream("/config.json");
			try(FileOutputStream fos = new FileOutputStream(configFile)) {
				byte[] b = new byte[1024];

				int length;

				while((length=is.read(b))>0){
					fos.write(b, 0, length);
				}

				is.close();
				fos.close();
			}catch(Throwable t){
				t.printStackTrace();
			} finally {
			}
		}
		json = JSONObject.parseObject(Util.readFile(configFile));
	}
	
	public void saveConfig() {
		try {
			Throwable t = Util.writeFile(configFile, json.toJSONString());
			if(t != null) throw t;
		} catch (Throwable t) {
			logger.error("配置文件保存错误");
			t.printStackTrace();
		}
	}
	
	public JSONObject getJSON() {
		return json;
	}
	
	public void setJSON(JSONObject j) {
		json = j;
	}
}