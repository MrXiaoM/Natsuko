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
			return "" + "��ӭ�������涼�з����� ------[" + (hour < 10 ? "0" + hour : hour) + ":"
					+ (minute < 10 ? "0" + minute : minute) + "]-" + "\n"

					+ "���������İ汾: 1.8 | IP: mc.66ko.cc" + "\n" + "��1.8-1.16 �Ŀͻ��˶����Խ��������Ŷ��" + "\n"
					+ "�� �ͻ�����Ⱥ�ļ�����ʲô��������Ⱥ����" + "\n" + "�� ���͡����ʵ��ǻۡ����ɻ�ȡ���ʵļ���" + "\n" + "�� ���͡������𡱼��ɻ�ȡ����������" + "\n"
					+ "�� ��Ҳ���Է��� ��/help�� �������ҵĲ˵�" + "\n";
		} else {
			return "" + "��ӭ�������涼�з����� ------[" + (hour < 10 ? "0" + hour : hour) + ":"
					+ (minute < 10 ? "0" + minute : minute) + "]-" + "\n"

					+ "��������ǰ�Ѿ��رգ���ȴ���һ��Ŀ :-(" + "\n" + "������Ŀǰ��Ȼ����ʹ�ñ������ˡ�" + "\n" + "�� �����������¿����������Աִ������������ı���Ϣ" + "\n"
					+ "/title switch on" + "\n" + "�� ���͡����ʵ��ǻۡ����ɻ�ȡ���ʵļ���" + "\n" + "�� ���͡������𡱼��ɻ�ȡ����������" + "\n"
					+ "�� ��Ҳ���Է��� ��/help�� �������ҵĲ˵�" + "\n";
		}
	}

	int i = 0;
	long lasttime = 0;

	@SuppressWarnings("unused")
	public void run() {
		Calendar c = Calendar.getInstance();// ���Զ�ÿ��ʱ���򵥶��޸�

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
								m_desc + "\nԴ��ַ: " + m_url + "\n" +
								"\n" +
								"������ �ٶ�����\n" +
								"Natsuko ����ÿ�� 12:30 �����������"));
						
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
								group.sendMessage("�ҵ�����������ֻʣ������Сʱ��");
								e.printStackTrace();
							}
						} else {
							if (hour == 0) {
								main.wallpaper.initStats();
								main.acgimg.initStats();
							}
							group.sendMessage(this.getWelcomeMessage() + (hour == 0
									? ("* ���ձ�ֽ�ӿ�ʹ��ͳ������������" + "\n" + "\nϲ������������? ���������ǰ�!" + "\n" + "�����ѡ�񵽡������硹ֱ�Ӵ�Ǯ:"
											+ "\n" + "https://afdian.net/@mrxiaom" + "\n" + "���ߵ����������������ҵ���Ƶ���ޡ�Ͷ�ҡ��ղء�����"
											+ "\n" + "�������ҵĴ�����������:" + "\n" + "https://space.bilibili.com/330771760")
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
