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
    	offline("??????"),
    	lobby("lobby","??????"), 
    	sc("sc","??????"), 
    	kd("kd","??????"), 
    	zd("zd","????????"),
    	afkgjsc("afkgjsc","??????-????"),
    	afkgj("afkgj","??????-????"),
    	afkgjzd("afkgjzd","??????-????"), unknown("????");
    	
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
				// 15??????????
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
                	info("[MIRAI] "+USERNAME + " ??????????");
                    //event.getSession().send(new ClientChatPacket(""));
                } else if(event.getPacket() instanceof ServerChatPacket) {
                	// TODO ???????? 
                	ServerChatPacket chatPacket = event.<ServerChatPacket>getPacket();
                	// ???????????????????????? (????????????????????????????)
                	if(chatPacket.getType() == MessageType.NOTIFICATION)
                		return; // ??????
                    Message message = chatPacket.getMessage();
                    info("[CHAT] " + message.getFullText());
                    
                    if(message.getFullText().equalsIgnoreCase("???????? >> ?????? /L <????> ????????.")) {
                    	if(nowtime > nexttime_login) {
                    		event.getSession().send(new ClientChatPacket("/login " + PEPUT_PASSWORD));
                    		nexttime_login = nowtime + 20;
                    	}
                    }
                    if(message.getFullText().equalsIgnoreCase("???????? >> ????????,????????????...")) {
                    	info("[MIRAI] " + USERNAME + "??????????????????");
                    	if(afkFlag) {
                    		sendAfk = true;
                    		afkFlag = false;
                    	}else{
                        	sendGroupMessage("[Peput] Peput??????????????????????????!");
                    	}
                    }
                    if(message.getFullText().startsWith("??a" + USERNAME + " -> ")) {
                    	if(message.getFullText().contains(":")) {
                    		String omsg = message.getFullText();
                    		// ????????
                    		String mplayer = omsg.substring(omsg.indexOf(">") + 2, omsg.indexOf(":"));
                    		// ????????
                    		String msg = omsg.substring(omsg.indexOf(":")+2);
                    		info("[MSG]["+current.getChineseName()+"][ " + mplayer + " -> ?? ] [" + msg + "]");
                    		
                    	}
                    }
                    if (message.getFullText().startsWith("????") 
                    		&& message.getFullText().contains("????")) {
        				//String player = message.getFullText().substring(message.getFullText().indexOf("????") + 3, message.getFullText().indexOf("????") - 1);
        				String s = message.getFullText().substring(message.getFullText().indexOf("????") + 3);
        				if(s.contains(".")) {
        					s = s.substring(0,s.indexOf("."));
        				}
        				//int money = Util.strToInt(s, -1);
        			}
                    if(message.getFullText().contains(" ")) {
                    	//????: Player ???????? has been online/offline since ????
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
                    			sendGroupMessage("["+current.getChineseName()+"][Peput] ???? " + mplayer + " ????" 
                    					+ (online?"????":"????") + " " + time + " ??");
                    		}
                    	}
                    }
                    String baltext = clearAllLetter_(message.getFullText()).replace("?", "");
                    if(baltext.startsWith("["))
                    	baltext = baltext.substring(baltext.indexOf("]")+1);
                    if(baltext.startsWith("??"))
                    	baltext = baltext.substring(baltext.indexOf("??")+1);
                    if(baltext.startsWith("??????:")) {
                    	sendGroupMessage("["+current.getChineseName()+"][Peput] " + message.getFullText().replace("?", ""));
                    }
                    if(message.getFullText().startsWith("????:")) {
                    	sendGroupMessage("["+current.getChineseName()+"][Peput] " + message.getFullText());
                    }
                    if(message.getFullText().startsWith("??????????????????????")) {
                    	sendGroupMessage("["+current.getChineseName()+"][Peput] " + message.getFullText());
                    }
                    if(message.getFullText().startsWith("[Alert]")) {
                    	if(lastAlert != message.getFullText()) {
                    		lastAlert = message.getFullText();
                    		sendGroupMessage("[Peput] " + message.getFullText());
                    	}
                    }
                    if(message.getFullText().equalsIgnoreCase("unknown command. type \"/help\" for help.")
                    		|| message.getFullText().equalsIgnoreCase("????????>> ??????????????")) {
                    	sendGroupMessage("["+current.getChineseName()+"][Peput] ???????? (??????????????????????????????)");
                    }
                    if(message.getFullText().startsWith("["+current.getChineseName()+"][Peput] ??????TPS:")) {
                    	sendGroupMessage(message.getFullText().replace("??a", ""));
                    }
                    if(message.getFullText().startsWith("[????] ??????????")) {
                    	openStpMenu();
                    	sendGroupMessage("["+current.getChineseName()+"][Peput] Peput ??????????????????????\n"
                    			+ "??????????????????????????");
                    }
                    if(stping) {
                    	if(message.getFullText().startsWith("??6??l??????????f??l>>??a??l??n????????")) {
                    		stping = false;
                    		sendGroupMessage("["+current.getChineseName()+"][Peput] Peput????????????????!\n???????????? /server stp [????] ??????????????????????");
                    	}
                    }
                    //if(message instanceof TranslationMessage) {
                    //    info("Received Translation Components: " + Arrays.toString(((TranslationMessage) message).getTranslationParams()));
                    //}

                    //event.getSession().disconnect("Finished");
                }
                else if(event.getPacket() instanceof ServerOpenWindowPacket) {	
                	// TODO ????????????
                	ServerOpenWindowPacket packet = event.<ServerOpenWindowPacket>getPacket();
            		//info("??????????????????????: " + packet.getName());
                	if(packet.getName().equalsIgnoreCase("{\"text\":\"??0????????\"}")) {
                		stping = true;
                		nowWindowId = packet.getWindowId();
                		//info("????Id: " + packet.getWindowId());
                	}
                }
                else if (event.getPacket() instanceof ServerWindowItemsPacket) {
                	// TODO ????????????????
                	ServerWindowItemsPacket packet = event.<ServerWindowItemsPacket>getPacket();
            		//info("????????????????????????Id: " + packet.getWindowId());
                	if(packet.getWindowId() == nowWindowId) {
                		//info("????????????????Id??????????????????");
                		int delay_seconds = 3;
                		// ????3????????
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
									sendGroupMessage("["+current.getChineseName()+"][Peput] Peput??????????????????????????");
									return;
								}
								// ?? WindowClick ???????????????? 20 ??????
		            			client.getSession().send(new ClientWindowActionPacket(windowId, 0, 20, 
		            				packet.getItems()[20], WindowAction.CLICK_ITEM, ClickItemParam.LEFT_CLICK));
							}
            			}, delay_seconds * 1000);
                	}
                } else if (event.getPacket() instanceof ServerTeamPacket) {
                	// TODO ??????????
                	
                	ServerTeamPacket packet = event.getPacket();
                	if(packet.getAction() == TeamAction.UPDATE) { // ??????????????
            			String text = packet.getPrefix() + packet.getSuffix();
            			// ??????6?? (??????????)
                		if(packet.getDisplayName().equalsIgnoreCase("MiaoboardLine6")) {
                			boolean unknown = true;
                			// ??????????????????????????????????
                			if(text.contains("????")) {
                 				// ????????
                 				current = Servers.zd;
                 				unknown = false;
                 			}
                			else if(text.contains("????")) {
                				// ????????
                				current = Servers.lobby; 
                 				unknown = false;
                			}
                			else if(text.contains("????")) {
                				// ????????
                				current = Servers.sc; 
                 				unknown = false;
                			}
                			
                			else if(text.contains("????")) {
                				// ????????
                				current = Servers.kd;
                 				unknown = false;
                			}
                			// ??????
                			else if(text.contains("????")) {
                				if(text.contains("sc")) {
                					// ??????????
                    				current = Servers.afkgjsc; 
                     				unknown = false;
                				}
                				if(text.contains("kd")) {
                					// ??????????
                    				current = Servers.afkgj; 
                     				unknown = false;
                				}
                				if(text.contains("zd")) {
                					// ??????????
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
                	reason = "????????";
                }
                
                if(event.getCause() != null) {
                    event.getCause().printStackTrace();
                }
                sendGroupMessage("[Peput] Peput???????????????????? ????:\n"
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
			event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] ??????????????????????????????????????"));
			return;
		}
		Calendar now = Calendar.getInstance();
		long nowtime = now.get(Calendar.HOUR_OF_DAY) * 3600 + now.get(Calendar.MINUTE) * 60
				+ now.get(Calendar.SECOND);
		if ((nexttime - nowtime) > cooldownTime+1) {
			nexttime = 0;
		}
		if (nowtime < nexttime && event.getSender().getId() != 2431208142L) {
			event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] ?????????????????????? ("+(nexttime-nowtime)+"s)"));
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
						string += "    ?????????????? ?? mc.66ko.cc\n";
						string += "--------------------------------["+ mc.getDelay() +"ms]-\n";
						string += "" + desc + "" + "\n";
						string += " ?? ????: " + online + "\n";
					} catch (Throwable e) {
						string = "????????????????????????????????\n" + e.toString();
						logger.error("????????????????????????????????", e);
					}
					event.getGroup().sendMessage(new QuoteReply(event.getSource()).plus(string));
					return;
				}
				if(args[1].equalsIgnoreCase("connect")) {
					if(!client.getSession().isConnected()) {
						if(((TcpSession)client.getSession()).isDisconnected()) {
							initClient();
						}
						event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] ????????????????"));
						client.getSession().connect();
						nexttime = nowtime + cooldownTime;
						return;
					}
					else {
						event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] Peput??????????????????! ????????????????"));
						nexttime = nowtime + cooldownTime;
						return;
					}
				}
				if(args[1].equalsIgnoreCase("tps")) {
					if(client.getSession().isConnected()) {
						if(current.equals(Servers.lobby)) {
							event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] ??????????"
									+ "???????????????????????????"));
							return;
						}
						event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] Peput??????????:\n"
								+ "/tps (????????????????)"));
						client.getSession().send(new ClientChatPacket("/ping ["+current.getChineseName()+"][Peput] ??????TPS: %server_tps%"));
						nexttime = nowtime + cooldownTime;
						return;
					}
					else {
						event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] Peput????????????????????????????????????!\n"
								+ "(???? /server connect)"));
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
							event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] Peput????????????????????"));
							return;
						}
						initClient();
						event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] ????????????????"));
						client.getSession().connect();
						nexttime = nowtime + cooldownTime;
						return;
					}
					else {
						event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] Peput??????????????????! ????????????????"));
						nexttime = nowtime + cooldownTime;
						return;
					}
				}
				if(!client.getSession().isConnected()) {
					event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] Peput????????????????????????????????????!\n"
							+ "(???? /server connect)"));
					return;
				}
				if(command.equalsIgnoreCase("stp")) {
					if(value.length() > 8) {
						event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] ????????????"));
						return;
					}
					if(value.equalsIgnoreCase("sk")) {
						event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] ???????????????????????????? xwx"));
						return;
					}
					if(value.equalsIgnoreCase("lobby") || value.equalsIgnoreCase("test")) {
						event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] Peput ??????????????????????"));
						return;
					}
					client.getSession().send(new ClientChatPacket("/stp " + value));
					event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] Peput??????????:\n"
							+ "/stp " + value));
					nexttime = nowtime + cooldownTime;
					return;
				}
				if(command.equalsIgnoreCase("bal")) {
					if(current.equals(Servers.lobby)) {
						event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] ??????????"
								+ "???????????????????????????"));
						return;
					}
					if(isPlayerName(value) && (value.length() >= 3 && value.length() <= 16)) {
						client.getSession().send(new ClientChatPacket("/bal " + value));
						//event.getGroup().sendMessage(quote.plus("[Peput] Peput??????????:\n"
						//		+ "/bal " + value));
						nexttime = nowtime + cooldownTime;
						return;
					}
					else {
						event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] Peput????????????????????????????????"));
						return;
					}
				}
				if(command.equalsIgnoreCase("seen")) {
					if(current.equals(Servers.lobby)) {
						event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] ??????????"
								+ "???????????????????????????"));
						return;
					}
					if(isPlayerName(value) && (value.length() >= 3 && value.length() <= 16)) {
						client.getSession().send(new ClientChatPacket("/seen " + value));
						//event.getGroup().sendMessage(quote.plus("[Peput] Peput??????????:\n"
						//		+ "/seen " + value));
						nexttime = nowtime + cooldownTime;
						return;
					}
					else {
						event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] Peput????????????????????????????????"));
						return;
					}
				}
				
			}
			
				if(event.getSender().getId() == 2431208142L) {
					if(args.length >= 3) {
						String command = args[1];
						if(command.equalsIgnoreCase("send")) {
							if(!client.getSession().isConnected()) {
								event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] Peput????????????????????????????????????!\n"
										+ "(???? /server connect)"));
								return;
							}
							String value = "";
							for(int i = 2; i < args.length; i++) {
								value = value + args[i] + ((i==args.length-1)?"":" ");
							}
							client.getSession().send(new ClientChatPacket(value));
							event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] Peput??????????????"));
							return;
						}
					}
					else {
						event.getGroup().sendMessage(quote.plus("["+current.getChineseName()+"][Peput] ????????"));
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
		// ??????????????????????5??
		client.getSession().send(new ClientPlayerChangeHeldItemPacket(4));
		// ????????????
		client.getSession().send(new ClientPlayerUseItemPacket(Hand.MAIN_HAND));
		// ??????????????????????
	}
	
	public boolean isPlayerName(String name) {
		Pattern p = Pattern.compile("[a-zA-Z0-9_]*");
        Matcher m = p.matcher(name);
		return m.matches();
	}
	
	public String getHelp(boolean admin) {
		return ""+"        - [Peput] ???????? -\n"
				+ "  /server - ????Peput????????\n"
				+ "  /server status - ??????????????"
				+ "  /server connect - ??Peput??????????\n"
				+ "  /server stp [????] - ????????????\n"
				+ "  /server bal [????] - ????????????\n"
				+ "  /server seen [????] - ????????????????\n"
				+ "  /server tps - ??????????TPS(??????????)"
				+ (admin?"\n  /server send [????] - ????????????????":"");
	}
}
