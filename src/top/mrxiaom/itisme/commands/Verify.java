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
			group.sendMessage(quote.plus(prefix + " ????????"));
			return;
		}

		if (args.length >= 1) {
			if (args.length == 1) {
				if (args[0].contentToString().equalsIgnoreCase("list")) {
					String list = "";
					for (String key : main.tickTask.tasks.keySet()) {
						Task task = main.tickTask.tasks.get(key);
						if (member.getId() == task.getOwner()) {
							list = list + " - " + key + " | ????????: "
									+ (group.getMembers().contains(task.getUserId())
											? (group.getMembers().get(task.getUserId()).getNameCard() + "("
													+ task.getUserId() + ")")
											: "??????")
									+ "\n      ????????: " + task.getTimeString() + "\n";
						}
					}
					if (list == "")
						list = "??";
					group.sendMessage(quote.plus(prefix + " ????????????????????:\n" + list));
					return;
				}
				if (args[0].contentToString().equalsIgnoreCase("alllist")) {
					String list = "";
					for (String key : main.tickTask.tasks.keySet()) {
						Task task = main.tickTask.tasks.get(key);
						list = list + " - " + key + " | ????????: "
								+ (group.getMembers().contains(task.getUserId())
										? (group.getMembers().get(task.getUserId()).getNameCard() + "("
												+ task.getUserId() + ")")
										: "??????")
								+ "\n      ????????: " + task.getTimeString() + "\n";

					}
					if (list == "")
						list = "??";
					group.sendMessage(quote.plus(prefix + " ????????????:\n" + list));
					return;
				}
			}
			int timeout = 300;
			if (args.length == 2) {
				if (args[0].contentToString().equalsIgnoreCase("cancel")) {
					if (!main.tickTask.tasks.keySet().contains(args[1].contentToString())) {
						group.sendMessage(quote.plus(prefix + " ????????id??????"));
						return;
					}
					main.tickTask.tasks.remove(args[1].contentToString());
					group.sendMessage(quote.plus(prefix + " ??????????????????????"));
					return;
				}
				if (args[1].contentToString().length() > 4) {
					group.sendMessage(quote.plus(prefix + " ???????????????????????????????? (??)"));
					return;
				}
				int tempTime = Util.strToInt(args[1].contentToString(), -1);
				if (tempTime <= 30L) {
					group.sendMessage(quote.plus(prefix + " ??????????????????????30????????????????"));
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
				group.sendMessage(quote.plus(prefix + "??????QQ??"));
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
						group.sendMessage(new At(owner).plus(" ?????????? ").plus(new At(userId))
								.plus(" ??????????????????\n"
								+ "??????????????????"));
					}
				}
			});
			main.tickTask.tasks.put(id, task);
			group.sendMessage(new At(userId).plus("\n---- Mirai reCAPTCHA\n"
					+ "????????????????????\n"
					+ "???? " + this.getTimeBySecond(timeout) + " ??????????????????????????\n"
					+ "??????????????????????\n"
					+ "--------------------------------\n"
					+ "???? "  + id + "\n"
					+ "??????????????????????????????????????\n"
					+ "????2??????????????????????????????????????????"));
			File tempDir = new File(main.getDataFolder(), "temp");
			if(!tempDir.exists()) tempDir.mkdirs();
			main.getScheduler().delayed(2000L, new Runnable() {
				public void run() {
					try {
						// ????????????
			        	BufferedImage image = ImageIO.read(new File(subjectDir, subject + ".png"));
			        	// ????????????????????
			        	Graphics2D graphics = image.createGraphics();
			        	// ???????? START
			        	final Font idFont = new Font("????", Font.PLAIN, 24);
			        	// ?????????? 
			        	graphics.setFont(idFont);
			        	graphics.setPaint(Color.BLACK);
			        	graphics.drawString(id, 14, 18);
			        	graphics.drawString("????????????????????????", 14, 42);
			        	// ???????? END
			        	// ????????
			        	graphics.dispose();
			        	ImageIO.write(image, "png", new File(tempDir, subject + ".png"));
			        	// ????????????????????????
			        	InputStream im = getImageStream(image);
						group.sendMessage(group.uploadImage(ExternalResource.create(im)));
					} catch(Throwable t) {
						t.printStackTrace();
						main.tickTask.tasks.remove(id);
						group.sendMessage(new At(userId).plus(" ????????????????????????:\n"
								+ "" + t.getLocalizedMessage() + "\n"
								+ "??????????????"));
					}
				}
			});
			return;
		}

		group.sendMessage(quote.plus(
				prefix + " Mirai reCAPTCHA (??????)" +
				prefix + " /???? <????????> <[????????]>\n" +
				prefix + " /verify <@QQ | QQ??> <[????????(??)]> - ??????????????????(????5????????)\n" +
				prefix + " /verify list - ????????????????????\n" + 
				prefix + " /verify cancel [ID] - ????????????"));
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
		return (hour != 0 ? (hour + "????"): "") + (minute != 0 ? (minute + "????"): "") + second + "??";
	}
}
