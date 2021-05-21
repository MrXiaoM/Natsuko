package top.mrxiaom.itisme.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.message.data.SingleMessage;
import net.mamoe.mirai.utils.ExternalResource;
import top.mrxiaom.itisme.Natsuko;
import top.mrxiaom.itisme.Util;
import top.mrxiaom.miraiutils.CommandModel;
import top.mrxiaom.miraiutils.CommandSender;
import top.mrxiaom.miraiutils.CommandSenderGroup;

public class Wallpaper extends CommandModel{

	Natsuko main;
	public List<String> apis = new ArrayList<String>();
	public List<Long> managers = new ArrayList<Long>();
	long nexttime = 0;
	public Wallpaper(Natsuko main) {
		super("wallpaper");
		this.main = main;
				
		apis.add("https://api.vc.bilibili.com/link_draw/v1/doc/doc_list?uid=6823116&page_num=0&page_size=300&biz=all");
		apis.add("https://api.vc.bilibili.com/link_draw/v1/doc/doc_list?uid=168687092&page_num=0&page_size=300&biz=all");
		apis.add("https://api.vc.bilibili.com/link_draw/v2/Doc/list?category=illustration&type=hot&page_num=$num&page_size=300");

		initStats();
		managers.add(2431208142L);
		managers.add(2406120646L);
	}
	HashMap<Integer, Integer> success = new HashMap<Integer, Integer>();
	HashMap<Integer, Integer> fail = new HashMap<Integer, Integer>();
	void addSucess(int index) {
		if(success.containsKey(index)) {
			success.put(index, success.get(index) + 1);
		}
		else {
			success.put(index, 1);
		}
	}

	void addFail(int index) {
		if(fail.containsKey(index)) {
			fail.put(index, fail.get(index) + 1);
		}
		else {
			fail.put(index, 1);
		}
	}

	public void initStats() {
		success = new HashMap<Integer, Integer>();
		fail = new HashMap<Integer, Integer>();
		success.clear();
		fail.clear();
		for (int i = 0; i < apis.size(); i++) {
			success.put(i, 0);
			fail.put(i, 0);
		}
	}

	@SuppressWarnings("unused")
	private boolean isOutOfDate(File file) {
		try {
			BasicFileAttributes attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
			LocalDateTime fileCreationTime = LocalDateTime.ofInstant(attributes.creationTime().toInstant(),
					ZoneId.systemDefault());
			Calendar now = Calendar.getInstance();
			if (now.get(Calendar.DAY_OF_MONTH) - 3 < 0) {// ������ڵ����� - 3 С��0
				return false; // δ����
			}
			// ����������� - 3 ���ڴ�������
			else if (now.get(Calendar.DAY_OF_MONTH) - 3 > fileCreationTime.getDayOfMonth()) {
				return true; // �ѹ���
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return false; // δ�������Ϊδ����
	}

	@Override
	public void onCommand(CommandSender sender, SingleMessage[] args) {
		if(this.main.getConfig().isBlackList(sender.getSenderID())) return;
		if(!(sender instanceof CommandSenderGroup)) return;
		CommandSenderGroup senderGroup = (CommandSenderGroup) sender;
		Group group = senderGroup.getGroup();
		Member member = senderGroup.getMember();
		QuoteReply quote = new QuoteReply(senderGroup.getMessageSource());
		boolean ok = false;
		int index1 = new Random().nextInt(apis.size());
		// boolean checkCache = false;
			if (args.length == 1) {
				if(args[0].contentToString().equalsIgnoreCase("about")) {
					group.sendMessage(quote.plus("[wallpaper] �������˽ӿ�ʹ����������:\n"
							+ "[0] https://space.bilibili.com/6823116\n"
							+ "[1] https://space.bilibili.com/168687092\n"
							+ "[2] https://h.bilibili.com/eden/draw_area\n"
							+ "��л��ҵ�֧��"
							));
					return;
				}
				
				if (args[0].contentToString().equalsIgnoreCase("stats")) {
					String message = "[wallpaper] ���յĻ����˱�ֽ��ȡ״̬:\n"
							+ "(������������ÿ��0�������)";
					int success = 0;
					int fail = 0;
					
					for(int i = 0; i < apis.size(); i++) {
						if(this.success.containsKey(i))
						success += this.success.get(i);
						if(this.fail.containsKey(i))
						fail += this.fail.get(i);
						message += "\n" + "  �ӿ�["+i+"] "
								+ "�ɹ�:"+  (this.success.containsKey(i)?this.success.get(i):0) + ", ʧ��:" + (this.fail.containsKey(i)?this.fail.get(i):0);
					}
					message += "\n[�ܹ�] �ɹ�:" + success + ", ʧ��:" + fail;
					group.sendMessage(quote.plus(message));
					return;
				}
				
				index1 = Util.strToInt(args[0].contentToString(), -1);
				if (index1 >= 0 && index1 < apis.size()) {
					ok = true;
				}
				/**/
			}

		final int newindex = index1;

		if (ok) {
			Calendar now = Calendar.getInstance();
			long nowtime = now.get(Calendar.HOUR_OF_DAY) * 3600 + now.get(Calendar.MINUTE) * 60
					+ now.get(Calendar.SECOND);
			if ((nexttime - nowtime) > 30) {
				nexttime = 0;
				//event.getGroup().sendMessage(" [wallpaper] ��ȴʱ������쳣�������ý�����ȴʱ��");
			}
			boolean admin = managers.contains(member.getId())
					|| member.getPermission() == MemberPermission.ADMINISTRATOR
					|| member.getPermission() == MemberPermission.OWNER;
			if (nowtime >= nexttime || admin) {
				if (!admin) {
					nexttime = nowtime + 30;
					if (nexttime >= 86400) {
						nexttime = 0;
					}
				}
				main.getScheduler().async(() -> {
					int index = newindex;
					// Pixivic �ӿڵ�ַ: https://api.pixivic.com/illusts/[ͼƬID]
					// ͼƬ��ַ data -> imageUrls -> �ڼ���ͼƬ -> ����
					String apiName = "" + index;
					String extra = "";
					if (index == 0) {
						apiName = "bilibili��ֽ��";
					}
					if (index == 1) {
						apiName = "����__jpg";
					}
					if (index == 2) {
						apiName = "bվ�廭";
					}
					group.sendMessage(quote
							.plus((admin ? "��ӵ��bypassȨ�ޣ�����ȴʱ�䡿\n" : "\n") + "����ʹ�ýӿ�[" + apiName + "]��ȡ�� ���Ե�" + extra));
					// boolean flag1 = apis.get(index).equalsIgnoreCase("pixivic")
					// || apis.get(index).equalsIgnoreCase("pixivic1")
					// || apis.get(index).equalsIgnoreCase("pixivic2");
					boolean flag2 = (index == 0) || (index == 1) || (index == 2);
					InputStream image_url = null;
					String extra_msg = "";
					//////////// bilibili �ӿ� ////////////////
					if (flag2) {
						int dynamic_id = 0;
						try {
							if(index == 2) { // bվ�廭
								String url = apis.get(index).replace("$num", ""+new Random().nextInt(15));
								String j = Util.sendGet(url);
								// System.out.println(j);
								JSONObject json = JSONObject.parseObject(j);

								JSONArray items = json.getJSONObject("data").getJSONArray("items");
								if(items.size() == 0) {
									nexttime = nowtime + 5;
									if (nexttime >= 86400) {
										nexttime = 0;
									}
									group.sendMessage(quote.plus("\n" + "ͼ���л�û�������ء�"));
									return null;
								}
								JSONObject one = items.getJSONObject(new Random().nextInt(items.size()));
								JSONObject user = one.getJSONObject("user");
								JSONObject item = one.getJSONObject("item");
								
								dynamic_id = item.getInteger("doc_id");
								String title = item.getString("title");
								String author = user.getString("name");
								JSONArray pictures = item.getJSONArray("pictures");
								JSONObject imageUrls = pictures.getJSONObject(
										new Random().nextInt(pictures.size()));
								String imgurl = imageUrls.getString("img_src");
								extra_msg = "\n-- " + apiName + "\n" + title + "\n"
									+ "��ʦ: " + author + "\n"
									+ "��̬��ַ : https://h.bilibili.com/" + dynamic_id + "\n"
									+ "ͼƬ����: " + pictures.size() + "��";
								System.out.println("ͼƬ��ַ: " + imgurl);
								HttpURLConnection conn = null;
								// ��������
								URL httpUrl = new URL(imgurl);
								conn = (HttpURLConnection) httpUrl.openConnection();
								// Ĭ��get��ʽ
								conn.setRequestMethod("GET");
								// bվ�Ŀ��Žӿ�Ҫɶ referer������Ҫ��
								// conn.setRequestProperty("referer", "https://pixivic.com/illusts/" + pid +
								// "");

								conn.setDoInput(true);
								conn.setDoOutput(true);
								conn.connect();
								// ��ȡ����������
								image_url = conn.getInputStream();
							}
							else { // ��ֽ�� | ����
								String url = apis.get(index);
								String j = Util.sendGet(url, 0);
								//System.out.println(j);
								JSONObject json = JSONObject.parseObject(j);
							
								if (!json.containsKey("data")) {
									nexttime = nowtime + 5;
									if (nexttime >= 86400) {
										nexttime = 0;
									}
									group.sendMessage(quote.plus("\n" + "ͼ���л�û�������ء�"));
									return null;
								}
								if (!json.getJSONObject("data").containsKey("items")) {
									nexttime = nowtime + 5;
									if (nexttime >= 86400) {
										nexttime = 0;
									}
									group.sendMessage(quote.plus("\n" + "ͼ���л�û�������ء�"));
									return null;
								}

								JSONArray items = json.getJSONObject("data").getJSONArray("items");
								JSONObject one = items.getJSONObject(new Random().nextInt(items.size()));

								//if (index == 1 && !one.getString("description").contains("#")) {
								//	event.getGroup().sendMessage(quote.plus("\n" + "��ȡ�ɹ�����û��ɬͼ��������"));
								//	nexttime = nowtime;
								//	return null;
								//}
								dynamic_id = one.getInteger("doc_id");
								JSONObject imageUrls = one.getJSONArray("pictures")
									.getJSONObject(new Random().nextInt(one.getJSONArray("pictures").size()));
								String imgurl = imageUrls.getString("img_src");
								extra_msg = "\n-- " + apiName + "\n" + one.getString("description")
									+ "\n��̬��ַ : https://h.bilibili.com/" + dynamic_id + "\nͼƬ����: " + one.getInteger("count")
									+ "��";
								System.out.println("ͼƬ��ַ: " + imgurl);
								HttpURLConnection conn = null;
								// ��������
								URL httpUrl = new URL(imgurl);
								conn = (HttpURLConnection) httpUrl.openConnection();
								// Ĭ��get��ʽ
								conn.setRequestMethod("GET");
								// bվ�Ŀ��Žӿ�Ҫɶ referer������Ҫ��
								// conn.setRequestProperty("referer", "https://pixivic.com/illusts/" + pid +
								// "");

								conn.setDoInput(true);
								conn.setDoOutput(true);
								conn.connect();
								// ��ȡ����������
								image_url = conn.getInputStream();
							}
						} catch (Exception e) {
							String s = e.toString();
							if(e instanceof java.io.FileNotFoundException || e.toString().contains("404")) {
								image_url = new FileInputStream(new File(main.getDataFolder(), "404.jpg"));
							}
							else {
								if (e.toString().contains("403")) {
									image_url = new FileInputStream(new File(main.getDataFolder(), "403.jpg"));
								}
								else {
									if (s.contains("timed out")) {
										s = "���ӳ�ʱ";
									}
									group.sendMessage(quote.plus(" [wallpaper] �����ǻ�ȡͼƬʱ������һ���쳣: \n" + s + "\n\n��̬��ַ : "
											+ (dynamic_id == 0 ? "��ȡʧ��" : ("https://h.bilibili.com/" + dynamic_id))));
									e.printStackTrace();
									nexttime = nowtime + 5;
									if (nexttime >= 86400) {
										nexttime = 0;
									}
									this.addFail(index);
								
									return null;
								}
							}
						}
					}
					try {
						String url = Util.getNewLocation(apis.get(index));
						if (!flag2) {
							System.out.println("ͼƬ��ַ: " + url);
							if (url.startsWith("//")) {
								url = "https:" + url;
							}
							URL imgUrl = new URL(url);
							HttpURLConnection conn = (HttpURLConnection) imgUrl.openConnection();
							conn.setConnectTimeout(5000);
							conn.setReadTimeout(60000);
							image_url = conn.getInputStream();
						}
						Image image = group.uploadImage(ExternalResource.create(image_url));
						Message m = image;
						if (/* flag1|| */flag2)
							m = m.plus(extra_msg);
						group.sendMessage(m);
						this.addSucess(index);

					} catch (Throwable e) {
						group.sendMessage(quote.plus(" [wallpaper] �����Ƿ���ͼƬʱ������һ���쳣: \n" + e.toString()));
						e.printStackTrace();
						nexttime = nowtime + 5;
						if (nexttime >= 86400) {
							nexttime = 0;
						}
						this.addFail(index);
						return null;
					}
					return null;
				});
			} else {
				group.sendMessage(quote.plus(" [wallpaper] ��ȴʱ�仹û��Ŷ\nʣ�� " + (nexttime - nowtime) + " ��"));
			}
		}
		if (!ok /* && !checkCache */) {
			group.sendMessage(quote.plus(" [wallpaper] ������Ч"));
		}
	}
}
