package top.mrxiaom.itisme.commands;

import ch.jamiete.mcping.MinecraftPing;
import ch.jamiete.mcping.MinecraftPingOptions;
import ch.jamiete.mcping.MinecraftPingReply;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.message.data.SingleMessage;
import top.mrxiaom.itisme.Natsuko;
import top.mrxiaom.itisme.Util;
import top.mrxiaom.miraiutils.CommandModel;
import top.mrxiaom.miraiutils.CommandSender;
import top.mrxiaom.miraiutils.CommandSenderGroup;

public class Status extends CommandModel {
	Natsuko main;

	public Status(Natsuko main) {
		super("status");
		this.main = main;
	}

	public void onCommand(GroupMessageEvent event) {

	}

	@Override
	public void onCommand(CommandSender sender, SingleMessage[] args) {
		if(this.main.getConfig().isBlackList(sender.getSenderID())) return;
		if (!(sender instanceof CommandSenderGroup))
			return;
		CommandSenderGroup senderGroup = (CommandSenderGroup) sender;
		Group group = senderGroup.getGroup();
		QuoteReply quote = new QuoteReply(senderGroup.getMessageSource());
		if (group.getId() == 241973735L) {
			String string = "";
			try {
				String serverip = "mc.66ko.cc";
				int port = 25565;
				if (args.length == 1) {
					if (args[0].contentToString().equalsIgnoreCase("mc")) {
						serverip = "mc.66ko.cc";
						port = 25565;
					}
					if (args[0].contentToString().equalsIgnoreCase("dx")) {
						serverip = "dx.66ko.cc";
						port = 25565;
					}
					if (args[0].contentToString().equalsIgnoreCase("lt")) {
						serverip = "lt.66ko.cc";
						port = 25565;
					}
					if (args[0].contentToString().equalsIgnoreCase("yd")) {
						serverip = "yd.66ko.cc";
						port = 25565;
					}
					if (args[0].contentToString().equalsIgnoreCase("ip")) {
						serverip = "218.93.206.50";
						port = 25565;
					}
					if (args[0].contentToString().equalsIgnoreCase("branch")) {
						serverip = "43.248.187.189";
						port = 40006;
					}
				}
				MinecraftPingOptions options = new MinecraftPingOptions();
				options.setHostname(serverip);
				options.setPort(port);
				MinecraftPingReply mc = new MinecraftPing().getPing(options);
				String desc = mc.getDescription();
				while (desc.contains("  ")) {
					desc = desc.replace("  ", " ");
				}
				String online = mc.getPlayers().getOnline() + "/" + mc.getPlayers().getMax();
				string += "    生存都市服务器 ★ " + serverip + (port == 25565 ? "" : (":" + port)) + "\n";
				string += "--------------------------------[" + mc.getDelay() + "ms]-\n";
				string += "" + desc + "" + "\n";
				string += " ◇ 在线: " + online + "\n";
			} catch (Throwable e) {
				string = "在获取服务器信息时出现了一个错误\n" + e.toString();
				System.out.println("在获取服务器信息时出现了一个错误");
				e.printStackTrace();
			}
			group.sendMessage(quote.plus(string));
		}
		if (group.getId() == 738159283L || group.getId() == 951534513L) {
			String string = "";
			try {
				MinecraftPingOptions options = new MinecraftPingOptions();
				options.setHostname("mc.doomteam.fun");
				options.setPort(831);
				MinecraftPingReply mc = new MinecraftPing().getPing(options);
				String desc = Natsuko.clearColor(mc.getDescription());
				while (desc.contains("  ")) {
					desc = desc.replace("  ", " ");
				}
				String online = mc.getPlayers().getOnline() + " / " + mc.getPlayers().getMax();
				// String online = mc.getVersion().getName()
				// .replace("§0", "")
				// .replace("§1", "")
				// .replace("§2", "")
				// .replace("§3", "")
				// .replace("§4", "")
				// .replace("§5", "")
				// .replace("§6", "")
				// .replace("§7", "")
				// .replace("§8", "")
				// .replace("§9", "")
				// .replace("§a", "")
				// .replace("§b", "")
				// .replace("§c", "")
				// .replace("§d", "")
				// .replace("§e", "")
				// .replace("§f", "")
				// .replace("§r", "")
				// .replace("§l", "")
				// .replace("§m", "")
				// .replace("§n", "")
				// .replace("§o", "")
				// .replace("§k", "")
				// .replace("[", "")
				// .replace("]", "");
				string += "--------------------------------[" + mc.getDelay() + "ms]-\n";
				string += "" + desc + "" + "\n";
				string += " ◇ 在线: " + online + "\n";
				string += "IP: mc.doomteam.fun:831";
			} catch (Throwable e) {
				boolean ok = checkServerConsoleStatus();
				string = "在获取服务器信息时出现了一个错误\n" + e.toString() + "\n";
				
				if(ok) string += "但服务器后台仍在运行，请联系小猫处理";
				else string += "无法连接到服务器后台，请联系阿强处理";
				
				System.out.println("在获取服务器信息时出现了一个错误");
				e.printStackTrace();
			}
			group.sendMessage(quote.plus(string));

		}
	}
	
	public static boolean checkServerConsoleStatus() {
		return Util.sendGet("http://mc.doomteam.fun:19198/public/check.txt").trim().equalsIgnoreCase("ok");
	}
}
