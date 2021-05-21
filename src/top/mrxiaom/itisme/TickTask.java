package top.mrxiaom.itisme;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.data.OnlineStatus;
import net.mamoe.mirai.internal.QQAndroidBot;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;
import net.mamoe.mirai.utils.OverFileSizeMaxException;

public class TickTask extends java.util.TimerTask {
	Natsuko main;
	
	public class Task {
		String id;
		long owner;
		long userId;
		int hour;
		int minute;
		int second;
		String subject;
		Runnable runnable;
		public Task(String id,long owner,long userId, int hour, int minute, int second, String subject, Runnable runnable) {
			this.id = id;
			this.owner = owner;
			this.userId = userId;
			this.hour = hour;
			this.minute = minute;
			this.second = second;
			this.subject = subject;
			this.runnable = runnable;
		}
		
		public long getOwner() {
			return owner;
		}
		
		public long getUserId() {
			return userId;
		}
		
		public String getTimeString() {
			return hour + ":" + minute + ":" + second;
		}
		
		public String getSubject() {
			return subject;
		}
		
		public File getSubjectFile() {
			return new File(subjectDir, subject + ".png");
		}
	}
	
	public HashMap<String, Task> tasks;
	File subjectDir;
	public TickTask(Natsuko main) {
		this.main = main;
		tasks = new HashMap<String, Task>();

		subjectDir = new File(main.getDataFolder(), "reCAPTCHA");
		if(!subjectDir.exists()) {
			subjectDir.mkdirs();
		}
	}

	private String getWelcomeMessage() {
		Calendar c = Calendar.getInstance();

		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		if (Natsuko.a) {
			return "" + "欢迎来到生存都市服务器 ------[" + (hour < 10 ? "0" + hour : hour) + ":"
					+ (minute < 10 ? "0" + minute : minute) + "]-" + "\n"

					+ "服务器核心版本: 1.8 | IP: mc.66ko.cc" + "\n" + "「1.8-1.16 的客户端都可以进入服务器哦」" + "\n"
					+ "◇ 客户端在群文件，有什么不懂的在群里问" + "\n" + "◇ 发送“提问的智慧”即可获取提问的技巧" + "\n" + "◇ 发送“问题解答”即可获取解答相关链接" + "\n"
					+ "◇ 你也可以发送 “/help” 来唤出我的菜单" + "\n";
		} else {
			return "" + "欢迎来到生存都市服务器 ------[" + (hour < 10 ? "0" + hour : hour) + ":"
					+ (minute < 10 ? "0" + minute : minute) + "]-" + "\n"

					+ "服务器当前已经关闭，请等待下一周目 :-(" + "\n" + "「但你目前依然可以使用本机器人」" + "\n" + "◇ 若服务器重新开启，请管理员执行以下命令更改本信息" + "\n"
					+ "/title switch on" + "\n" + "◇ 发送“提问的智慧”即可获取提问的技巧" + "\n" + "◇ 发送“问题解答”即可获取解答相关链接" + "\n"
					+ "◇ 你也可以发送 “/help” 来唤出我的菜单" + "\n";
		}
	}

	int i = 0;
	long lasttime = 0;

	@SuppressWarnings("unused")
	public void run() {
		Calendar c = Calendar.getInstance();// 可以对每个时间域单独修改

		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
		int date = c.get(Calendar.DATE);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		int second = c.get(Calendar.SECOND);

		long nowtime = hour * 3600 + minute * 60 + second;
		if (lasttime == nowtime)
			return;
		lasttime = nowtime;
		
		for(String id : tasks.keySet()) {
			Task task = tasks.get(id);
			if(hour == task.hour && minute == task.hour && second == task.second) {
				task.runnable.run();
				tasks.remove(id);
			}
		}
		
		QQAndroidBot bot = main.bot;
		if (bot == null)
			return;
		
		if(second == 0 && minute % 5 == 0) {
			File tempDir = new File(main.getDataFolder(), "temp");
			if(!tempDir.exists()) tempDir.mkdirs();
			for(File file : tempDir.listFiles()) {
				long lastTime = file.lastModified() / 1000;
				
				long difference = nowtime - lastTime;
				if(difference > 60 || difference < -60) {
					file.delete();
				}
			}
		}
		
		if(hour == 12 && minute == 30 && second == 0) {

			if (bot.getGroups().contains(241973735L)) {
				Group group = bot.getGroup(241973735L);
				try {
					if (group.getBotMuteRemaining() <= 0) {
						String requrl = "http://news.baidu.com/widget?id=AllOtherData&channel=internet&ajax=json";
						JSONObject json = JSONObject.parseObject(Util.sendGet(requrl));
						JSONArray array = json.getJSONObject("data").getJSONObject("AllOtherData").getJSONArray("InstantNews");
						JSONObject news = array.getJSONObject(new Random().nextInt(array.size()));
						String m_url = news.getString("m_url");
						String m_title = news.getString("m_title");
						String m_desc = news.getString("m_text");
						String m_image = news.getString("m_image_url");

						URL imgUrl = new URL(m_image);
						HttpURLConnection conn = (HttpURLConnection) imgUrl.openConnection();
						conn.setConnectTimeout(5000);
						conn.setReadTimeout(60000);
						InputStream image_url = conn.getInputStream();
						ExternalResource image = ExternalResource.create(image_url);
						group.sendMessage(group.uploadImage(image).plus("\n" +
								m_title + "\n" +
								m_desc + "\n源地址: " + m_url + "\n" +
								"\n" +
								"订阅自 百度新闻\n" +
								"Natsuko 将于每天 12:30 发送随机新闻"));
						
						image.close();
						image_url.close();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
		if (minute == 0 && second == 0) {
			if (bot.getGroups().contains(241973735L)) {
				Group group = bot.getGroup(241973735L);
				try {
					if (group.getBotMuteRemaining() <= 0) {
						if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY && hour == 21) {
							try {
								File file = new File(main.getDataFolder(), "sunday.png");
								Image image = group.uploadImage(ExternalResource.create(file));
								group.sendMessage(image);
							} catch (OverFileSizeMaxException e) {
								group.sendMessage("我的美丽星期天只剩下三个小时了");
								e.printStackTrace();
							}
						} else {
							if (hour == 0) {
								main.wallpaper.initStats();
								main.acgimg.initStats();
							}
							group.sendMessage(this.getWelcomeMessage() + (hour == 0
									? ("* 今日壁纸接口使用统计数据已重置" + "\n" + "\n喜欢本机器人吗? 来赞助我们吧!" + "\n" + "你可以选择到「爱发电」直接打钱:"
											+ "\n" + "https://afdian.net/@mrxiaom" + "\n" + "或者到「哔哩哔哩」给我的视频点赞、投币、收藏、分享，"
											+ "\n" + "以增加我的创作激励收入:" + "\n" + "https://space.bilibili.com/330771760")
									: ""));
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		if (main.bot.getClient().getOnlineStatus() != OnlineStatus.Q_ME) {
			main.bot.getClient().setOnlineStatus(OnlineStatus.Q_ME);
		}
	}
}
