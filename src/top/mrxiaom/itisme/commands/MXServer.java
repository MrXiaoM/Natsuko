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
		return "   -- ServerHelper �������� --   \n" + " �� /server - �鿴�˰���\n" + " �� /server ping [��ַ] - ���ĳ������\n"
				+ " �� [��ͣ��]/server scan [��ַ] <��С�˿�> <���˿�> - ���ж˿�ɨ��\n" + " #�������ʲô���õĹ���\n"
				+ " #����ͨ��QQ 2431208142 �ṩ����\n" + " #�һὫ����ӵ���������";
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
										group.sendMessage(quote.plus("����SRV��¼����"));
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
							mcb.add("������ ��\n��ѯĿ��� " + recordType + " ��¼\n");
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
										reason = "���ӳ�ʱ";
									}
									if (reason.equalsIgnoreCase("Finished")
											|| reason.equalsIgnoreCase("Connection closed."))
										return;

									mcb.add("���ķ�������ַ: " + ip2 + ":" + port2 + "\n");
									mcb.add("�Ѻͷ������Ͽ����ӣ�ԭ��:\n" + reason
											+ (e.getCause() != null ? ("\n" + e.getCause().getMessage()) : ""));
									group.sendMessage(mcb.build());

								}
							});
							client.getSession().setFlag(MinecraftConstants.SERVER_INFO_HANDLER_KEY,
									new ServerInfoHandler() {
										@Override
										public void handle(Session session, ServerStatusInfo info) {
											// ������ͼ��
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
													mcb.add("��ȡͼ��ʱ������һ���쳣\n");
												}
											else {
												mcb.add("��ȡͼ��ʱ������һ���쳣\n");
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
											mcb.add("���ķ�������ַ: " + session.getHost() + ":" + session.getPort() + sss
													+ "\n" + "��Ϸ�汾: " + info.getVersionInfo().getVersionName() + ", "
													+ info.getVersionInfo().getProtocolVersion() + "\n" + "��������: "
													+ info.getPlayerInfo().getOnlinePlayers() + " / "
													+ info.getPlayerInfo().getMaxPlayers() + "\n" + "����б�: "
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
						group.sendMessage(quote.plus("[MX] ��bug�������ѹرգ�������"));
						return;
					}
					String ip = args[1].contentToString();
					if (running) {
						group.sendMessage(quote.plus("[MX] �˿�ɨ������������\n��Ҫǿ��ֹͣ������ /server stopscan"));
					} else {
						int minPort = 1;
						int maxPort = 65535;
						if (args.length == 4) {
							try {
								minPort = Integer.parseInt(args[2].contentToString());
							} catch (final NumberFormatException e) {
								group.sendMessage(quote.plus("[MX] ��Ч����С�˿�"));
								return;
							}

							try {
								maxPort = Integer.parseInt(args[3].contentToString());
							} catch (final NumberFormatException e) {
								group.sendMessage(quote.plus("[MX] ��Ч�����˿�"));
								return;
							}
						} else if (args.length != 2) {
							group.sendMessage(
									quote.plus("[MX] �÷�: /server scan ��������ַ ��С�˿� ���˿�\n" + "ɨ����������ŵĶ˿ڣ���ָ����Χ����Χ�ɲ�����"));
							return;
						}

						group.sendMessage(
								quote.plus("[MX] ���ڿ�ʼִ�ж˿�ɨ��...\n" + ip + "[" + minPort + "-" + maxPort + "]"));

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
									mcb.add("ɨ�赽IP: " + ip + " �Ķ˿�����:");
									for (int j = 0; j < ports.size(); j++) {
										mcb.add(ports.get(j) + (j < ports.size() - 1 ? ", " : ""));
									}
									group.sendMessage(mcb.build());
								} catch (final Exception e) {
									group.sendMessage(quote.plus("ɨ��IP: " + ip + " ʱ���ִ���: "
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
				group.sendMessage(quote.plus("[MX] �÷�: /server ping ��������ַ\n" + "��ַʹ�á�:���ָ�ip�Ͷ˿ڣ��˿ڸ�ʽ�������ֱ��ɨ25565"));
				return;
			}
			if (args[0].contentToString().equalsIgnoreCase("scan")) {
				group.sendMessage(quote.plus("[MX] �÷�: /server scan ��������ַ ��С�˿� ���˿�\n" + "ɨ����������ŵĶ˿ڣ���ָ����Χ����Χ�ɲ�����"));
				return;
			}
			if (args[0].contentToString().equalsIgnoreCase("stopscan")) {
				group.sendMessage(quote.plus("[MX] ��ֹͣɨ��"));
				running = false;
				return;
			}
			if (args[0].contentToString().equalsIgnoreCase("seenscan")) {
				if (!running) {
					if (runningTask != null) {
						MessageChainBuilder mcb = new MessageChainBuilder();
						mcb.add(quote);
						mcb.add("[MX] ��һ��ɨ����:\n" + "ɨ�赽IP: " + runningTask.ip + " �Ķ˿�����:");
						for (int j = 0; j < runningTask.ports.size(); j++) {
							mcb.add(runningTask.ports.get(j) + (j < runningTask.ports.size() - 1 ? ", " : ""));
						}
						group.sendMessage(mcb.build());
					} else {
						group.sendMessage(quote.plus("[MX] û��ɨ���¼"));
					}
				} else {
					int max = runningTask.maxPort = runningTask.minPort;
					int current = runningTask.currentPort - runningTask.minPort;
					group.sendMessage(quote.plus("[MX] ��ǰ����ɨ��IP " + runningTask.ip + " �У�" + "ɨ�����: "
							+ ((float) current / (float) max * (float) 100) + "%\n" + "��ɨ�赽ͨ�еĶ˿�����: "
							+ runningTask.ports.size()));
				}
				return;
			}
		}
		group.sendMessage(quote.plus(getHelp()));

	}

}
