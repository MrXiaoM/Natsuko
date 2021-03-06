package top.mrxiaom.itisme.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.FlashImage;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.utils.ExternalResource;
import top.mrxiaom.itisme.Natsuko;
import top.mrxiaom.itisme.Util;

public class Acg {
	
	Natsuko main;
	public List<Long> managers = new ArrayList<Long>();
	long nexttime = 0;
	boolean init = false;
    boolean enable = true;

	JSONArray normal;
	JSONArray r18;
	boolean isbanned = false;
	boolean nextDayCheck = false;
	long nextCheckUpdateTime = -1;
	
	@Deprecated
	public Acg(Natsuko main) {
		this.main = main;
		this.seTuListFile = new File(main.getDataFolder().getAbsoluteFile() + "\\preSetu.json");
		this.dataFile = new File(main.getDataFolder().getAbsoluteFile() + "\\data.json");

		this.initPictureList();
		this.loadLocalSeTu();
		this.clearRepetitive();
		this.saveSeTuList();
		initStats();
		managers.add(2431208142L);
		managers.add(2406120646L);

		Calendar now = Calendar.getInstance();
		nextCheckUpdateTime = now.get(Calendar.HOUR_OF_DAY) * 3600 + now.get(Calendar.MINUTE) * 60
				+ now.get(Calendar.SECOND) + 30;
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				
				if(isbanned || !enable) return;
				//main.getLogger().info("????");
				Calendar now = Calendar.getInstance();
				long nowtime = now.get(Calendar.HOUR_OF_DAY) * 3600 + now.get(Calendar.MINUTE) * 60
						+ now.get(Calendar.SECOND);
				if(nowtime < 128 && nextCheckUpdateTime == 86401 && nextDayCheck) {
					main.getLogger().info("????????");
					nextDayCheck = false;
					nextCheckUpdateTime = nowtime + 10 * 60;
					updateSeTu();
					clearRepetitive();
					saveSeTuList();
				}
				if(nowtime >= nextCheckUpdateTime || (nextCheckUpdateTime > nowtime + 10 * 60 && !nextDayCheck)) {
					main.getLogger().info("????????");
					nextCheckUpdateTime = nowtime + 10 * 60;
					updateSeTu();
					clearRepetitive();
					saveSeTuList();
				}
				
			}
		}, 20 * 1000, 1 * 1000);
	}
	
	class NumComparator implements Comparator<Integer> {  
	    public int compare(Integer o1, Integer o2) {  
	        return o1 - o2;  
	    }  
	}  
	
	public void clearRepetitive() {
		Comparator<Integer> ascComparator = new NumComparator();
		Comparator<Integer> descComparator = Collections.reverseOrder(ascComparator);  
		List<Integer> normalRemove = new ArrayList<Integer>();
		List<Integer> preNormalRemove = new ArrayList<Integer>();
		List<Integer> r18Remove = new ArrayList<Integer>(); 
		List<Integer> preR18Remove = new ArrayList<Integer>();
		for(int i = 0;i < normal.size(); i++) {
			JSONObject json1 = normal.getJSONObject(i);
			for(int j = 0;j < normal.size(); j++) {
				JSONObject json2 = normal.getJSONObject(j);
				if(json1.getString("url").equalsIgnoreCase(json2.getString("url")) && i != j) {
					normalRemove.add(j);
				}
			}

			for(int j = 0;j < preSeTuList.size(); j++) {
				PreSeTu pic2 = preSeTuList.get(j);
				if(json1.getString("url").equalsIgnoreCase(pic2.url) && i != j) {
					preNormalRemove.add(j);
				}
			}
		}

		for(int i = 0;i < r18.size(); i++) {
			JSONObject json1 = r18.getJSONObject(i);
			for(int j = 0;j < r18.size(); j++) {
				JSONObject json2 = r18.getJSONObject(j);
				if(json1.getString("url").equalsIgnoreCase(json2.getString("url")) && i != j) {
					r18Remove.add(j);
				}
			}
			for(int j = 0;j < preSeTuR18List.size(); j++) {
				PreSeTu pic2 = preSeTuR18List.get(j);
				if(json1.getString("url").equalsIgnoreCase(pic2.url) && i != j) {
					preR18Remove.add(j);
				}
			}
		}
		
		for(int i = 0;i < preSeTuList.size(); i++) {
			PreSeTu pic1 = preSeTuList.get(i);
			for(int j = 0;j < preSeTuList.size(); j++) {
				PreSeTu pic2 = preSeTuList.get(j);
				if(pic1.url.equalsIgnoreCase(pic2.url) && i != j) {
					if(!preNormalRemove.contains(j))
						preNormalRemove.add(j);
				}
			}
		}

		for(int i = 0;i < preSeTuR18List.size(); i++) {
			PreSeTu pic1 = preSeTuR18List.get(i);
			for(int j = 0;j < preSeTuR18List.size(); j++) {
				PreSeTu pic2 = preSeTuR18List.get(j);
				if(pic1.url.equalsIgnoreCase(pic2.url) && i != j) {
					if(!preR18Remove.contains(j))
						preR18Remove.add(j);
				}
			}
		}
		Collections.sort(normalRemove, descComparator);  
		Collections.sort(preNormalRemove, descComparator);  
		Collections.sort(r18Remove, descComparator);  
		Collections.sort(preR18Remove, descComparator);  

		for(int i : normalRemove) normal.remove(i);
		for(int i : preNormalRemove) preSeTuList.remove(i);
		for(int i : r18Remove) r18.remove(i);
		for(int i : preR18Remove) preSeTuR18List.remove(i);
		main.getLogger().info("????????????????????????????");
		main.getLogger().info("???? | N: -" + normalRemove + ", R: -" + r18Remove);
		main.getLogger().info("?????? | N: -" + preNormalRemove + ", R: -" + preR18Remove);
	}
	
	public class PreSeTu {
		int uid;
		String author;
		String pid;
		String url;
		String tags;
		public PreSeTu(int uid, String author, String pid, String url, JSONArray tags){
			this.uid = uid;
			this.author = author;
			this.pid = pid;
			this.url = url;
			this.tags = "";
			for(int i = 0; i< tags.size();i++) {
				this.tags = this.tags + tags.getString(i) + (i!=tags.size()-1?",":"");
			}
		}

		public PreSeTu(int uid, String author, String pid, String url, String tags){
			this.uid = uid;
			this.author = author;
			this.pid = pid;
			this.url = url;
			this.tags = tags;
		}
		public JSONObject getJSON() {
			JSONObject json = new JSONObject();
			json.put("uid", uid);
			json.put("author", author);
			json.put("pid", pid);
			json.put("url", url);
			json.put("tags", tags);
			return json;
		}
	}

	List<PreSeTu> preSeTuList = new ArrayList<PreSeTu>();
	List<PreSeTu> preSeTuR18List = new ArrayList<PreSeTu>();
	File seTuListFile;
	File dataFile;
	private String getApiURL() {
		return "https://api.lolicon.app/setu/?apikey="+main.getConfig().getJSON().getString("lolicon-APIKEY")+"&r18=2&num=10";
	}
	
	public void saveSeTuList() {
		JSONObject finalJson = new JSONObject();

		JSONArray normalJson = new JSONArray();
		JSONArray r18Json = new JSONArray();

		for(int i = 0;i<preSeTuList.size();i++) {
			PreSeTu picNormal = preSeTuList.get(i);
			normalJson.add(picNormal.getJSON());
		}
		for(int i = 0;i<preSeTuR18List.size();i++) {
			PreSeTu picR18 = preSeTuR18List.get(i);
			r18Json.add(picR18.getJSON());
		}
		finalJson.put("normal", normalJson);
		finalJson.put("R18", r18Json);
		Util.writeFile(seTuListFile, JSONObject.toJSONString(finalJson, true));

		main.getLogger().info("??????????????????");
	}
	
	public void loadLocalSeTu() {
		if(seTuListFile.exists()) {
			main.getLogger().info("????????????????????????????");
			JSONObject json = JSONObject.parseObject(Util.readFile(seTuListFile));
			if(json.containsKey("normal")) {
				JSONArray normalList = json.getJSONArray("normal");
				for(int i = 0; i < normalList.size(); i++) {
					JSONObject j = normalList.getJSONObject(i);
					int uid = j.getInteger("uid");
					String author = j.getString("author");
					String pid = j.getString("pid");
					String url = j.getString("url");
					String tags = j.getString("tags");
					PreSeTu setu = new PreSeTu(uid,author,pid,url,tags);
					boolean addToList = true;
					for(PreSeTu st : preSeTuList) {
						if(st.url.equalsIgnoreCase(url)) {
							addToList = false;
							break;
						}
					}
					if(addToList)
						preSeTuList.add(setu);
				}
			}
			if(json.containsKey("R18")) {
				JSONArray r18List = json.getJSONArray("R18");
				for(int i = 0; i < r18List.size(); i++) {
					JSONObject j = r18List.getJSONObject(i);
					int uid = j.getInteger("uid");
					String author = j.getString("author");
					String pid = j.getString("pid");
					String url = j.getString("url");
					String tags = j.getString("tags");
					PreSeTu setu = new PreSeTu(uid,author,pid,url,tags);

					boolean addToList = true;
					for(PreSeTu st : preSeTuR18List) {
						if(st.url.equalsIgnoreCase(url)) {
							addToList = false;
							break;
						}
					}
					if(addToList)
						preSeTuR18List.add(setu);
				}
			}
		}else {
			main.getLogger().info("??????????????????????????????????");
		}
	}
	
	/*
	 *???????????? 
	 */
	@SuppressWarnings("unused")
	public void updateSeTu() {

		main.getLogger().info("??????????????????????");
		Calendar now = Calendar.getInstance();
		long nowtime = now.get(Calendar.HOUR_OF_DAY) * 3600 + now.get(Calendar.MINUTE) * 60
				+ now.get(Calendar.SECOND);
		
		String jsonString = Util.sendGet(this.getApiURL());
		JSONObject json = JSONObject.parseObject(jsonString);
		int code = json.getInteger("code");
		String msg = json.getString("msg");
		int quota = json.getInteger("quota");
		int quota_min_ttl = json.getInteger("quota_min_ttl");
		int count = json.getInteger("count");
		JSONArray data = json.getJSONArray("data");

		main.getLogger().info("??????????????????: " + quota + ", ????????(??): " + quota_min_ttl + ", ????????????????: " + count);
		// ????
		if(code == 0) {
			for(int i = 0;i< data.size();i++) {
				JSONObject pic = data.getJSONObject(i);
				boolean r18 = pic.getBoolean("r18");
				int pid = pic.getInteger("pid");
				int p = pic.getInteger("p");
				int uid = pic.getInteger("uid");
				String title = pic.getString("title");
				String author = pic.getString("author");
				String url = pic.getString("url");
				int width = pic.getInteger("width");
				int height = pic.getInteger("height");
				JSONArray tags = pic.getJSONArray("tags");
				PreSeTu setu = new PreSeTu(uid, author, String.valueOf(pid), url, tags);
				if(!r18) {
					boolean addTo = true;
					for(int j = 0;j < normal.size(); j++) {
						JSONObject json1 = normal.getJSONObject(j);
						if(json1.getString("url").equalsIgnoreCase(setu.url)) {
							addTo = false;
							break;
						}
					}
					if(addTo) {
						preSeTuList.add(setu);
					}
				}
				else {
					boolean addTo = true;
					for(int j = 0;j < this.r18.size(); j++) {
						JSONObject json1 = this.r18.getJSONObject(j);
						if(json1.getString("url").equalsIgnoreCase(setu.url)) {
							addTo = false;
							break;
						}
					}
					if(addTo) {
						preSeTuR18List.add(setu);
					}
				}
				if(r18) {
					preSeTuR18List.add(setu);
				}
				else {
					preSeTuList.add(setu);
				}
			}
		}
		// APIKEY ??????????????
		else if (code == 401) {
			isbanned = true;
			Bot b = Bot.getInstanceOrNull(3191684705L);
			if (b!=null) b.getFriend(2431208142L).sendMessage("ACG ?? APIKEY ??????????????????????????");
		}
		// ????????????????????????????
		else if (code == 403) {
			return;
		}
		// ??????????????????????
		else if (code == 404) {
			return;
		}
		// ????????????????
		else if (code == 429) {
			nextDayCheck = true;
			nextCheckUpdateTime = 86401;
		}
		// ????????????????????????????
		else if (code == -1) {
			Bot b = Bot.getInstanceOrNull(3191684705L);
			if (b!=null) b.getFriend(2431208142L).sendMessage("ACG ??????????????????????????????\n"
					+ "code: " + code +"\n"
					+ "msg: " + msg + "\n"
					+ "quota: " + quota + "\n"
					+ "quota_min_ttl: " + quota_min_ttl + "\n"
					+ "count: " + count);
		}
	}
	
	void initPictureList() {
		try {
			String dataString = "{}";
			if(!dataFile.exists()) {
				String path = "data.json";
				InputStream is = this.getClass().getClassLoader().getResourceAsStream(path);
				BufferedReader in = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
				StringBuffer buffer = new StringBuffer();
				String line = "";
				while ((line = in.readLine()) != null) {
					buffer.append(line + "\r\n");
				}
				dataString = buffer.toString();
				Util.writeFile(dataFile, buffer.toString());
			}
			else {
				dataString = Util.readFile(dataFile);
			}
			JSONObject data = JSONObject.parseObject(dataString);
			normal = data.getJSONArray("normal");
			r18 = data.getJSONArray("R18");
			init = true;
		} catch (Throwable t) {
			t.printStackTrace();
			init = false;
		}
	}

	int success = 0;
	int fail = 0;

	void addSucess() {
		success++;
	}

	void addFail() {
		fail++;
	}

	public void initStats() {
		success = 0;
		fail = 0;
	}

	@SuppressWarnings("unused")
	private boolean isOutOfDate(File file) {
		try {
			BasicFileAttributes attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
			LocalDateTime fileCreationTime = LocalDateTime.ofInstant(attributes.creationTime().toInstant(),
					ZoneId.systemDefault());
			Calendar now = Calendar.getInstance();
			if (now.get(Calendar.DAY_OF_MONTH) - 3 < 0) {// ?????????????? - 3 ????0
				return false; // ??????
			}
			// ???????????? - 3 ????????????
			else if (now.get(Calendar.DAY_OF_MONTH) - 3 > fileCreationTime.getDayOfMonth()) {
				return true; // ??????
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return false; // ??????????????????
	}
	// Command /acg
	public void onCommand(GroupMessageEvent event) {
		if(main.getConfig().isBlackList(event.getSender().getId())) return;
		
		main.getScheduler().async(() -> {
			Calendar now = Calendar.getInstance();
			long nowtime = now.get(Calendar.HOUR_OF_DAY) * 3600 + now.get(Calendar.MINUTE) * 60
					+ now.get(Calendar.SECOND);
			
		String msg = event.getMessage().contentToString();
		// boolean checkCache = false;
		QuoteReply quote = new QuoteReply(event.getSource());
		if (msg.contains(" ")) {
			String temp = msg;
			while (temp.contains("  ")) {
				temp = temp.replace("  ", " ");
			}
			String[] args = temp.split(" ");
			boolean admin = managers.contains(event.getSender().getId())
					|| event.getSender().getPermission() == MemberPermission.ADMINISTRATOR
					|| event.getSender().getPermission() == MemberPermission.OWNER;
	    	if(admin) {
	    		if(temp.equalsIgnoreCase("/acg switch")) {
	    			if(enable) {
	    				enable=false;
	    				event.getGroup().sendMessage(quote.plus("[acg] ????????????"));
	    			}
	    			else {
	    				enable=true;
	    				event.getGroup().sendMessage(quote.plus("[acg] ????????????"));
	    			}
	    			return;
	    		}
	    	}
		    if (!enable) {
		
		    	event.getGroup().sendMessage(quote.plus("[acg] ????????????????????\n"
		    			+ "???????????????? /acg switch ??????"));
		    	return;
		    }
			if (args.length == 2) {
				if (!args[0].equalsIgnoreCase("/acg")) {
					return;
				}
				if (args[1].equalsIgnoreCase("reload") && event.getSender().getId() == 2431208142L) {
					event.getGroup().sendMessage(quote.plus("[acg] ??????????????????????????????????"));
					
					this.preSeTuList.clear();
					this.preSeTuR18List.clear();
					this.initPictureList();
					this.loadLocalSeTu();
					this.clearRepetitive();
					this.saveSeTuList();
				}
				if (!init) {
					event.getGroup().sendMessage(quote.plus("????????????????????????????????"));
					return;
				}

				if (args[1].equalsIgnoreCase("forceupdate") && event.getSender().getId() == 2431208142L) {

					updateSeTu();
					saveSeTuList();
					
					String message = "[acg] ??????????????????????????????????????";

					event.getGroup().sendMessage(quote.plus(message));
					return;
				}
				if (args[1].equalsIgnoreCase("update")) {
					String message = "[acg] ??????????????????????:\n"
							+ "N: " + preSeTuList.size() +"??\n"
							+ "R: " + preSeTuR18List.size() + "??\n"
							+ "????????????????????????????:\n"
							+ "N: " + normal.size() +"??\n"
							+ "R: " + r18.size() + "??\n"
							+ "??????????????????: " + (isbanned ? "????????????????" : (nextDayCheck ? "????" : String.valueOf(nextCheckUpdateTime - nowtime + "??")) + "\n"
							+ "Power by Lolicon");

					event.getGroup().sendMessage(quote.plus(message));
					return;
				}
				if (args[1].equalsIgnoreCase("stats")) {
					String message = "[acg] ????????????????????????:\n" + "(????????????????0????????)";

					message += "\n????:" + success + ", ????:" + fail;
					event.getGroup().sendMessage(quote.plus(message));
					return;
				}
				if (args[1].equalsIgnoreCase("normal")) {
					if ((nexttime - nowtime) > 32) {
						nexttime = 0;
						//event.getGroup().sendMessage(" [acg] ????????????????????????????????????");
					}
					if (nowtime >= nexttime || admin) {
						if (!admin) {
							nexttime = nowtime + 32;
							if (nexttime >= 86400) {
								nexttime = 0;
							}
						}
						String pid = "null";
						
						event.getGroup().sendMessage(quote.plus((admin ? "??????bypass??????????????????\n" : "\n") + "?????????????? ??????"));
						
						try {
							InputStream image_url = null;
							// TODO ?????????? R18 ????
							boolean R18 = false;
                            if (new Random().nextInt(1000) < 10) {
                            	R18=true;
                            }
							if (R18) {
								// ????: FlashImage.from(event.getGroup().uploadImage(inputStream))
								// ????: event.getGroup().sendMessage(message).recallIn(10 * 1000L);

								JSONObject pic_data = r18.getJSONObject(new Random().nextInt(r18.size()));
								pid = pic_data.getString("pid");
								String imgurl = pic_data.getString("url");
								HttpURLConnection conn = null;
								// ????????
								URL httpUrl = new URL(imgurl);
								conn = (HttpURLConnection) httpUrl.openConnection();
								conn.setRequestProperty("user-agent",
										"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36 Edg/85.0.564.51");

								// ????get????
								conn.setRequestMethod("GET");

								conn.setDoInput(true);
								conn.setDoOutput(true);
								conn.connect();
								// ??????????????
								image_url = conn.getInputStream();
								// ????????
								FlashImage image = FlashImage.from(event.getGroup().uploadImage(ExternalResource.create(image_url)));
								// 10 ????????
								event.getGroup().sendMessage(quote.plus(image)).recallIn(10 * 1000L);
								this.addSucess();
							} else {
								JSONObject pic_data = normal.getJSONObject(new Random().nextInt(normal.size()));
								pid = pic_data.getString("pid");
								String imgurl = pic_data.getString("url");
								HttpURLConnection conn = null;
								// ????????
								URL httpUrl = new URL(imgurl);
								conn = (HttpURLConnection) httpUrl.openConnection();
								conn.setRequestProperty("user-agent",
										"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36 Edg/85.0.564.51");

								// ????get????
								conn.setRequestMethod("GET");

								conn.setDoInput(true);
								conn.setDoOutput(true);
								conn.connect();
								// ??????????????
								image_url = conn.getInputStream();
								Image image = event.getGroup().uploadImage(ExternalResource.create(image_url));

								event.getGroup().sendMessage(image);
								
								event.getGroup()
										.sendMessage(quote.plus("????: " + pic_data.getString("author") + "\n" + "Pid: "
												+ pic_data.getString("pid") + "\n" + "Tags: "
												+ pic_data.getString("tags")));
								this.addSucess();
							}
						} catch (Throwable t) {
							String s = t.toString();
							if (s.contains("timed out")) {
								s = "????????";
							} else if (t.toString().contains("403")) {
								s = "???????????????????? (HTTP Code: 403)\n????????????????????????????";
							} else if (t.toString().contains("404") || t instanceof java.io.FileNotFoundException) {
								s = "???????????????????? (HTTP Code: 404)";
							}
							event.getGroup().sendMessage(
									quote.plus(" [acg] ??????????????????????????????: \n" + s + "\n????????pid: " + pid));
							t.printStackTrace();

							this.addFail();
							nexttime=0;
						}
					} else {
						event.getGroup().sendMessage(quote.plus(" [acg] ????????????????\n???? " + (nexttime - nowtime) + " ??"));
					}
					// event.getGroup().sendMessage(quote.plus(message));
					return;
				}
				/**/
			}
			if (args.length == 3) {
				if (args[1].equalsIgnoreCase("tag")) {
					if ((nexttime - nowtime) > 32) {
						nexttime = 0;
						event.getGroup().sendMessage(" [acg] ????????????????????????????????????");
					}
					if (nowtime >= nexttime || admin) {
						if (!admin) {
							nexttime = nowtime + 32;
							if (nexttime >= 86400) {
								nexttime = 0;
							}
						}
						String pid = "null";

						String tag = args[2];
						boolean a = false;
						if (tag.trim() != "") {
							List<JSONObject> result = new ArrayList<JSONObject>();
							for (int i = 0; i < normal.size(); i++) {
								JSONObject data = normal.getJSONObject(i);
								String tags_str = data.getString("tags");
								if (tags_str.contains(",")) {
									String[] tags = tags_str.split(",");
									for (String s : tags) {
										if (s.contains(tag)) {
											result.add(data);
											a = true;
											break;
										}
									}
								}
							}
							if (!a) {
								event.getGroup().sendMessage(quote.plus("????????????????????????tag"));
								nexttime=0;
								return;
							}
							event.getGroup().sendMessage(quote.plus(
									(admin ? "??????bypass??????????????????\n" : "\n") + "?????????????? ??????"));

							JSONObject pic = result.get(new Random().nextInt(result.size()));
							pid = pic.getString("pid");
							try {
								InputStream image_url = null;
								String imgurl = pic.getString("url");
								HttpURLConnection conn = null;
								// ????????
								URL httpUrl = new URL(imgurl);
								conn = (HttpURLConnection) httpUrl.openConnection();
								conn.setRequestProperty("user-agent",
										"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36 Edg/85.0.564.51");

								// ????get????
								conn.setRequestMethod("GET");

								conn.setDoInput(true);
								conn.setDoOutput(true);
								conn.connect();
								// ??????????????
								image_url = conn.getInputStream();
								Image image = event.getGroup().uploadImage(ExternalResource.create(image_url));
								event.getGroup().sendMessage(quote.plus("????: " + pic.getString("author") + "\n"
										+ "Pid: " + pic.getString("pid") + "\n" + "Tags: " + pic.getString("tags")));
								event.getGroup().sendMessage(image);
								this.addSucess();
							} catch (Throwable t) {
								String s = t.toString();
								if (s.contains("timed out")) {
									s = "????????";
								} else if (t.toString().contains("403")) {
									s = "???????????????????? (HTTP Code: 403)\n????????????????????????????";
								} else if (t.toString().contains("404")|| t instanceof java.io.FileNotFoundException) {
									s = "???????????????????? (HTTP Code: 404)";
								}
								event.getGroup().sendMessage(
										quote.plus(" [acg] ??????????????????????????????: \n" + s + "\n????????pid: " + pid));
								t.printStackTrace();
								this.addFail();
								nexttime = 0;
							}

						} else {
							event.getGroup().sendMessage(quote.plus(" [acg] ????????????"));
							nexttime = 0;
						}
					} else {
						event.getGroup().sendMessage(quote.plus(" [acg] ????????????????\n???? " + (nexttime - nowtime) + " ??"));
					}
				}
			}
		} else {
			if (msg.equalsIgnoreCase("/acg") || msg.toLowerCase().startsWith("/acg ")) {
				if (!init) {
					event.getGroup().sendMessage(quote.plus("????????????????????????????????"));
					return;
				}
				event.getGroup().sendMessage(quote.plus(getHelp()));
			} else
				return;
		}
		});
	}

	private String getHelp() {
		return "   -- Acg ???????? --   \n" 
				+ "  /acg - ??????????????\n" 
				+ "  /acg normal - ????????\n" 
				+ "     [????] 32 ??\n"
				+ "  /acg tag [????] - ????????????????????\n" 
				+ "  /acg stats - ????????????????\n" 
				+ "  /acg update - ????????????????\n" 
				+ " *Acg??????????????\n"
				+ " *??????????Bug????????????????";
	}

}
