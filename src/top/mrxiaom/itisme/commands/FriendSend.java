package top.mrxiaom.itisme.commands;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.internal.message.OnlineImage;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.SingleMessage;
import net.mamoe.mirai.utils.ExternalResource;
import top.mrxiaom.itisme.Natsuko;
import top.mrxiaom.itisme.Util;
import top.mrxiaom.miraiutils.CommandModel;
import top.mrxiaom.miraiutils.CommandSender;
import top.mrxiaom.miraiutils.CommandSenderFriend;

public class FriendSend extends CommandModel{
	Natsuko main;
	public FriendSend(Natsuko main) {
		super("fs");
		this.main = main;
	}
	
	@Override
	public void onCommand(CommandSender sender, SingleMessage[] args) {
		if(this.main.getConfig().isBlackList(sender.getSenderID())) return;
		if(!(sender instanceof CommandSenderFriend)) return;
		long taker = main.getConfig().getJSON().getLong("private-message-taker");
		CommandSenderFriend senderFriend = (CommandSenderFriend) sender;
		Friend friend = senderFriend.getFriend();
		if(friend.getId() != taker) return;
		if(args.length < 1) {
			friend.sendMessage("参数不足");
			return;
		}
		
		long friendqq = Util.strToLong(args[0].contentToString(),-1);
		//System.out.println(args2);
		if(friendqq < 10000) {
			friend.sendMessage("qq号不存在");
			return;
		}
		if(!sender.getBot().getFriends().contains(friendqq)) {
			friend.sendMessage("我没有TA的好友，无法发送消息");
			return;
		}
		//System.out.println(event.getMessage().getSize());
		MessageChainBuilder mcb = new MessageChainBuilder();
		
		for(int i = 1; i < args.length; i++) {
			SingleMessage single = args[i];
			if(single instanceof Image) {
				//FriendImage，无法用于群聊发送
				OnlineImage image = (OnlineImage) single;
				System.out.println(image.getOriginUrl());
				try {
					URL url = new URL(image.getOriginUrl());
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setConnectTimeout(5000);
					conn.setReadTimeout(60000);
					InputStream inputStream = conn.getInputStream();
					mcb.add(sender.getBot().getFriend(friendqq).uploadImage(ExternalResource.create(inputStream)));
				} catch (Throwable e) {
					mcb.add("[图片]");
					e.printStackTrace();
				}
				continue;
			}
			mcb.add(single.plus(" "));
		}
		sender.getBot().getFriend(friendqq).sendMessage(mcb.build());
		friend.sendMessage("消息转发完成");
	}
}
