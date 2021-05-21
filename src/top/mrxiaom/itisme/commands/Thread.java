package top.mrxiaom.itisme.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.message.data.SingleMessage;
import top.mrxiaom.itisme.Natsuko;
import top.mrxiaom.miraiutils.CommandModel;
import top.mrxiaom.miraiutils.CommandSender;
import top.mrxiaom.miraiutils.CommandSenderGroup;

public class Thread extends CommandModel{
	Natsuko main;
	public HashMap<String, String> threads = new HashMap<String, String>();

	public Thread(Natsuko main) {
		super("thread");
		this.main = main;
		loadThreads();
	}

	public void loadThreads() {

		File f = new File(main.getDataFolder().getAbsoluteFile().getPath() + "\\threads.txt");
		if (f.isFile() && f.exists()) {
			BufferedReader bufferedReader = null;
			try {
				FileInputStream fileInputStream = new FileInputStream(f);
				InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
				bufferedReader = new BufferedReader(inputStreamReader);
				String text = null;
				while ((text = bufferedReader.readLine()) != null) {
					if (!text.startsWith("#") && text.contains(":")) {
						String key = text.substring(0, text.indexOf(":"));
						String value = text.substring(text.indexOf(":") + 1).replace("\\n", "\n");
						threads.put(key, value);
					}
				}
				System.out.println("[thread] 列表载入完毕");
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (bufferedReader != null) {
					try {
						bufferedReader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void onCommand(CommandSender sender, SingleMessage[] args) {
		if(this.main.getConfig().isBlackList(sender.getSenderID())) return;
		if(!(sender instanceof CommandSenderGroup)) return;
		CommandSenderGroup senderGroup = (CommandSenderGroup) sender;
		Group group = senderGroup.getGroup();
		if(group.getId() != 241973735L) return;
		Member member = senderGroup.getMember();
		QuoteReply quote = new QuoteReply(senderGroup.getMessageSource());
		if (args.length == 1) {
			String key = args[0].contentToString();
			if (key.equalsIgnoreCase("reload") && member.getId() == 2431208142L) {
				threads.clear();
				loadThreads();
				group.sendMessage(quote.plus("[thread] 已重新载入列表"));
				return;
			} else {
				if (threads.containsKey(key)) {
					group.sendMessage(quote.plus("\n" + threads.get(key)));
					return;
				}
			}
		}
		String help = "\n [thread] 参数无效 \n用法: /thread [帖子编号]\n举个例子，比如 /thread 0\n帖子编号列表如下:";
		for (String k : threads.keySet()) {
			if(threads.get(k).contains("\n"))
				help = help + "\n[" + k + "] " + threads.get(k).substring(0, threads.get(k).indexOf("\n"));
		}
		group.sendMessage(quote.plus(help));
	}
}
