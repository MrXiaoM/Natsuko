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
			if (now.get(Calendar.DAY_OF_MONTH) - 3 < 0) {// 如果现在的天数 - 3 小于0
				return false; // 未过期
			}
			// 如果现在天数 - 3 大于创建天数
			else if (now.get(Calendar.DAY_OF_MONTH) - 3 > fileCreationTime.getDayOfMonth()) {
				return true; // 已过期
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return false; // 未处理均定为未过期
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
					group.sendMessage(quote.plus("[wallpaper] 本机器人接口使用链接如下:\n"
							+ "[0] https://space.bilibili.com/6823116\n"
							+ "[1] https://space.bilibili.com/168687092\n"
							+ "[2] https://h.bilibili.com/eden/draw_area\n"
							+ "感谢大家的支持"
							));
					return;
				}
				
				if (args[0].contentToString().equalsIgnoreCase("stats")) {
					String message = "[wallpaper] 今日的机器人壁纸获取状态:\n"
							+ "(机器人重启或每日0点后清零)";
					int success = 0;
					int fail = 0;
					
					for(int i = 0; i < apis.size(); i++) {
						if(this.success.containsKey(i))
						success += this.success.get(i);
						if(this.fail.containsKey(i))
						fail += this.fail.get(i);
						message += "\n" + "  接口["+i+"] "
								+ "成功:"+  (this.success.containsKey(i)?this.success.get(i):0) + ", 失败:" + (this.fail.containsKey(i)?this.fail.get(i):0);
					}
					message += "\n[总共] 成功:" + success + ", 失败:" + fail;
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
				//event.getGroup().sendMessage(" [wallpaper] 冷却时间出现异常，已重置今日冷却时间");
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
					// Pixivic 接口地址: https://api.pixivic.com/illusts/[图片ID]
					// 图片地址 data -> imageUrls -> 第几张图片 -> 画质
					String apiName = "" + index;
					String extra = "";
					if (index == 0) {
						apiName = "bilibili壁纸娘";
					}
					if (index == 1) {
						apiName = "鸽子__jpg";
					}
					if (index == 2) {
						apiName = "b站插画";
					}
					group.sendMessage(quote
							.plus((admin ? "【拥有bypass权限，免冷却时间】\n" : "\n") + "正在使用接口[" + apiName + "]获取… 请稍等" + extra));
					// boolean flag1 = apis.get(index).equalsIgnoreCase("pixivic")
					// || apis.get(index).equalsIgnoreCase("pixivic1")
					// || apis.get(index).equalsIgnoreCase("pixivic2");
					boolean flag2 = (index == 0) || (index == 1) || (index == 2);
					InputStream image_url = null;
					String extra_msg = "";
					//////////// bilibili 接口 ////////////////
					if (flag2) {
						int dynamic_id = 0;
						try {
							if(index == 2) { // b站插画
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
									group.sendMessage(quote.plus("\n" + "图库中还没有内容呢…"));
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
									+ "画师: " + author + "\n"
									+ "动态地址 : https://h.bilibili.com/" + dynamic_id + "\n"
									+ "图片总数: " + pictures.size() + "张";
								System.out.println("图片地址: " + imgurl);
								HttpURLConnection conn = null;
								// 建立链接
								URL httpUrl = new URL(imgurl);
								conn = (HttpURLConnection) httpUrl.openConnection();
								// 默认get方式
								conn.setRequestMethod("GET");
								// b站的开放接口要啥 referer，不需要的
								// conn.setRequestProperty("referer", "https://pixivic.com/illusts/" + pid +
								// "");

								conn.setDoInput(true);
								conn.setDoOutput(true);
								conn.connect();
								// 获取网络输入流
								image_url = conn.getInputStream();
							}
							else { // 壁纸娘 | 鸽子
								String url = apis.get(index);
								String j = Util.sendGet(url, 0);
								//System.out.println(j);
								JSONObject json = JSONObject.parseObject(j);
							
								if (!json.containsKey("data")) {
									nexttime = nowtime + 5;
									if (nexttime >= 86400) {
										nexttime = 0;
									}
									group.sendMessage(quote.plus("\n" + "图库中还没有内容呢…"));
									return null;
								}
								if (!json.getJSONObject("data").containsKey("items")) {
									nexttime = nowtime + 5;
									if (nexttime >= 86400) {
										nexttime = 0;
									}
									group.sendMessage(quote.plus("\n" + "图库中还没有内容呢…"));
									return null;
								}

								JSONArray items = json.getJSONObject("data").getJSONArray("items");
								JSONObject one = items.getJSONObject(new Random().nextInt(items.size()));

								//if (index == 1 && !one.getString("description").contains("#")) {
								//	event.getGroup().sendMessage(quote.plus("\n" + "获取成功，但没有涩图，请重试"));
								//	nexttime = nowtime;
								//	return null;
								//}
								dynamic_id = one.getInteger("doc_id");
								JSONObject imageUrls = one.getJSONArray("pictures")
									.getJSONObject(new Random().nextInt(one.getJSONArray("pictures").size()));
								String imgurl = imageUrls.getString("img_src");
								extra_msg = "\n-- " + apiName + "\n" + one.getString("description")
									+ "\n动态地址 : https://h.bilibili.com/" + dynamic_id + "\n图片总数: " + one.getInteger("count")
									+ "张";
								System.out.println("图片地址: " + imgurl);
								HttpURLConnection conn = null;
								// 建立链接
								URL httpUrl = new URL(imgurl);
								conn = (HttpURLConnection) httpUrl.openConnection();
								// 默认get方式
								conn.setRequestMethod("GET");
								// b站的开放接口要啥 referer，不需要的
								// conn.setRequestProperty("referer", "https://pixivic.com/illusts/" + pid +
								// "");

								conn.setDoInput(true);
								conn.setDoOutput(true);
								conn.connect();
								// 获取网络输入流
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
										s = "连接超时";
									}
									group.sendMessage(quote.plus(" [wallpaper] 可能是获取图片时出现了一个异常: \n" + s + "\n\n动态地址 : "
											+ (dynamic_id == 0 ? "获取失败" : ("https://h.bilibili.com/" + dynamic_id))));
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
							System.out.println("图片地址: " + url);
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
						group.sendMessage(quote.plus(" [wallpaper] 可能是发送图片时出现了一个异常: \n" + e.toString()));
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
				group.sendMessage(quote.plus(" [wallpaper] 冷却时间还没到哦\n剩余 " + (nexttime - nowtime) + " 秒"));
			}
		}
		if (!ok /* && !checkCache */) {
			group.sendMessage(quote.plus(" [wallpaper] 参数无效"));
		}
	}
}
