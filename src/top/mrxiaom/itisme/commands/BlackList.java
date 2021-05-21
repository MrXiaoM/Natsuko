package top.mrxiaom.itisme.commands;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.message.data.SingleMessage;
import top.mrxiaom.itisme.Natsuko;
import top.mrxiaom.itisme.Util;
import top.mrxiaom.miraiutils.CommandModel;
import top.mrxiaom.miraiutils.CommandSender;
import top.mrxiaom.miraiutils.CommandSenderGroup;

public class BlackList extends CommandModel {
	Natsuko main;

	public BlackList(Natsuko main) {
		super("blacklist");
		this.main = main;
	}

	@Override
	public void onCommand(CommandSender sender, SingleMessage[] args) {
		if (this.main.getConfig().isBlackList(sender.getSenderID()))
			return;
		if (sender instanceof CommandSenderGroup) {
			CommandSenderGroup senderGroup = (CommandSenderGroup) sender;
			Group group = senderGroup.getGroup();
			Member member = senderGroup.getMember();
			QuoteReply quote = new QuoteReply(senderGroup.getMessageSource());
			if (main.isGroupAccess(group, this.getClass())) {
				boolean admin = main.isManager(member.getId())
						|| member.getPermission() == MemberPermission.ADMINISTRATOR
						|| member.getPermission() == MemberPermission.OWNER;
				if (!admin) {
					if (args.length == 2) {
						if (args[0].contentToString().equalsIgnoreCase("add")) {
							long qq = Util.strToLong(args[1].contentToString(), -1);
							if (qq < 10000) {
								group.sendMessage(quote.plus("[������] �޷�ʶ���QQ��"));
								return;
							}
							if (main.getConfig().isBlackList(qq)) {
								group.sendMessage(quote.plus("[������] �������ں������ڣ������ظ����"));
								return;
							}
							main.getConfig().addBlackList(qq);
							group.sendMessage(quote.plus("[������] ����������û�Ϊ�����˺�����\n" + "  - " + qq + " "
									+ (group.getMembers().contains(qq) ? ("" + group.getMembers().get(qq).getNameCard())
											: "����Ⱥ��")));
							return;
						}
						if (args[0].contentToString().equalsIgnoreCase("remove")) {
							long qq = Util.strToLong(args[1].contentToString(), -1);
							if (qq < 10000) {
								group.sendMessage(quote.plus("[������] �޷�ʶ���QQ��"));
								return;
							}
							if (!main.getConfig().isBlackList(qq)) {
								group.sendMessage(quote.plus("[������] ����û�б���ӵ��������У������Ƴ�"));
								return;
							}
							main.getConfig().removeBlackList(qq);
							group.sendMessage(quote.plus("[������] �ѽ������û��ӻ����˺��������Ƴ�\n" + "  - " + qq + " "
									+ (group.getMembers().contains(qq)
											? ("Ⱥ��Ƭ: " + group.getMembers().get(qq).getNameCard())
											: "����Ⱥ��")));
							return;
						}
					}
				}

				String result = "�����˺������б�:";
				for (Object s : this.main.getConfig().getBlackList()) {
					result = result + "\n" + "  - " + s + ""
							+ (group.getMembers().contains((long)s) ? ("Ⱥ��Ƭ: " + group.get((long)s).getNameCard()) : "����Ⱥ��");
				}
				group.sendMessage(quote.plus(result));
			}
		}
	}

}
