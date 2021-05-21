package top.mrxiaom.itisme;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import net.mamoe.mirai.contact.ContactList;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.OtherClient;
import net.mamoe.mirai.contact.Platform;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.FriendAddEvent;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MemberMuteEvent;
import net.mamoe.mirai.event.events.MemberUnmuteEvent;
import net.mamoe.mirai.event.events.MessagePostSendEvent;
import net.mamoe.mirai.event.events.NewFriendRequestEvent;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.event.events.OtherClientMessageEvent;
import net.mamoe.mirai.event.events.MemberLeaveEvent.Kick;
import net.mamoe.mirai.event.events.MemberLeaveEvent.Quit;
import net.mamoe.mirai.event.events.MessageRecallEvent.GroupRecall;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.FlashImage;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.LightApp;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.internal.message.OnlineImage;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.message.data.SingleMessage;
import net.mamoe.mirai.utils.ExternalResource;
import net.mamoe.mirai.utils.MiraiLogger;
import top.mrxiaom.itisme.TickTask.Task;
import top.mrxiaom.miraiutils.CommandSenderGroup;

public class EventHost extends SimpleListenerHost{
	final Natsuko main;
	MiraiLogger logger;
	public EventHost(Natsuko m) {
		this.main = m;
		this.logger = MiraiLogger.create("O");
	}
	
	// ȺԱ������Ϣ
	@EventHandler
	private ListeningStatus onGroupRecall(GroupRecall event) {
		if(event.getBot().getId() != main.getConfig().getBotId()) return ListeningStatus.LISTENING;
		if(!main.isGroupListenRecall(event.getGroup())) return ListeningStatus.LISTENING;
		int msgId = event.getMessageIds()[0];
		
		MessageChainBuilder mcb = new MessageChainBuilder();
		mcb.add("Ⱥ " + event.getGroup().getName() + "\n("+event.getGroup().getId()+") �ĳ�Ա " + event.getGroup().getMembers().get(event.getAuthorId()).getNameCard()+"\n"
				+ "(" + event.getAuthorId() + ") ������һ����Ϣ:\n");
		Group group = event.getBot().getGroup(main.getConfig().getJSON().getJSONObject("recall-settings").getLong("forward-group"));
		if (main.messageRecord.containsKey(msgId)) {
			group.sendMessage(this.MessageTrans(group, "Ⱥ " + event.getGroup().getName() + "\n("+event.getGroup().getId() + ") �ĳ�Ա" + event.getGroup().getMembers().get(event.getAuthorId()).getNameCard()+"\n"
					+ "(" + event.getAuthorId() + ") " + (event.getAuthorId()==event.getOperator().getId()?"":
						"�� "+event.getOperator().getNameCard() + "\n("+event.getOperator().getId()+") ") + "������һ����Ϣ:\n", main.messageRecord.get(msgId).getOriginalMessage()));
		}
		else {
			group.sendMessage(mcb.build().plus("- ��Ϣ��ȡʧ�� - \n"
					+ "messageId: " + event.getMessageIds()[0] + "\n"
					+ "messageInternalId: " + event.getMessageInternalIds()[0] + "\n"
					+ "messageTime: " + event.getMessageTime()));
		}
		return  ListeningStatus.LISTENING;
	}

	private MessageChain MessageTrans(Group group, String a, MessageChain msg) {
		MessageChainBuilder result = new MessageChainBuilder();
		result.add(a);
		for(int i = 0; i < msg.size();i++) {
			SingleMessage single = msg.get(i);
			if (single instanceof Image) {
				try {
					OnlineImage image = (OnlineImage) single;
					// ���ԭͼƬ��ַ
					System.out.println(image.getOriginUrl());
					URL url = new URL(image.getOriginUrl());
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setConnectTimeout(5000);
					conn.setReadTimeout(60000);
					InputStream inputStream = conn.getInputStream();
					result.add(group.uploadImage(ExternalResource.create(inputStream)));
				}catch(Throwable t){
					t.printStackTrace();
					result.add("[ͼƬ]");
				}
			}
			else result.add(single);
		}
		return result.build();
	}
	@SuppressWarnings({ "unused" })
	private MessageChain MessageTrans(Friend friend, String a, MessageChain msg) {
		MessageChainBuilder result = new MessageChainBuilder();
		result.add(a);
		for(int i = 0; i < msg.size();i++) {
			SingleMessage single = msg.get(i);
			if (single instanceof Image) {
				try {
					
					OnlineImage image = (OnlineImage) single;
					// ���ԭͼƬ��ַ
					System.out.println(image.getOriginUrl());
					URL url = new URL(image.getOriginUrl());
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setConnectTimeout(5000);
					conn.setReadTimeout(60000);
					InputStream inputStream = conn.getInputStream();
					result.add(friend.uploadImage(ExternalResource.create(inputStream)));
				}catch(Throwable t){
					t.printStackTrace();
					result.add("[ͼƬ]");
				}
			}
			else result.add(single);
		}
		return result.build();
	}

	@EventHandler
	private ListeningStatus onMemberNudge(NudgeEvent event) {
		if(event.getSubject() instanceof Group) {
			Group group = (Group) event.getSubject();
			if(group.getId() == 241973735L || group.getId() == 951534513L || group.getId() == 252631851L) {
				if(event.getTarget().getId() == event.getBot().getId()) {
					group.sendMessage(new At(event.getFrom().getId()).plus(" ��ʲô���� owo\n���� /help ���Ի���ҵİ���ҳ��Ŷ"));
				}
			}
		}
		return ListeningStatus.LISTENING;
	}
	// ȺԱ������ʱ
	@EventHandler
	private ListeningStatus onMemberMute(MemberMuteEvent event) {
		if(event.getBot().getId() != main.getConfig().getBotId()) return ListeningStatus.LISTENING;
		if (main.getConfig().isBlackList(event.getMember().getId()))
			return ListeningStatus.LISTENING;
		int s = event.getDurationSeconds();
		String time = s+"��";
		/*
		if(s < 3600) {
			time = (int)(s/60)+"����";
		}
		
		if(s < 86400) {
			time = (int)(s/3600)+"Сʱ";
		}
		
		if(s >= 86400) {
			time = (int)(s/86400)+"��";
		}*/
		
		if(event.getOperator().getPermission() == MemberPermission.OWNER) {
			
			event.getGroup().sendMessage(event.getMember().getNameCard() + "\n("+event.getMember().getId()+") ��Ⱥ��\n����" + time);
			
		}
		if(event.getOperator().getPermission() == MemberPermission.ADMINISTRATOR) {
			
			event.getGroup().sendMessage(event.getMember().getNameCard() + "\n("+event.getMember().getId()+") ������Ա "+event.getOperator().getNameCard()+"\n���� " + time);
			
		}
		return ListeningStatus.LISTENING;
	}


	// ȺԱ���������ʱ
	@EventHandler
	private ListeningStatus onMemberUnmute(MemberUnmuteEvent event) {
		if(event.getBot().getId() != main.getConfig().getBotId()) return ListeningStatus.LISTENING;
		if (main.getConfig().isBlackList(event.getMember().getId()))
			return ListeningStatus.LISTENING;

		if(event.getOperator().getPermission() == MemberPermission.OWNER) {
			
			event.getGroup().sendMessage(event.getMember().getNameCard() + "\n("+event.getMember().getId()+") ��Ⱥ���������");
			
		}
		if(event.getOperator().getPermission() == MemberPermission.ADMINISTRATOR) {
			
			event.getGroup().sendMessage(event.getMember().getNameCard() + "\n("+event.getMember().getId()+") ������Ա "+event.getOperator().getNameCard()+"\n�������");
			
		}
		return ListeningStatus.LISTENING;
	}
	
	// ��Ӻ��Ѻ�
	@EventHandler
	private ListeningStatus onFriendAdd(FriendAddEvent event) {
		if(event.getBot().getId() != main.getConfig().getBotId()) return ListeningStatus.LISTENING;
		return ListeningStatus.LISTENING;
	}
	
	// �������촦��
	@EventHandler
	private ListeningStatus onFriendMessage(FriendMessageEvent event) {
		if(event.getBot().getId() != main.getConfig().getBotId()) return ListeningStatus.LISTENING;
		this.logFriendMsg(event);
		if (main.getConfig().isBlackList(event.getSender().getId()))
			return ListeningStatus.LISTENING;
		
		main.fme.onCommand(event);
		
		return ListeningStatus.LISTENING;
	}
	
	// �������ܺ�������
	@EventHandler
	private ListeningStatus onNewFriendRequest(NewFriendRequestEvent event) {
		if(event.getBot().getId() != main.getConfig().getBotId()) return ListeningStatus.LISTENING;
		if (main.getConfig().isBlackList(event.getFromId()))
			return ListeningStatus.LISTENING;
		if(!event.isIntercepted()) { // ���û�а�TA����
			event.accept(); // ͬ���������
		}
		return ListeningStatus.LISTENING;
	}
	
	// ȺԱ������Ⱥ
	@EventHandler
	private ListeningStatus onMemberLeaveQuit(Quit event) {
		if(event.getBot().getId() != main.getConfig().getBotId()) return ListeningStatus.LISTENING;
		if (main.getConfig().isBlackList(event.getMember().getId()))
			return ListeningStatus.LISTENING;
		if (event.getGroup().getId() == 241973735L) {
			event.getGroup().sendMessage(new At(event.getMember().getId()).getDisplay(event.getGroup())
						+ "\n" + "(" + event.getMember().getId() + ") ���뿪��Ⱥ");
		}
		return ListeningStatus.LISTENING;
	}
	
	// ȺԱ���߳�
	@EventHandler
	private ListeningStatus onMemberLeaveKick(Kick event) {
		if(event.getBot().getId() != main.getConfig().getBotId()) return ListeningStatus.LISTENING;
		if (main.getConfig().isBlackList(event.getMember().getId()))
			return ListeningStatus.LISTENING;
		if (event.getGroup().getId() == 241973735L) {
			if(event.getOperator().getPermission().equals(MemberPermission.OWNER)) {
				event.getGroup().sendMessage(
						new At(event.getMember().getId()).getDisplay(event.getGroup()) + "\n" + "(" + event.getMember().getId() + ") ��Ⱥ���߳���Ⱥ");
			}
			else {
				event.getGroup().sendMessage(
						new At(event.getMember().getId()).getDisplay(event.getGroup()) + "\n" + "(" + event.getMember().getId() + ") ������Ա�߳���Ⱥ");
				
			}
		}
		return ListeningStatus.LISTENING;
	}

	public void logFriendMsg(FriendMessageEvent event) {
		logger.info("[����]["+event.getSenderName()+"("+event.getSender().getId()+")]: " + event.getMessage().toString());
	}
	
	public void logGroupMsg(GroupMessageEvent event) {
		logger.info("[Ⱥ][" +event.getGroup().getName() + "("+event.getGroup().getId()+")]["+event.getSenderName()+"("+event.getSender().getId()+")]: " + event.getMessage().toString());
	}
	
	@EventHandler
	private ListeningStatus onMessagePostSend(MessagePostSendEvent<?> event) {
		if(event.getTarget() instanceof Group) {
			Group target = (Group) event.getTarget();
			logger.info("[��][Ⱥ][" + target.getName() + "("+target.getId()+")]["+event.getBot().getNick()+"("+event.getBot().getId()+")]: " + event.getMessage().toString());
		}
		else if(event.getTarget() instanceof Friend){
			Friend target = (Friend) event.getTarget();
			logger.info("[��][����][" + target.getNick() + "("+target.getId()+")]["+event.getBot().getNick()+"("+event.getBot().getId()+")]: " + event.getMessage().toString());
		}
		return ListeningStatus.LISTENING;
	}
	
	private ListeningStatus qrcode(GroupMessageEvent event) {
		boolean flag = false;
		List<String> links = new ArrayList<String>();
		// ������Ϣ�����зֶ�
		for(int i = 0; i < event.getMessage().size(); i++) {
			SingleMessage message = event.getMessage().get(i);
			// ���������
			if(message instanceof OnlineImage) {
				OnlineImage oImage = (OnlineImage) message;
				InputStream inputStream = null;
				try {
					URL url = new URL(oImage.getOriginUrl());
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setConnectTimeout(5000);
					conn.setReadTimeout(60000);
					inputStream = conn.getInputStream();
					if(inputStream != null) {
						String link = Util.decode(ImageIO.read(inputStream));
						links.add(link);
						flag = true;
					}
				}
				catch(Throwable t) {
					if(!(t instanceof com.google.zxing.NotFoundException))
					t.printStackTrace();
				}
				try {
					if(inputStream!=null)
						inputStream.close();
				}catch(Throwable t) {
					inputStream = null;
					System.gc();
				}
			}
		}
		if(flag) {
			String str = "[N]����ͼƬ�д��ڶ�ά�룬�������:\n";
			for(String s : links) {
				str += s + "\n";
			}
			event.getGroup().sendMessage(new QuoteReply(event.getSource()).plus(str));
		}
		return ListeningStatus.LISTENING;
	}
	
	@EventHandler
	private ListeningStatus onGroupMessage(GroupMessageEvent event) {
		if(event.getBot().getId() != main.getConfig().getBotId()) return ListeningStatus.LISTENING;
		this.logGroupMsg(event);
		
		if(event.getGroup().getId() == 738159283L || event.getGroup().getId() == 951534513L) {
			if(event.getMessage().contentToString().startsWith("��Ӱ�����")) {
				ContactList<OtherClient> clients = event.getBot().getOtherClients();
				boolean online = false;
				for(OtherClient client : clients) {
					if(client.getInfo().getPlatform().equals(Platform.IOS)) {
						online = true;
						break;
					}
				}
				if(!online) {
					event.getGroup().sendMessage(new QuoteReply(event.getSource()).plus("[����] ������δ�ڷ������ϵ�¼������ϵ����Ա"));
				}
			}
		}
		
		if (event.getGroup().getId() == 241973735L) {
			//System.out.println(event.getSource().getTime());
			main.messageRecord.put(event.getSource().getIds()[0], event.getSource());
			main.clearOutdataMessage(event.getSource().getTime());
			this.qrcode(event);
			// ������Ϣ�����зֶ�
			for(int i = 0; i < event.getMessage().size(); i++) {
				SingleMessage message = event.getMessage().get(i);
				// ���������
				if(message instanceof FlashImage) {
					FlashImage fimage = (FlashImage) message;
					//fimage.getImage()
					Group group = event.getBot().getGroup(1147228914L);
					MessageChainBuilder mcb = new MessageChainBuilder();
					mcb.add( "����һȺȺ�� " + event.getSender().getNameCard()+"\n"
					+ "(" + event.getSender().getId() + ") ������һ������:\n");
					try {

						URL url = new URL(Util.getUrlFromImage(fimage.getImage()));
						HttpURLConnection conn = (HttpURLConnection) url.openConnection();
						conn.setConnectTimeout(5000);
						conn.setReadTimeout(60000);
						InputStream inputStream = conn.getInputStream();
						mcb.add(group.uploadImage(ExternalResource.create(inputStream)));
					} catch (Throwable e) {
						mcb.add("���ջ�ȡʧ��!\n");
						mcb.add(e.getMessage());
					}

					group.sendMessage(mcb.build());
				}
			}
		}
		if (main.getConfig().isBlackList(event.getSender().getId()))
			return ListeningStatus.LISTENING;
		String msg = event.getMessage().contentToString();

		if(event.getGroup().getId() == 738159283L || event.getGroup().getId() == 951534513L) {
			if(msg.equalsIgnoreCase("#������״̬")) {
				MessageChainBuilder mcb = new MessageChainBuilder();
				mcb.add("/status" + msg.substring(6));
				main.cmdListener.dispitchCommand(new CommandSenderGroup(event.getBot(), event.getGroup(),event.getSender(),event.getSource(),event.getTime()), mcb.build());
			}
		}
		if (event.getGroup().getId() == 241973735L || event.getGroup().getId() == 951534513L) {
			boolean banned = (false);
			
			if(event.getGroup().getId() == 241973735L) {
				boolean permissionAccess = event.getSender().getPermission() == MemberPermission.ADMINISTRATOR ||
						event.getSender().getPermission() == MemberPermission.OWNER
						|| event.getSender().getId() == 2431208142L;
				if(msg.equalsIgnoreCase("/title switch on")) {
					if(permissionAccess) {
						event.getGroup().sendMessage(new QuoteReply(event.getSource()).plus("[DS] �����ñ���Ϊ���� \"on\""));
						Natsuko.a = true;
					}
					else {
						event.getGroup().sendMessage(new QuoteReply(event.getSource()).plus("[DS] Ȩ�޲���"));
					}
				}
				else if(msg.equalsIgnoreCase("/title switch off")) {
					if(permissionAccess) {
						event.getGroup().sendMessage(new QuoteReply(event.getSource()).plus("[DS] �����ñ���Ϊ���� \"off\""));
						Natsuko.a = false;
					}
					else {
						event.getGroup().sendMessage(new QuoteReply(event.getSource()).plus("[DS] Ȩ�޲���"));
					}
				}
			}
			if (msg.trim().equalsIgnoreCase("���ʵ��ǻ�")) {
				if(banned) {
					return ListeningStatus.LISTENING;
				}
				event.getGroup().sendMessage(new At(event.getSender().getId()).plus("\n"
						+ "���㒁��һ����������ʱ�������Ƿ��ܵõ����õĻش�����ȡ�����������ʺ�׷�ʵķ�ʽ��\n" + "�����ʵ��ǻۡ�\n"
						+ "https://github.com/FredWe/How-To-Ask-Questions-The-Smart-Way/blob/master/README-zh_CN.md"));
			}
			if (msg.trim().equalsIgnoreCase("������") || msg.trim().equalsIgnoreCase("����������")) {
				if(banned) {
					return ListeningStatus.LISTENING;
				}
				event.getGroup().sendMessage(new At(event.getSender().getId()).plus("\n"
						+ "https://github.com/MrXiaoM/LivingCityFAQ/blob/master/README.md\n" + "������ӽ�������������? ȥ @ ��ֻ\n"
						+ "QQ �� 2431208142 ��" + event.getGroup().get(2431208142L).getNameCard() + "��Ȼ���ύ����\n"));
			}

			if(banned) {
				return ListeningStatus.LISTENING;
			}
			boolean ifat = false;
			for (SingleMessage m : event.getMessage()) {
				if (m instanceof At) {
					At at = (At) m;
					if (at.getTarget() == event.getBot().getId()) {
						ifat = true;
						break;
					}
				}
			}
			if (ifat) {
				if (msg.contains("�ٶ�")) {
					try {
						
						URL url = new URL("https://s1.ax1x.com/2020/04/07/GgIM3d.jpg");
						HttpURLConnection conn = (HttpURLConnection) url.openConnection();
						conn.setConnectTimeout(5000);
						conn.setReadTimeout(60000);
						InputStream inputStream = conn.getInputStream();
						Image image = event.getGroup().uploadImage(ExternalResource.create(inputStream));
						event.getGroup().sendMessage(new QuoteReply(event.getSource()).plus("\n").plus(image));
					} catch (Throwable t) {
						event.getGroup().sendMessage(new QuoteReply(event.getSource()).plus("\nצ��"));
						t.printStackTrace();
					}
				}
			}
		}
		else if (event.getGroup().getId() == 252631851L) {
			
			for(String key : main.tickTask.tasks.keySet()) {
				Task task = main.tickTask.tasks.get(key);
				if(event.getSender().getId() == task.userId) {
					if(task.subject.equalsIgnoreCase(msg)) {
						main.tickTask.tasks.remove(key);
						event.getGroup().sendMessage(new QuoteReply(event.getSource()).plus(
								"---- Mirai reCAPTCHA\n" +
								" ���ѳɹ�ͨ���˻���֤"));
						break;
					}
				}
			}
			
			if(Util.startsWithIgnoreCase(msg, "/ͼ�����")) {
				MessageChainBuilder mcb = new MessageChainBuilder();
				mcb.add("/verify" + msg.substring(5));
				main.cmdListener.dispitchCommand(new CommandSenderGroup(event.getBot(), event.getGroup(),event.getSender(),event.getSource(),event.getTime()), mcb.build());
			}
		}
		return ListeningStatus.LISTENING;
	}
}
