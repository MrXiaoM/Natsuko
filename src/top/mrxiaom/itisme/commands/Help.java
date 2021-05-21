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
				+ "          -- Natsuko �˵� --       "    + "\n"
				+ "\n" 
				+ " �� /help | �鿴�˲˵�"                  + "\n"
				+ " �� /status [��·] | �鿴������״̬"     + "\n"
				+ "      - [�ӿ�]����mc,dx,lt,yd,ip,branch" + "\n" 
				+ "      - [��·]������Ĭ����·"        + "\n" 
				+ " �� /wallpaper [�ӿ�] | ��ȡ����Ԫ��ֽ"  + "\n"
				+ "      - [�ӿ�]���Ϊ���(cd:30��)"     + "\n" 
				+ "      - [�ӿ�]��stats��ѯ�ӿ�ʹ�����"   + "\n" 
				+ "      - [�ӿ�]��about�鿴�ӿ���Դ����"   + "\n" 
				+ " �� /mcban [QQ��] | MC�ƶ˺�������ѯ"    + "\n"
				+ " �� /thread [���ӱ��] | �鿴����"       + "\n"
				+ "      - [���ӱ��]���Ϊ�鿴�б�"      + "\n"
				//+ " �� /server | �鿴������ż��������"       + "\n"
				+ " �� /acgimg | ������ͻ����˱��صĶ���Ԫ��ֽ"       + "\n"
				+ " �� /randomnews | ��㿴������"       + "\n"
				+ " �� /about | ���ڻ�����"                 + "\n"
				+ "\n"
				+ "-------------------------------------"   + "\n"
				+ "          POWER BY MIRAI ";
	}
	public String getHelpMX() {
		return ""
				+ "          -- Natsuko �˵� --       "    + "\n"
				+ "\n" 
				+ " �� /help | �鿴�˲˵�"                  + "\n"
				+ " �� /server | ��������⹤��"     + "\n"
				+ " �� /verify <QQ/@QQ> [ʱ��(��)] | ͼ�����"  + "\n"
				+ " �� /acgimg | ������ͻ����˱��صĶ���Ԫ��ֽ"       + "\n"
				+ " �� /about | ���ڻ�����"                 + "\n"
				+ "\n"
				+ "-------------------------------------"   + "\n"
				+ "          POWER BY MIRAI ";
	}
}
