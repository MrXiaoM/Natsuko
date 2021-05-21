package top.mrxiaom.itisme.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Random;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.message.data.SingleMessage;
import net.mamoe.mirai.utils.ExternalResource;
import top.mrxiaom.itisme.Natsuko;
import top.mrxiaom.miraiutils.CommandModel;
import top.mrxiaom.miraiutils.CommandSender;
import top.mrxiaom.miraiutils.CommandSenderGroup;

public class AcgImg extends CommandModel{
	Natsuko main;
	File picDir;
	long nexttime = 0;

	int success = 0;
	int fail = 0;

	public AcgImg(Natsuko main) {
		super("acgimg");
		this.main = main;
		picDir = new File(main.getDataFolder(), "acg");
		if(!picDir.exists()) {
			picDir.mkdirs();
		}
		
		initStats();
	}
	public void initStats() {
		success = 0;
		fail = 0;
	}
	
	@Override
	public void onCommand(CommandSender sender, SingleMessage[] args) {
		if(this.main.getConfig().isBlackList(sender.getSenderID())) return;
		if(!(sender instanceof CommandSenderGroup)) return;
		CommandSenderGroup senderGroup = (CommandSenderGroup) sender;
		Group group = senderGroup.getGroup();
		Member member = senderGroup.getMember();
 		if(main.isGroupAccess(group, this.getClass())) {
			
			QuoteReply quote = new QuoteReply(senderGroup.getMessageSource());
			if(args.length == 1 && args[0].contentToString().equalsIgnoreCase("stats")) {
				group.sendMessage(quote.plus("[AcgImg] 图库图片总量: " + picDir.listFiles().length + "\n"
						+ "今日机器人壁纸获取状态:\n"
						+ "(机器人重启或每日0点后清零)\n"
						+ "成功: " + success+"\n"
						+ "失败: " + fail));
				return;
			}
			if(picDir.listFiles().length == 0) {
				group.sendMessage(quote.plus("[AcgImg] 图库暂无内容，敬请期待"));
			}
			Calendar now = Calendar.getInstance();
			long nowtime = now.get(Calendar.HOUR_OF_DAY) * 3600 + now.get(Calendar.MINUTE) * 60
					+ now.get(Calendar.SECOND);
			if ((nexttime - nowtime) > 15) {
				nexttime = 0;
				//event.getGroup().sendMessage(" [AcgImg] 冷却时间出现异常，已重置今日冷却时间");
			}

			boolean admin = main.isManager(member.getId())
					|| member.getPermission() == MemberPermission.ADMINISTRATOR
					|| member.getPermission() == MemberPermission.OWNER;
			if (nowtime >= nexttime || admin) {
				if (!admin) {
					nexttime = nowtime + 15;
					if (nexttime >= 86400) {
						nexttime = 0;
					}
				}
				InputStream is = null;
				try {
					File file = picDir.listFiles()[new Random().nextInt(picDir.listFiles().length)];
					is = new FileInputStream(file);
					main.getLogger().info("图片文件名: " + file.getName());
					group.sendMessage(group.uploadImage(ExternalResource.create(is)));
					success++;
				} catch(Throwable t) {
					t.printStackTrace();
					fail++;
					group.sendMessage(quote.plus("[AcgImg] 图片上传或发送失败:\n" + t.getLocalizedMessage()));
				}
				finally {
					if(is != null) {
						try {
							is.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			else {
				group.sendMessage(quote.plus("[AcgImg] 冷却时间还没到哦\n剩余 " + (nexttime - nowtime) + " 秒"));
			}
		}
	}
}
