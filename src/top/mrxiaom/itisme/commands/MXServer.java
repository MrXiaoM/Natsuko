package top.mrxiaom.itisme.commands;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.Type;

import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.SubProtocol;
import com.github.steveice10.mc.protocol.data.message.Message;
import com.github.steveice10.mc.protocol.data.status.ServerStatusInfo;
import com.github.steveice10.mc.protocol.data.status.handler.ServerInfoHandler;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.tcp.TcpSession;
import com.github.steveice10.packetlib.tcp.TcpSessionFactory;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.message.data.SingleMessage;
import net.mamoe.mirai.utils.ExternalResource;
import top.mrxiaom.itisme.Natsuko;
import top.mrxiaom.itisme.Util;
import top.mrxiaom.miraiutils.CommandModel;
import top.mrxiaom.miraiutils.CommandSender;
import top.mrxiaom.miraiutils.CommandSenderGroup;

public class MXServer extends CommandModel {
	Natsuko main;

	public MXServer(Natsuko main) {
		super("server");
		this.main = main;
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

	boolean running = false;
	Run runningTask;

	class Run implements Runnable {
		String ip;
		int checkedPort;
		int currentPort;
		public int minPort;
		public int maxPort;
		List<Integer> ports = new ArrayList<Integer>();

		public Run(String ip, int minPort, int maxPort) {
			this.ip = ip;
			this.minPort = minPort;
			this.maxPort = maxPort;

			checkedPort = minPort;
			currentPort = minPort - 1;
		}

		@Override
		public void run() {
		}
	}

	private String getHelp() {
		return "   -- ServerHelper 帮助命令 --   \n" + " ◇ /server - 查看此帮助\n" + " ◇ /server ping [地址] - 检测某服务器\n"
				+ " ◇ [已停用]/server scan [地址] <最小端口> <最大端口> - 进行端口扫描\n" + " #如果你有什么好用的功能\n"
				+ " #可以通过QQ 2431208142 提供给我\n" + " #我会将其添加到机器人上";
	}

	@Override
	public void onCommand(CommandSender sender, SingleMessage[] args) {
		if (this.main.getConfig().isBlackList(sender.getSenderID()))
			return;
		if (!(sender instanceof CommandSenderGroup))
			return;
		CommandSenderGroup senderGroup = (CommandSenderGroup) sender;
		Group group = senderGroup.getGroup();
		if (group.getId() != 252631851L)
			return;
		QuoteReply quote = new QuoteReply(senderGroup.getMessageSource());
		if (args.length >= 1) {
			if (args.length >= 2) {
				if (args[0].contentToString().equalsIgnoreCase("ping")) {
					main.getScheduler().async(new Runnable() {
						public void run() {
							String recordType = "A";
							String ip = "";
							int port = 25565;
							boolean flag1 = false;
							if (args.length >= 3) {
								if (args[2].contentToString().equalsIgnoreCase("A")) {
									ip = args[1].contentToString().substring(0,
											args[1].contentToString().lastIndexOf(':'));
									port = Util.strToInt(args[1].contentToString()
											.substring(args[1].contentToString().lastIndexOf(':') + 1), 25565);
									flag1 = true;
								}
								if (args[2].contentToString().equalsIgnoreCase("SRV")) {
									ip = args[1].contentToString();
									try {
										Record[] records = new Lookup("_minecraft._tcp." + ip, Type.SRV).run();
										if (records != null && records.length > 0) {
											for (Record r : records) {
												if (r instanceof SRVRecord) {
													SRVRecord srv = (SRVRecord) r;
													ip = srv.getTarget().toString().replaceFirst("\\.$", "");
													port = srv.getPort();
													recordType = "SRV";
													break;
												}
											}
										}
									} catch (Throwable t) {
										group.sendMessage(quote.plus("解析SRV记录出错"));
										return;
									}
									flag1 = true;
								}
							}
							if (!flag1) {
								if (args[1].contentToString().contains(":")) {
									ip = args[1].contentToString().substring(0,
											args[1].contentToString().lastIndexOf(':'));
									port = Util.strToInt(args[1].contentToString()
											.substring(args[1].contentToString().lastIndexOf(':') + 1), 25565);
								} else {
									ip = args[1].contentToString();
									try {
										Record[] records = new Lookup("_minecraft._tcp." + ip, Type.SRV).run();
										if (records != null && records.length > 0) {
											for (Record r : records) {
												if (r instanceof SRVRecord) {
													SRVRecord srv = (SRVRecord) r;
													ip = srv.getTarget().toString().replaceFirst("\\.$", "");
													port = srv.getPort();
													recordType = "SRV";
													break;
												}
											}
										}
									} catch (Throwable t) {
										t.printStackTrace();
										recordType = "A";
										ip = args[1].contentToString();
										port = 25565;
									}
								}
							}
							MessageChainBuilder mcb = new MessageChainBuilder();
							mcb.add(quote);
							mcb.add("测试中 →\n查询目标的 " + recordType + " 记录\n");
							MinecraftProtocol protocol = new MinecraftProtocol(SubProtocol.STATUS);
							Client client = new Client(ip, port, protocol, new TcpSessionFactory(Proxy.NO_PROXY));
							client.getSession().setFlag(MinecraftConstants.AUTH_PROXY_KEY, Proxy.NO_PROXY);
							final String ip2 = ip;
							final int port2 = port;
							client.getSession().addListener(new SessionAdapter() {
								@Override
								public void disconnected(DisconnectedEvent e) {
									String reason = Message.fromString(e.getReason()).getFullText();
									if (reason.contains("timed out")) {
										reason = "连接超时";
									}
									if (reason.equalsIgnoreCase("Finished")
											|| reason.equalsIgnoreCase("Connection closed."))
										return;

									mcb.add("检测的服务器地址: " + ip2 + ":" + port2 + "\n");
									mcb.add("已和服务器断开连接，原因:\n" + reason
											+ (e.getCause() != null ? ("\n" + e.getCause().getMessage()) : ""));
									group.sendMessage(mcb.build());

								}
							});
							client.getSession().setFlag(MinecraftConstants.SERVER_INFO_HANDLER_KEY,
									new ServerInfoHandler() {
										@Override
										public void handle(Session session, ServerStatusInfo info) {
											// 服务器图标
											InputStream icon = getImageStream(info.getIcon());
											if (icon == null) {
												try {
													icon = this.getClass().getResourceAsStream("/unknown_server.png");
												} catch (Throwable t) {
													t.printStackTrace();
												}
											}
											if (icon != null)
												try {
													mcb.add(group.uploadImage(ExternalResource.create(icon))
															.plus("\n"));
												} catch (Throwable t) {
													mcb.add("获取图标时出现了一个异常\n");
												}
											else {
												mcb.add("获取图标时出现了一个异常\n");
											}
											InetSocketAddress ss = (InetSocketAddress) ((TcpSession) session)
													.getRemoteAddress();
											String sss = "";
											if (ss != null) {
												if (ss.getAddress() != null) {
													sss = " (" + ss.getAddress().getHostAddress() + ":" + ss.getPort()
															+ ")";
												}
											}
											mcb.add("检测的服务器地址: " + session.getHost() + ":" + session.getPort() + sss
													+ "\n" + "游戏版本: " + info.getVersionInfo().getVersionName() + ", "
													+ info.getVersionInfo().getProtocolVersion() + "\n" + "在线人数: "
													+ info.getPlayerInfo().getOnlinePlayers() + " / "
													+ info.getPlayerInfo().getMaxPlayers() + "\n" + "玩家列表: "
													+ Arrays.toString(info.getPlayerInfo().getPlayers()) + "\n"
													+ "Motd: " + info.getDescription().getFullText() + "\n");

											group.sendMessage(mcb.build());
										}
									});
							client.getSession().connect();
						}
					});
					return;
				}
			}
			if (args.length >= 2) {
				if (args[0].contentToString().equalsIgnoreCase("scan")) {
					boolean enable = false;
					if (!enable) {
						group.sendMessage(quote.plus("[MX] 有bug，功能已关闭，懒得修"));
						return;
					}
					String ip = args[1].contentToString();
					if (running) {
						group.sendMessage(quote.plus("[MX] 端口扫描正在运行中\n若要强行停止，请用 /server stopscan"));
					} else {
						int minPort = 1;
						int maxPort = 65535;
						if (args.length == 4) {
							try {
								minPort = Integer.parseInt(args[2].contentToString());
							} catch (final NumberFormatException e) {
								group.sendMessage(quote.plus("[MX] 无效的最小端口"));
								return;
							}

							try {
								maxPort = Integer.parseInt(args[3].contentToString());
							} catch (final NumberFormatException e) {
								group.sendMessage(quote.plus("[MX] 无效的最大端口"));
								return;
							}
						} else if (args.length != 2) {
							group.sendMessage(
									quote.plus("[MX] 用法: /server scan 服务器地址 最小端口 最大端口\n" + "扫描服务器开放的端口，可指定范围，范围可不输入"));
							return;
						}

						group.sendMessage(
								quote.plus("[MX] 正在开始执行端口扫描...\n" + ip + "[" + minPort + "-" + maxPort + "]"));

						new java.lang.Thread(runningTask = new Run(ip, minPort, maxPort) {

							public void run() {
								try {
									while (running && currentPort < maxPort) {
										currentPort++;

										final int port = currentPort;

										try {
											final Socket socket = new Socket();
											socket.connect(new InetSocketAddress(ip, port), 500);
											socket.close();

											synchronized (ports) {
												if (!ports.contains(port))
													ports.add(port);
											}
										} catch (final Exception ignored) {
										}

										if (checkedPort < port)
											checkedPort = port;
									}

									running = false;
									MessageChainBuilder mcb = new MessageChainBuilder();
									mcb.add(quote);
									mcb.add("扫描到IP: " + ip + " 的端口如下:");
									for (int j = 0; j < ports.size(); j++) {
										mcb.add(ports.get(j) + (j < ports.size() - 1 ? ", " : ""));
									}
									group.sendMessage(mcb.build());
								} catch (final Exception e) {
									group.sendMessage(quote.plus("扫描IP: " + ip + " 时出现错误: "
											+ e.getClass().getSimpleName() + ": " + e.getMessage()));
								}
							}
						}).start();

						running = true;
					}
					return;
				}
			}

			if (args[0].contentToString().equalsIgnoreCase("ping")) {
				group.sendMessage(quote.plus("[MX] 用法: /server ping 服务器地址\n" + "地址使用“:”分隔ip和端口，端口格式错误或不填直接扫25565"));
				return;
			}
			if (args[0].contentToString().equalsIgnoreCase("scan")) {
				group.sendMessage(quote.plus("[MX] 用法: /server scan 服务器地址 最小端口 最大端口\n" + "扫描服务器开放的端口，可指定范围，范围可不输入"));
				return;
			}
			if (args[0].contentToString().equalsIgnoreCase("stopscan")) {
				group.sendMessage(quote.plus("[MX] 已停止扫描"));
				running = false;
				return;
			}
			if (args[0].contentToString().equalsIgnoreCase("seenscan")) {
				if (!running) {
					if (runningTask != null) {
						MessageChainBuilder mcb = new MessageChainBuilder();
						mcb.add(quote);
						mcb.add("[MX] 上一次扫描结果:\n" + "扫描到IP: " + runningTask.ip + " 的端口如下:");
						for (int j = 0; j < runningTask.ports.size(); j++) {
							mcb.add(runningTask.ports.get(j) + (j < runningTask.ports.size() - 1 ? ", " : ""));
						}
						group.sendMessage(mcb.build());
					} else {
						group.sendMessage(quote.plus("[MX] 没有扫描记录"));
					}
				} else {
					int max = runningTask.maxPort = runningTask.minPort;
					int current = runningTask.currentPort - runningTask.minPort;
					group.sendMessage(quote.plus("[MX] 当前正在扫描IP " + runningTask.ip + " 中，" + "扫描进度: "
							+ ((float) current / (float) max * (float) 100) + "%\n" + "已扫描到通行的端口数量: "
							+ runningTask.ports.size()));
				}
				return;
			}
		}
		group.sendMessage(quote.plus(getHelp()));

	}

}
