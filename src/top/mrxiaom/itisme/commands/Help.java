package top.mrxiaom.itisme.commands;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Face;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.message.data.SingleMessage;
import top.mrxiaom.itisme.Natsuko;
import top.mrxiaom.miraiutils.CommandModel;
import top.mrxiaom.miraiutils.CommandSender;
import top.mrxiaom.miraiutils.CommandSenderGroup;

public class Help extends CommandModel{
	Natsuko main;
	public Help(Natsuko main) {
		super("help");
		this.main = main;
	}
	
	@Override
	public void onCommand(CommandSender sender, SingleMessage[] args) {
		if(this.main.getConfig().isBlackList(sender.getSenderID())) return;
		if(sender instanceof CommandSenderGroup) {
			CommandSenderGroup senderGroup = (CommandSenderGroup) sender;
			Group group = senderGroup.getGroup();
			QuoteReply quote = new QuoteReply(senderGroup.getMessageSource());
			if(group.getId() == 241973735L ||
				group.getId() == 951534513L) {
				group.sendMessage(quote.plus(this.getHelp()).plus(new Face(Face.KE_AI)));
			}
			if(group.getId() == 252631851L) {
				group.sendMessage(quote.plus(this.getHelpMX()).plus(new Face(Face.KE_AI)));
			}
		}
	}

	public String getHelp() {
		return ""
				+ "          -- Natsuko 菜单 --       "    + "\n"
				+ "\n" 
				+ " ◇ /help | 查看此菜单"                  + "\n"
				+ " ◇ /status [线路] | 查看服务器状态"     + "\n"
				+ "      - [接口]可填mc,dx,lt,yd,ip,branch" + "\n" 
				+ "      - [线路]不填即检测默认线路"        + "\n" 
				+ " ◇ /wallpaper [接口] | 获取二次元壁纸"  + "\n"
				+ "      - [接口]不填即为随机(cd:30秒)"     + "\n" 
				+ "      - [接口]填stats查询接口使用情况"   + "\n" 
				+ "      - [接口]填about查看接口来源链接"   + "\n" 
				+ " ◇ /mcban [QQ号] | MC云端黑名单查询"    + "\n"
				+ " ◇ /thread [帖子编号] | 查看帖子"       + "\n"
				+ "      - [帖子编号]不填即为查看列表"      + "\n"
				//+ " ◇ /server | 查看进服人偶帮助命令"       + "\n"
				+ " ◇ /acgimg | 随机发送机器人本地的二次元壁纸"       + "\n"
				+ " ◇ /randomnews | 随便看看新闻"       + "\n"
				+ " ◇ /about | 关于机器人"                 + "\n"
				+ "\n"
				+ "-------------------------------------"   + "\n"
				+ "          POWER BY MIRAI ";
	}
	public String getHelpMX() {
		return ""
				+ "          -- Natsuko 菜单 --       "    + "\n"
				+ "\n" 
				+ " ◇ /help | 查看此菜单"                  + "\n"
				+ " ◇ /server | 服务器检测工具"     + "\n"
				+ " ◇ /verify <QQ/@QQ> [时间(秒)] | 图灵测试"  + "\n"
				+ " ◇ /acgimg | 随机发送机器人本地的二次元壁纸"       + "\n"
				+ " ◇ /about | 关于机器人"                 + "\n"
				+ "\n"
				+ "-------------------------------------"   + "\n"
				+ "          POWER BY MIRAI ";
	}
}
