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
			group.sendMessage(quote.plus(prefix + " Ȩ�޲���"));
			return;
		}

		if (args.length >= 1) {
			if (args.length == 1) {
				if (args[0].contentToString().equalsIgnoreCase("list")) {
					String list = "";
					for (String key : main.tickTask.tasks.keySet()) {
						Task task = main.tickTask.tasks.get(key);
						if (member.getId() == task.getOwner()) {
							list = list + " - " + key + " | ���Զ���: "
									+ (group.getMembers().contains(task.getUserId())
											? (group.getMembers().get(task.getUserId()).getNameCard() + "("
													+ task.getUserId() + ")")
											: "����Ⱥ")
									+ "\n      ��ʱʱ��: " + task.getTimeString() + "\n";
						}
					}
					if (list == "")
						list = "��";
					group.sendMessage(quote.plus(prefix + " �㷢���ͼ������б�:\n" + list));
					return;
				}
				if (args[0].contentToString().equalsIgnoreCase("alllist")) {
					String list = "";
					for (String key : main.tickTask.tasks.keySet()) {
						Task task = main.tickTask.tasks.get(key);
						list = list + " - " + key + " | ���Զ���: "
								+ (group.getMembers().contains(task.getUserId())
										? (group.getMembers().get(task.getUserId()).getNameCard() + "("
												+ task.getUserId() + ")")
										: "����Ⱥ")
								+ "\n      ��ʱʱ��: " + task.getTimeString() + "\n";

					}
					if (list == "")
						list = "��";
					group.sendMessage(quote.plus(prefix + " ͼ������б�:\n" + list));
					return;
				}
			}
			int timeout = 300;
			if (args.length == 2) {
				if (args[0].contentToString().equalsIgnoreCase("cancel")) {
					if (!main.tickTask.tasks.keySet().contains(args[1].contentToString())) {
						group.sendMessage(quote.plus(prefix + " ��ָ����id������"));
						return;
					}
					main.tickTask.tasks.remove(args[1].contentToString());
					group.sendMessage(quote.plus(prefix + " �ɹ��Ƴ���ͼ���������"));
					return;
				}
				if (args[1].contentToString().length() > 4) {
					group.sendMessage(quote.plus(prefix + " �ְ�����ô������ô����ͼ����԰� (��)"));
					return;
				}
				int tempTime = Util.strToInt(args[1].contentToString(), -1);
				if (tempTime <= 30L) {
					group.sendMessage(quote.plus(prefix + " ʱ�����������ڻ����30�����֣���λ����"));
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
				group.sendMessage(quote.plus(prefix + "��Ч��QQ��"));
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
						group.sendMessage(new At(owner).plus(" �㷢��Ķ� ").plus(new At(userId))
								.plus(" ��ͼ������Ѿ���ʱ\n"
								+ "�����о����Ƿ񴦾�"));
					}
				}
			});
			main.tickTask.tasks.put(id, task);
			group.sendMessage(new At(userId).plus("\n---- Mirai reCAPTCHA\n"
					+ "���Ǽ�⵽���쳣����\n"
					+ "���� " + this.getTimeBySecond(timeout) + " ��������²��������˻���֤\n"
					+ "��������ܻᱻ��ֹ����\n"
					+ "--------------------------------\n"
					+ "���� "  + id + "\n"
					+ "��Ҫ�����һ�е����ݣ���Ϊ��Ŀ��ʶ����\n"
					+ "�ȴ�2�����Ŀ�����֣��ñ�ʶ���������ҵ������Ŀ"));
			File tempDir = new File(main.getDataFolder(), "temp");
			if(!tempDir.exists()) tempDir.mkdirs();
			main.getScheduler().delayed(2000L, new Runnable() {
				public void run() {
					try {
						// ����ͼƬ����
			        	BufferedImage image = ImageIO.read(new File(subjectDir, subject + ".png"));
			        	// ����ͼƬ����򿪻�ͼ
			        	Graphics2D graphics = image.createGraphics();
			        	// ��ͼ�߼� START
			        	final Font idFont = new Font("����", Font.PLAIN, 24);
			        	// �����ı��� 
			        	graphics.setFont(idFont);
			        	graphics.setPaint(Color.BLACK);
			        	graphics.drawString(id, 14, 18);
			        	graphics.drawString("Ŀǰ������Ŀ�������ڱ�д", 14, 42);
			        	// ��ͼ�߼� END
			        	// �����ͼ
			        	graphics.dispose();
			        	ImageIO.write(image, "png", new File(tempDir, subject + ".png"));
			        	// �����ƺõ�ͼƬд�뵽ͼƬ
			        	InputStream im = getImageStream(image);
						group.sendMessage(group.uploadImage(ExternalResource.create(im)));
					} catch(Throwable t) {
						t.printStackTrace();
						main.tickTask.tasks.remove(id);
						group.sendMessage(new At(userId).plus(" ������Ŀʱ������һ���쳣:\n"
								+ "" + t.getLocalizedMessage() + "\n"
								+ "��ȡ��ͼ�����"));
					}
				}
			});
			return;
		}

		group.sendMessage(quote.plus(
				prefix + " Mirai reCAPTCHA (���԰�)" +
				prefix + " /���� <��ѡ����> <[��ѡ����]>\n" +
				prefix + " /verify <@QQ | QQ��> <[��ʱʱ��(��)]> - ��ĳ�˽���ͼ�����(Ĭ��5���ӳ�ʱ)\n" +
				prefix + " /verify list - �鿴�㷢����ͼ�����\n" + 
				prefix + " /verify cancel [ID] - ȡ��ͼ�����"));
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
		return (hour != 0 ? (hour + "Сʱ"): "") + (minute != 0 ? (minute + "����"): "") + second + "��";
	}
}
