package top.mrxiaom.itisme.commands;

import java.util.Calendar;
import java.util.jar.JarFile;

import net.mamoe.mirai.console.internal.MiraiConsoleBuildConstants;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Face;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.message.data.SingleMessage;
import top.mrxiaom.itisme.Natsuko;
import top.mrxiaom.miraiutils.CommandModel;
import top.mrxiaom.miraiutils.CommandSender;
import top.mrxiaom.miraiutils.CommandSenderGroup;

public class About extends CommandModel{
	Natsuko main;
	public About(Natsuko main) {
		super("about");
		this.main = main;
	}
	public String getAbout() {
		String updateTime = "2077/12/10 --:--:--";
		try {
			String path = getClass().getClassLoader().getResource(this.getClass().getName().replace('.', '/') + ".class").getPath();
			JarFile jf = new JarFile(path.substring(6, path.indexOf("!")));
			Calendar date2 = Calendar.getInstance();
			date2.setTimeInMillis(jf.getEntry("META-INF/MANIFEST.MF").getLastModifiedTime().toMillis());
			jf.close();
			int year = date2.get(Calendar.YEAR);
			int month = date2.get(Calendar.MONTH) + 1;
			int day = date2.get(Calendar.DAY_OF_MONTH);
			int hour = date2.get(Calendar.HOUR_OF_DAY);
			int minute = date2.get(Calendar.MINUTE);
			int second =  date2.get(Calendar.SECOND);
			updateTime = year + "/" + (month < 10 ? "0" : "") + month + "/" + (day < 10 ? "0" : "") + day
			+ " " + (hour < 10 ? "0" : "") + hour + ":" + (minute < 10 ? "0" : "") + minute + ":" + (second < 10 ? "0" : "") + second;
		}catch(Throwable t) {
			System.gc();
			updateTime = "2077/12/10 --:--:--";
			t.printStackTrace();
		}
		return ""
				+ "          -- 关于 Natsuko --       "     + "\n"
				+ "\n" 
				+ " * 主人/开发者: MrXiaoM"         + "\n"
				+ " * 使用库:"                              + "\n"
				+ "  - alibaba/fastjson"                    + "\n"
				+ "  - dnsjava/dnsjava"                     + "\n"
				+ "  - google/gson"                         + "\n"
				+ "  - jamietech/MinecraftServerPing"       + "\n"
				+ "  - mamoe/mirai"                         + "\n"
				+ "  - mamoe/mirai-console"                 + "\n" 
				+ "  - mrxiaom/mirai-utils"                 + "\n" 
				+ "  - netty/netty"                         + "\n"
				//+ "  - Steveice10/AuthLib"                  + "\n"
				//+ "  - Steveice10/PacketLib"                + "\n"
				//+ "  - Steveice10/OpenNBT"                  + "\n"
				//+ "  - Steveice10/MCProtocolLib"            + "\n"
				+ " 感谢上述的开源项目! "                    + "\n"
				//+ " 感谢rua提供机器"                        + "\n"
				+ " 没有他们就没有今天的 Natsuko"           + "\n"
				+ " Feature update on " + updateTime       + "\n"
				+ " Mirai Core/Console v" + MiraiConsoleBuildConstants.versionConst + "\n"
				+ " Mirai by Mamoe Technologies"                  + "\n"
				+ " Natsuko by MrXiaoM"                   + "\n"
				+ "\n"
				+ "------------------------------------"    + "\n"
				+ "          POWER BY MIRAI ";
	}

	@Override
	public void onCommand(CommandSender sender, SingleMessage[] args) {
		if(this.main.getConfig().isBlackList(sender.getSenderID())) return;
		if(sender instanceof CommandSenderGroup) {
			CommandSenderGroup senderGroup = (CommandSenderGroup) sender;
			Group group = senderGroup.getGroup();
			if(main.isGroupAccess(group, this.getClass())) {
				QuoteReply quote = new QuoteReply(senderGroup.getMessageSource());
				group.sendMessage(quote.plus(this.getAbout()).plus(new Face(Face.KE_AI)));
			}
		}
	}
}
