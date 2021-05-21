package top.mrxiaom.itisme.commands;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.SingleMessage;
import net.mamoe.mirai.utils.ExternalResource;
import top.mrxiaom.itisme.Natsuko;
import net.mamoe.mirai.internal.message.OnlineImage;

public class FriendMessageReceived {
	Natsuko main;
	public FriendMessageReceived(Natsuko main) {
		this.main = main;
	}
	
	public void onCommand(FriendMessageEvent event) {
		MessageChainBuilder mcb = new MessageChainBuilder();
		long taker = main.getConfig().getJSON().getLong("private-message-taker");
		String qq = String.valueOf(event.getSender().getId());
		String name = event.getSender().getNick();
		mcb.add("我收到了一条来自 ${name} (${qq}) 的消息:\n".replace("${name}", name).replace("${qq}", qq)
			  + "---------------------------------\n");
		for(int i = 1;i<event.getMessage().size();i++) {
			SingleMessage m = event.getMessage().get(i);

				if(m instanceof Image) {

					OnlineImage image = (OnlineImage)m;
					System.out.println(image.getOriginUrl());
					try {

						URL url = new URL(image.getOriginUrl());
						HttpURLConnection conn = (HttpURLConnection) url.openConnection();
						conn.setConnectTimeout(5000);
						conn.setReadTimeout(60000);
						InputStream inputStream = conn.getInputStream();
						mcb.add(event.getBot().getFriend(taker).uploadImage(ExternalResource.create(inputStream)));
					} catch (Throwable e) {
						mcb.add("[图片]");
						e.printStackTrace();
					}
				}
				else {
					mcb.add(m);
				}
			
		}
		event.getBot().getFriend(taker).sendMessage(mcb.build());
	}
}
