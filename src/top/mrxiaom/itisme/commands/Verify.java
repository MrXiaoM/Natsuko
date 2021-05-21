package top.mrxiaom.itisme.commands;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Random;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.message.data.SingleMessage;
import net.mamoe.mirai.utils.ExternalResource;
import top.mrxiaom.itisme.Natsuko;
import top.mrxiaom.itisme.Util;
import top.mrxiaom.itisme.TickTask.Task;
import top.mrxiaom.miraiutils.CommandModel;
import top.mrxiaom.miraiutils.CommandSender;
import top.mrxiaom.miraiutils.CommandSenderGroup;

public class Verify extends CommandModel {
	final String prefix = "[reCAPTCHA]";
	Natsuko main;
	File subjectDir;
	public Verify(Natsuko main) {
		super("verify");
		this.main = main;
		subjectDir = new File(main.getDataFolder(), "reCAPTCHA");
		if(!subjectDir.exists()) {
			subjectDir.mkdirs();
		}
	}

	@Override
	public void onCommand(CommandSender sender, SingleMessage[] args) {
		if(this.main.getConfig().isBlackList(sender.getSenderID())) return;
		if (!(sender instanceof CommandSenderGroup))
			return;
		CommandSenderGroup senderGroup = (CommandSenderGroup) sender;
		Group group = senderGroup.getGroup();
		if (group.getId() != 252631851L)
			return;
		Member member = senderGroup.getMember();
		QuoteReply quote = new QuoteReply(senderGroup.getMessageSource());

		boolean access = (member.getPermission() == MemberPermission.ADMINISTRATOR
				|| member.getPermission() == MemberPermission.OWNER || member.getId() == 2431208142L);
		if (!access) {
			group.sendMessage(quote.plus(prefix + " 权限不足"));
			return;
		}

		if (args.length >= 1) {
			if (args.length == 1) {
				if (args[0].contentToString().equalsIgnoreCase("list")) {
					String list = "";
					for (String key : main.tickTask.tasks.keySet()) {
						Task task = main.tickTask.tasks.get(key);
						if (member.getId() == task.getOwner()) {
							list = list + " - " + key + " | 测试对象: "
									+ (group.getMembers().contains(task.getUserId())
											? (group.getMembers().get(task.getUserId()).getNameCard() + "("
													+ task.getUserId() + ")")
											: "已退群")
									+ "\n      超时时间: " + task.getTimeString() + "\n";
						}
					}
					if (list == "")
						list = "空";
					group.sendMessage(quote.plus(prefix + " 你发起的图灵测试列表:\n" + list));
					return;
				}
				if (args[0].contentToString().equalsIgnoreCase("alllist")) {
					String list = "";
					for (String key : main.tickTask.tasks.keySet()) {
						Task task = main.tickTask.tasks.get(key);
						list = list + " - " + key + " | 测试对象: "
								+ (group.getMembers().contains(task.getUserId())
										? (group.getMembers().get(task.getUserId()).getNameCard() + "("
												+ task.getUserId() + ")")
										: "已退群")
								+ "\n      超时时间: " + task.getTimeString() + "\n";

					}
					if (list == "")
						list = "空";
					group.sendMessage(quote.plus(prefix + " 图灵测试列表:\n" + list));
					return;
				}
			}
			int timeout = 300;
			if (args.length == 2) {
				if (args[0].contentToString().equalsIgnoreCase("cancel")) {
					if (!main.tickTask.tasks.keySet().contains(args[1].contentToString())) {
						group.sendMessage(quote.plus(prefix + " 你指定的id不存在"));
						return;
					}
					main.tickTask.tasks.remove(args[1].contentToString());
					group.sendMessage(quote.plus(prefix + " 成功移除该图灵测试请求"));
					return;
				}
				if (args[1].contentToString().length() > 4) {
					group.sendMessage(quote.plus(prefix + " 兄啊，怎么会有这么长的图灵测试啊 (恼)"));
					return;
				}
				int tempTime = Util.strToInt(args[1].contentToString(), -1);
				if (tempTime <= 30L) {
					group.sendMessage(quote.plus(prefix + " 时间必须输入大于或等于30的数字，单位是秒"));
					return;
				}
				timeout = tempTime;
			}
			UUID uuid = UUID.randomUUID();
			String id = uuid.toString();

			Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);
			int second = c.get(Calendar.SECOND);
			second = second + timeout;
			for (; second >= 60; second = second - 60) {
				minute = minute + 1;
			}
			for (; minute >= 60; minute = minute - 60) {
				hour = hour + 1;
			}
			for (; hour >= 24; hour = hour - 24)
				;

			final long userId = args[0] instanceof At ? ((At) args[0]).getTarget()
					: Util.strToLong(args[0].contentToString(), -1);
			if (userId < 10000L) {
				group.sendMessage(quote.plus(prefix + "无效的QQ号"));
			}
			final long owner = member.getId();
			final long groupId = group.getId();
			final String subject = this.getRandomSubject(); 
			Task task = main.tickTask.new Task(id, owner, userId, hour, minute, second, subject, new Runnable() {
				@Override
				public void run() {
					Bot bot = Bot.getInstanceOrNull(3191684705L);
					if (bot == null)
						return;
					if (bot.getGroups().contains(groupId)) {
						Group group = bot.getGroup(groupId);
						group.sendMessage(new At(owner).plus(" 你发起的对 ").plus(new At(userId))
								.plus(" 的图灵测试已经超时\n"
								+ "请自行决定是否处决"));
					}
				}
			});
			main.tickTask.tasks.put(id, task);
			group.sendMessage(new At(userId).plus("\n---- Mirai reCAPTCHA\n"
					+ "我们检测到了异常流量\n"
					+ "请在 " + this.getTimeBySecond(timeout) + " 内完成以下操作进行人机验证\n"
					+ "否则你可能会被禁止访问\n"
					+ "--------------------------------\n"
					+ "发送 "  + id + "\n"
					+ "不要完成上一行的内容，此为题目标识符，\n"
					+ "等待2秒后题目将出现，该标识符可让你找到你的题目"));
			File tempDir = new File(main.getDataFolder(), "temp");
			if(!tempDir.exists()) tempDir.mkdirs();
			main.getScheduler().delayed(2000L, new Runnable() {
				public void run() {
					try {
						// 创建图片对象
			        	BufferedImage image = ImageIO.read(new File(subjectDir, subject + ".png"));
			        	// 基于图片对象打开绘图
			        	Graphics2D graphics = image.createGraphics();
			        	// 绘图逻辑 START
			        	final Font idFont = new Font("宋体", Font.PLAIN, 24);
			        	// 绘制文本框 
			        	graphics.setFont(idFont);
			        	graphics.setPaint(Color.BLACK);
			        	graphics.drawString(id, 14, 18);
			        	graphics.drawString("目前更换题目功能正在编写", 14, 42);
			        	// 绘图逻辑 END
			        	// 处理绘图
			        	graphics.dispose();
			        	ImageIO.write(image, "png", new File(tempDir, subject + ".png"));
			        	// 将绘制好的图片写入到图片
			        	InputStream im = getImageStream(image);
						group.sendMessage(group.uploadImage(ExternalResource.create(im)));
					} catch(Throwable t) {
						t.printStackTrace();
						main.tickTask.tasks.remove(id);
						group.sendMessage(new At(userId).plus(" 发送题目时发生了一个异常:\n"
								+ "" + t.getLocalizedMessage() + "\n"
								+ "已取消图灵测试"));
					}
				}
			});
			return;
		}

		group.sendMessage(quote.plus(
				prefix + " Mirai reCAPTCHA (测试版)" +
				prefix + " /命令 <必选参数> <[可选参数]>\n" +
				prefix + " /verify <@QQ | QQ号> <[超时时间(秒)]> - 对某人进行图灵测试(默认5分钟超时)\n" +
				prefix + " /verify list - 查看你发出的图灵测试\n" + 
				prefix + " /verify cancel [ID] - 取消图灵测试"));
	}

	public InputStream getImageStream(BufferedImage bimage) {
		if (bimage == null)
			return null;
		InputStream is = null;
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		ImageOutputStream imOut;
		try {
			imOut = ImageIO.createImageOutputStream(bs);
			ImageIO.write(bimage, "png", imOut);
			is = new ByteArrayInputStream(bs.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return is;
	}
	
	private String getRandomSubject() {
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if(name.endsWith(".png"))
					return true;
				return false;
			}
		};
		File[] files = subjectDir.listFiles(filter);
		String fileName = files[new Random().nextInt(files.length)].getName();
		return fileName.substring(0, fileName.lastIndexOf("."));
	}
	
	private String getTimeBySecond(int s) {
		int hour = s / 3600;
		int minute = (s - hour * 3600) / 60;
		int second = s - hour * 300 - minute * 60;
		return (hour != 0 ? (hour + "小时"): "") + (minute != 0 ? (minute + "分钟"): "") + second + "秒";
	}
}
