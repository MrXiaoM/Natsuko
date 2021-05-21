package top.mrxiaom.itisme.commands;

import java.net.Proxy;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.game.MessageType;
import com.github.steveice10.mc.protocol.data.game.entity.player.Hand;
import com.github.steveice10.mc.protocol.data.game.scoreboard.TeamAction;
import com.github.steveice10.mc.protocol.data.game.window.ClickItemParam;
import com.github.steveice10.mc.protocol.data.game.window.WindowAction;
import com.github.steveice10.mc.protocol.data.message.Message;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerChangeHeldItemPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerUseItemPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.window.ClientWindowActionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.scoreboard.ServerTeamPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerOpenWindowPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerWindowItemsPacket;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.tcp.TcpSession;
import com.github.steveice10.packetlib.tcp.TcpSessionFactory;

import ch.jamiete.mcping.MinecraftPing;
import ch.jamiete.mcping.MinecraftPingReply;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.MiraiConsole;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.internal.QQAndroidBot;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.utils.MiraiLogger;
import top.mrxiaom.itisme.Natsuko;

@Deprecated
public class Server {
	Natsuko main;
	Client client;
	
    private String HOST = "mc.66ko.cc";
    private static final int PORT = 25565;
    private static final Proxy PROXY = Proxy.NO_PROXY;
    private static final Proxy AUTH_PROXY = Proxy.NO_PROXY;
    private static final String USERNAME = "Peput";
    private static final String PEPUT_PASSWORD = "minecraft233";
    Servers current = Servers.offline;
    enum Servers{
    	offline("未连接"),
    	lobby("lobby","大厅服"), 
    	sc("sc","生存服"), 
    	kd("kd","空岛服"), 
    	zd("zd","钻石大陆"),
    	afkgjsc("afkgjsc","挂机服-生存"),
    	afkgj("afkgj","挂机服-空岛"),
    	afkgjzd("afkgjzd","挂机服-钻大"), unknown("未知");
    	
        private String name;
        private String stp;
        private Servers(String name) {
            this.name = name;
        }
        private Servers(String stp, String name) {
        	this.stp = stp;
            this.name = name;
        }

        public String getChineseName() {
            return name;
        }
        public String getStpName() {
            return stp;
        }
        
    }
    long nexttime = 0;

	long nexttime_login = 0;
	long times = 0;
	boolean afkFlag = false;
	boolean sendAfk = false;
	int waitTime = 0;
	MiraiLogger logger;
	public Server(Natsuko main) {
		this.main = main;
		this.logger = MiraiConsole.INSTANCE.createLogger("Minecraft");
		initClient();
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				if(current != Servers.offline) {
					if(client!=null) {
						if(!client.getSession().isConnected()) {
							current = Servers.offline;
						}
					}
					else {
						if(sendAfk) {
							if(waitTime >= 3) {
	                    		client.getSession().send(new ClientChatPacket("/stp afkgjsc"));
								sendAfk = false;
								waitTime = 0;
							}
							else waitTime++;
						}
					}
				}
				// 15分钟无响应
				if(times >= 300) {
					if(current == Servers.offline) {
						initClient();
						client.getSession().connect();
						afkFlag = true;
					}
					else{
						if(current != Servers.afkgjsc)
							sendAfk = true;
					}
					times = 0;
				}
				times++;
			}
			
		}, 10 * 6000, 3 * 1000);
	}
	
	public Client getClient() {
		return this.client;
	}
	
	private void info(String s) {
		logger.info(s);
	}
	
	private void initClient() {
        MinecraftProtocol protocol = new MinecraftProtocol(USERNAME);
        
        this.client = new Client(HOST, PORT, protocol, new TcpSessionFactory(PROXY));
        client.getSession().setFlag(MinecraftConstants.AUTH_PROXY_KEY, AUTH_PROXY);
        client.getSession().addListener(new SessionAdapter() {
            @Override
            public void packetReceived(PacketReceivedEvent event) {
        		Calendar now = Calendar.getInstance();
            	long nowtime = now.get(Calendar.HOUR_OF_DAY) * 3600 + now.get(Calendar.MINUTE) * 60
        				+ now.get(Calendar.SECOND);
            	if(nexttime_login - nowtime > 20)
            		nexttime_login = 0;
                if(event.getPacket() instanceof ServerJoinGamePacket) {
                	info("[MIRAI] "+USERNAME + " 加入了游戏");
                    //event.getSession().send(new ClientChatPacket(""));
                } else if(event.getPacket() instanceof ServerChatPacket) {
                	// TODO 聊天消息 
                	ServerChatPacket chatPacket = event.<ServerChatPacket>getPacket();
                	// 如果类型是屏幕中间的提示 (差不多是唱片机播放信息的位置)
                	if(chatPacket.getType() == MessageType.NOTIFICATION)
                		return; // 不执行
                    Message message = chatPacket.getMessage();
                    info("[CHAT] " + message.getFullText());
                    
                    if(message.getFullText().equalsIgnoreCase("生存都市 >> 请输入 /L <密码> 进行登入.")) {
                    	if(nowtime > nexttime_login) {
                    		event.getSession().send(new ClientChatPacket("/login " + PEPUT_PASSWORD));
                    		nexttime_login = nowtime + 20;
                    	}
                    }
                    if(message.getFullText().equalsIgnoreCase("生存都市 >> 登入成功,正在匹配大厅...")) {
                    	info("[MIRAI] " + USERNAME + "已经自动登录成功了");
                    	if(afkFlag) {
                    		sendAfk = true;
                    		afkFlag = false;
                    	}else{
                        	sendGroupMessage("[Peput] Peput成功登录到生存都市服务器了!");
                    	}
                    }
                    if(message.getFullText().startsWith("§a" + USERNAME + " -> ")) {
                    	if(message.getFullText().contains(":")) {
                    		String omsg = message.getFullText();
                    		// 玩家名称
                    		String mplayer = omsg.substring(omsg.indexOf(">") + 2, omsg.indexOf(":"));
                    		// 私聊内容
                    		String msg = omsg.substring(omsg.indexOf(":")+2);
                    		info("[MSG]["+current.getChineseName()+"][ " + mplayer + " -> 我 ] [" + msg + "]");
                    		
                    	}
                    }
                    if (message.getFullText().startsWith("已从") 
                    		&& message.getFullText().contains("接收")) {
        				//String player = message.getFullText().substring(message.getFullText().indexOf("已从") + 3, message.getFullText().indexOf("接收") - 1);
        				String s = message.getFullText().substring(message.getFullText().indexOf("接收") + 3);
        				if(s.contains(".")) {
        					s = s.substring(0,s.indexOf("."));
        				}
        				//int money = Util.strToInt(s, -1);
        			}
                    if(message.getFullText().contains(" ")) {
                    	//格式: Player 玩家名称 has been online/offline since 时间
                    	String[] args = message.getFullText().split(" ");
                    	if(args.length > 5) {
                    		if(args[0].equalsIgnoreCase("player")
                    		&& args[2].equalsIgnoreCase("has")
                    		&& args[3].equalsIgnoreCase("been")) {
                    			String mplayer = args[1];
                    			boolean online = args[4].equalsIgnoreCase("online");
                    			String time = "";
                    			for(int i = 6; i < args.length; i++) {
                    				time = time + args[i] + (i-1==args.length?"":" ");
                    			}
                    			if(time.length() >= 2)
                    				time = time.substring(0, time.length() - 2);
                    			sendGroupMessage("["+current.getChineseName()+"][Peput] 玩家 " + mplayer + " 已经" 
                    					+ (online?"在线":"离线") + " " + time + " 了");
                    		}
                    	}
                    }
                    String baltext = clearAllLetter_(message.getFullText()).replace("?", "");
                    if(baltext.startsWith("["))
                    	baltext = baltext.substring(baltext.indexOf("]")+1);
                    if(baltext.startsWith("「"))
                    	baltext = baltext.substring(baltext.indexOf("」")+1);
                    if(baltext.startsWith("的金钱:")) {
                    	sendGroupMessage("["+current.getChineseName()+"][Peput] " + message.getFullText().replace("?", ""));
                    }
                    if(message.getFullText().startsWith("错误:")) {
                    	sendGroupMessage("["+current.getChineseName()+"][Peput] " + message.getFullText());
                    }
                    if(message.getFullText().startsWith("你没有使用该命令的权限")) {
                    	sendGroupMessage("["+current.getChineseName()+"][Peput] " + message.getFullText());
                    }
                    if(message.getFullText().startsWith("[Alert]")) {
                    	if(lastAlert != message.getFullText()) {
                    		lastAlert = message.getFullText();
                    		sendGroupMessage("[Peput] " + message.getFullText());
                    	}
                    }
                    if(message.getFullText().equalsIgnoreCase("unknown command. type \"/help\" for help.")
                    		|| message.getFullText().equalsIgnoreCase("生存都市>> 未知的游戏命令")) {
                    	sendGroupMessage("["+current.getChineseName()+"][Peput] 未知命令 (也有可能是当前子服不支持该命令)");
                    }
                    if(message.getFullText().startsWith("["+current.getChineseName()+"][Peput] 服务器TPS:")) {
                    	sendGroupMessage(message.getFullText().replace("§a", ""));
                    }
                    if(message.getFullText().startsWith("[跨服] 你没有权限")) {
                    	openStpMenu();
                    	sendGroupMessage("["+current.getChineseName()+"][Peput] Peput 没有权限传送到其他子服\n"
                    			+ "正在努力用咸鱼传送到生存…");
                    }
                    if(stping) {
                    	if(message.getFullText().startsWith("§6§l生存都市§f§l>>§a§l§n点击打开")) {
                    		stping = false;
                    		sendGroupMessage("["+current.getChineseName()+"][Peput] Peput成功传送到生存服!\n你可以继续用 /server stp [服区] 来把我传送到其他地方了");
                    	}
                    }
                    //if(message instanceof TranslationMessage) {
                    //    info("Received Translation Components: " + Arrays.toString(((TranslationMessage) message).getTranslationParams()));
                    //}

                    //event.getSession().disconnect("Finished");
                }
                else if(event.getPacket() instanceof ServerOpenWindowPacket) {	
                	// TODO 窗口打开事件
                	ServerOpenWindowPacket packet = event.<ServerOpenWindowPacket>getPacket();
            		//info("打开了一个窗口，名称是: " + packet.getName());
                	if(packet.getName().equalsIgnoreCase("{\"text\":\"§0选择区服\"}")) {
                		stping = true;
                		nowWindowId = packet.getWindowId();
                		//info("窗口Id: " + packet.getWindowId());
                	}
                }
                else if (event.getPacket() instanceof ServerWindowItemsPacket) {
                	// TODO 窗口物品更新事件
                	ServerWindowItemsPacket packet = event.<ServerWindowItemsPacket>getPacket();
            		//info("检测到窗口物品更新，窗口Id: " + packet.getWindowId());
                	if(packet.getWindowId() == nowWindowId) {
                		//info("与选择服区窗口的Id吻合，开始执行点击");
                		int delay_seconds = 3;
                		// 等待3秒后执行
            			Timer timer = new Timer();
            			timer.schedule(new TimerTask() {
							@Override
							public void run() {
								int windowId = nowWindowId;
								nowWindowId = -1;
								//for(int i = 0; i < packet.getItems().length; i++) {
								//	if(packet.getItems()[i]!=null)
								//		info("["+i+"] "+ packet.getItems()[i].getId());
								//}
								if(packet.getItems()[20] == null) {
									sendGroupMessage("["+current.getChineseName()+"][Peput] Peput无法获取到传送按钮的位置…");
									return;
								}
								// 发 WindowClick 行为，按下序号为 20 的格子
		            			client.getSession().send(new ClientWindowActionPacket(windowId, 0, 20, 
		            				packet.getItems()[20], WindowAction.CLICK_ITEM, ClickItemParam.LEFT_CLICK));
							}
            			}, delay_seconds * 1000);
                	}
                } else if (event.getPacket() instanceof ServerTeamPacket) {
                	// TODO 记分板获取
                	
                	ServerTeamPacket packet = event.getPacket();
                	if(packet.getAction() == TeamAction.UPDATE) { // 检测记分板更新
            			String text = packet.getPrefix() + packet.getSuffix();
            			// 判断第6行 (从下往上数)
                		if(packet.getDisplayName().equalsIgnoreCase("MiaoboardLine6")) {
                			boolean unknown = true;
                			// 因为公会名是可变的，所以要优先判断
                			if(text.contains("公会")) {
                 				// 在钻大内
                 				current = Servers.zd;
                 				unknown = false;
                 			}
                			else if(text.contains("联通")) {
                				// 在大厅内
                				current = Servers.lobby; 
                 				unknown = false;
                			}
                			else if(text.contains("位置")) {
                				// 在生存内
                				current = Servers.sc; 
                 				unknown = false;
                			}
                			
                			else if(text.contains("金币")) {
                				// 在空岛内
                				current = Servers.kd;
                 				unknown = false;
                			}
                			// 挂机服
                			else if(text.contains("输入")) {
                				if(text.contains("sc")) {
                					// 生存挂机服
                    				current = Servers.afkgjsc; 
                     				unknown = false;
                				}
                				if(text.contains("kd")) {
                					// 空岛挂机服
                    				current = Servers.afkgj; 
                     				unknown = false;
                				}
                				if(text.contains("zd")) {
                					// 钻大挂机服
                    				current = Servers.afkgjzd; 
                     				unknown = false;
                				}
                			}
                			if(unknown) {
                    			current = Servers.unknown;
                    		}
                		}
                		
                		//info(current.getChineseName());
                	}
                }
            }

            @Override
            public void disconnected(DisconnectedEvent event) {
            	String reason = Message.fromString(event.getReason()).getFullText();
                info("Disconnected: " + reason);
                if(reason.contains("timed out")) {
                	reason = "连接超时";
                }
                
                if(event.getCause() != null) {
                    event.getCause().printStackTrace();
                }
                sendGroupMessage("[Peput] Peput从服务器断开连接了… 原因:\n"
                		+ reason + (event.getCause()!=null?("\n"+event.getCause().getMessage()):""));
            }
        });
	}
	String lastAlert = "";
	boolean stping = false;
	private String clearAllLetter_(String o) {
		return o.toLowerCase()
				.replace("a", "")
				.replace("b", "")
				.replace("c", "")
				.replace("d", "")
				.replace("e", "")
				.replace("f", "")
				.replace("g", "")
				.replace("h", "")
				.replace("i", "")
				.replace("j", "")
				.replace("k", "")
				.replace("l", "")
				.replace("m", "")
				.replace("n", "")
				.replace("o", "")
				.replace("p", "")
				.replace("q", "")
				.replace("r", "")
				.replace("s", "")
				.replace("t", "")
				.replace("u", "")
				.replace("v", "")
				.replace("w", "")
				.replace("x", "")
				.replace("y", "")
				.replace("z", "")
				.replace("_", "")
				.replace("-", "")
				.replace("0", "")
				.replace("1", "")
				.replace("2", "")
				.replace("3", "")
				.replace("4", "")
				.replace("5", "")
				.replace("6", "")
				.replace("7", "")
				.replace("8", "")
				.replace("9", "");
	}
	
	private void sendGroupMessage(String msg) {
		MessageChainBuilder mcb = new MessageChainBuilder();
		mcb.add(msg);
		this.sendGroupMessage(mcb.build());
	}
	
	private void sendGroupMessage(MessageChain msg) {
		QQAndroidBot bot = (QQAndroidBot) Bot.getInstance(3191684705L);
		if (bot != null) {
			Group group = bot.getGroup(241973735L);
			if (group != null) {
				group.sendMessage(msg);
			}
		}
	}
	
	int cooldownTime = 6;
	
	public void onCommand(GroupMessageEvent event) {
		QuoteReply quote = new QuoteReply(event.getSource());
		if(stping) {
			event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] 正在执行传送到子服脚本，请等待执行完毕"));
			return;
		}
		Calendar now = Calendar.getInstance();
		long nowtime = now.get(Calendar.HOUR_OF_DAY) * 3600 + now.get(Calendar.MINUTE) * 60
				+ now.get(Calendar.SECOND);
		if ((nexttime - nowtime) > cooldownTime+1) {
			nexttime = 0;
		}
		if (nowtime < nexttime && event.getSender().getId() != 2431208142L) {
			event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] 请等待命令冷却时间结束 ("+(nexttime-nowtime)+"s)"));
			return;
		}
		String msg = event.getMessage().contentToString();
		if (msg.contains(" ")) {
			String temp = msg;
			while (temp.contains("  ")) {
				temp = temp.replace("  ", " ");
			}
			String[] args = temp.split(" ");
			if(!args[0].equalsIgnoreCase("/server")) {
				return;
			}
			times = 0;
			if(args.length == 2) {
				if(args[1].equalsIgnoreCase("status")) {
					String string = "";
					try {
						MinecraftPingReply mc = new MinecraftPing().getPing("mc.66ko.cc");
						String desc = mc.getDescription();
						while(desc.contains("  ")) {
							desc = desc.replace("  ", " ");
						}
						String online =  mc.getPlayers().getOnline() + "/" + mc.getPlayers().getMax();
						string += "    生存都市服务器 ★ mc.66ko.cc\n";
						string += "--------------------------------["+ mc.getDelay() +"ms]-\n";
						string += "" + desc + "" + "\n";
						string += " ◇ 在线: " + online + "\n";
					} catch (Throwable e) {
						string = "在获取服务器信息时出现了一个错误\n" + e.toString();
						logger.error("在获取服务器信息时出现了一个错误", e);
					}
					event.getGroup().sendMessage(new QuoteReply(event.getSource()).plus(string));
					return;
				}
				if(args[1].equalsIgnoreCase("connect")) {
					if(!client.getSession().isConnected()) {
						if(((TcpSession)client.getSession()).isDisconnected()) {
							initClient();
						}
						event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] 正在连接到服务器"));
						client.getSession().connect();
						nexttime = nowtime + cooldownTime;
						return;
					}
					else {
						event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] Peput已经连接到服务器了! 不需要再请求连接"));
						nexttime = nowtime + cooldownTime;
						return;
					}
				}
				if(args[1].equalsIgnoreCase("tps")) {
					if(client.getSession().isConnected()) {
						if(current.equals(Servers.lobby)) {
							event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] 你个憨批，"
									+ "在登入服执行这些命令有啥用?"));
							return;
						}
						event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] Peput已发送命令:\n"
								+ "/tps (部分子服无法使用)"));
						client.getSession().send(new ClientChatPacket("/ping ["+current.getChineseName()+"][Peput] 服务器TPS: %server_tps%"));
						nexttime = nowtime + cooldownTime;
						return;
					}
					else {
						event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] Peput还没连接到服务器呢，请先连接服务器吧!\n"
								+ "(命令 /server connect)"));
						nexttime = nowtime + cooldownTime;
						return;
					}
				}
				event.getGroup().sendMessage(quote.plus(this.getHelp(event.getSender().getId() == 2431208142L)));
				return;
			}
			if(args.length == 3) {
				String command = args[1];
				String value = args[2];
				if(command.equalsIgnoreCase("connect")) {					
					if(!client.getSession().isConnected()) {
						if(value.equalsIgnoreCase("mc") || value.equalsIgnoreCase("dx")
						 ||value.equalsIgnoreCase("lt") || value.equalsIgnoreCase("yd")) {
							this.HOST = value.toLowerCase() + ".66ko.cc";
						}
						else {
							event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] Peput找不到你所请求的线路"));
							return;
						}
						initClient();
						event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] 正在连接到服务器"));
						client.getSession().connect();
						nexttime = nowtime + cooldownTime;
						return;
					}
					else {
						event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] Peput已经连接到服务器了! 不需要再请求连接"));
						nexttime = nowtime + cooldownTime;
						return;
					}
				}
				if(!client.getSession().isConnected()) {
					event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] Peput还没连接到服务器呢，请先连接服务器吧!\n"
							+ "(命令 /server connect)"));
					return;
				}
				if(command.equalsIgnoreCase("stp")) {
					if(value.length() > 8) {
						event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] 子服名称过长"));
						return;
					}
					if(value.equalsIgnoreCase("sk")) {
						event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] 要是进了空岛战争我就出不来了 xwx"));
						return;
					}
					if(value.equalsIgnoreCase("lobby") || value.equalsIgnoreCase("test")) {
						event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] Peput 不想回到大厅或者登入服"));
						return;
					}
					client.getSession().send(new ClientChatPacket("/stp " + value));
					event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] Peput已发送命令:\n"
							+ "/stp " + value));
					nexttime = nowtime + cooldownTime;
					return;
				}
				if(command.equalsIgnoreCase("bal")) {
					if(current.equals(Servers.lobby)) {
						event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] 你个憨批，"
								+ "在登入服执行这些命令有啥用?"));
						return;
					}
					if(isPlayerName(value) && (value.length() >= 3 && value.length() <= 16)) {
						client.getSession().send(new ClientChatPacket("/bal " + value));
						//event.getGroup().sendMessage(quote.plus("[Peput] Peput已发送命令:\n"
						//		+ "/bal " + value));
						nexttime = nowtime + cooldownTime;
						return;
					}
					else {
						event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] Peput认为那样的玩家名称服务器是不认的"));
						return;
					}
				}
				if(command.equalsIgnoreCase("seen")) {
					if(current.equals(Servers.lobby)) {
						event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] 你个憨批，"
								+ "在登入服执行这些命令有啥用?"));
						return;
					}
					if(isPlayerName(value) && (value.length() >= 3 && value.length() <= 16)) {
						client.getSession().send(new ClientChatPacket("/seen " + value));
						//event.getGroup().sendMessage(quote.plus("[Peput] Peput已发送命令:\n"
						//		+ "/seen " + value));
						nexttime = nowtime + cooldownTime;
						return;
					}
					else {
						event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] Peput认为那样的玩家名称服务器是不认的"));
						return;
					}
				}
				
			}
			
				if(event.getSender().getId() == 2431208142L) {
					if(args.length >= 3) {
						String command = args[1];
						if(command.equalsIgnoreCase("send")) {
							if(!client.getSession().isConnected()) {
								event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] Peput还没连接到服务器呢，请先连接服务器吧!\n"
										+ "(命令 /server connect)"));
								return;
							}
							String value = "";
							for(int i = 2; i < args.length; i++) {
								value = value + args[i] + ((i==args.length-1)?"":" ");
							}
							client.getSession().send(new ClientChatPacket(value));
							event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] Peput已执行你的命令"));
							return;
						}
					}
					else {
						event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] 参数不足"));
						return;
					}
				}
			
		}
		else if(msg.equalsIgnoreCase("/server")) {
			times = 0;
			event.getGroup().sendMessage(quote.plus(this.getHelp(event.getSender().getId() == 2431208142L)));
		}
	}
	int nowWindowId = -1;
	private void openStpMenu() {
		// 第一步，选择物品栏上第5格
		client.getSession().send(new ClientPlayerChangeHeldItemPacket(4));
		// 第二步，右键
		client.getSession().send(new ClientPlayerUseItemPacket(Hand.MAIN_HAND));
		// 接下来的交给收包处处理
	}
	
	public boolean isPlayerName(String name) {
		Pattern p = Pattern.compile("[a-zA-Z0-9_]*");
        Matcher m = p.matcher(name);
		return m.matches();
	}
	
	public String getHelp(boolean admin) {
		return ""+"        - [Peput] 帮助命令 -\n"
				+ "  /server - 查看Peput帮助命令\n"
				+ "  /server status - 查看服务器状态"
				+ "  /server connect - 让Peput连接服务器\n"
				+ "  /server stp [服区] - 传送到某服区\n"
				+ "  /server bal [玩家] - 查看玩家金钱\n"
				+ "  /server seen [玩家] - 查看玩家在线情况\n"
				+ "  /server tps - 查看服务器TPS(仅部分子服)"
				+ (admin?"\n  /server send [内容] - 发送一条聊天消息":"");
	}
}
