package top.mrxiaom.itisme.commands;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.message.data.SingleMessage;
import net.mamoe.mirai.utils.ExternalResource;
import top.mrxiaom.itisme.Natsuko;
import top.mrxiaom.itisme.Util;
import top.mrxiaom.miraiutils.CommandModel;
import top.mrxiaom.miraiutils.CommandSender;
import top.mrxiaom.miraiutils.CommandSenderGroup;

public class RandomNews extends CommandModel{
	Natsuko main;
	public List<Long> managers = new ArrayList<Long>();
	long nexttime = 0;

	public RandomNews(Natsuko main) {
		super("randomnews");
		this.main = main;
		managers.add(2431208142L);
		managers.add(2406120646L);
	}
	
	@Override
	public void onCommand(CommandSender sender, SingleMessage[] args) {
		if(this.main.getConfig().isBlackList(sender.getSenderID())) return;
		if(!(sender instanceof CommandSenderGroup)) return;
		CommandSenderGroup senderGroup = (CommandSenderGroup) sender;
		Group group = senderGroup.getGroup();
		Member member = senderGroup.getMember();
 		if(group.getId() == 241973735L
			|| group.getId() == 951534513L
			|| group.getId() == 252631851L) {
			
			QuoteReply quote = new QuoteReply(senderGroup.getMessageSource());
			Calendar now = Calendar.getInstance();
			long nowtime = now.get(Calendar.HOUR_OF_DAY) * 3600 + now.get(Calendar.MINUTE) * 60
					+ now.get(Calendar.SECOND);
			if ((nexttime - nowtime) > 15) {
				nexttime = 0;
				//event.getGroup().sendMessage(" [AcgImg] 冷却时间出现异常，已重置今日冷却时间");
			}

			boolean admin = managers.contains(member.getId())
					|| member.getPermission() == MemberPermission.ADMINISTRATOR
					|| member.getPermission() == MemberPermission.OWNER;
			if (nowtime >= nexttime || admin) {
				if (!admin) {
					nexttime = nowtime + 15;
					if (nexttime >= 86400) {
						nexttime = 0;
					}
				}
				try {
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
				} catch(Throwable t) {
					t.printStackTrace();
					group.sendMessage(quote.plus("[RandomNews] 出现异常:\n" + t.getLocalizedMessage()));
				}
			}
			else {
				group.sendMessage(quote.plus("[RandomNews] 冷却时间还没到哦\n剩余 " + (nexttime - nowtime) + " 秒"));
			}
		}
	}
}
