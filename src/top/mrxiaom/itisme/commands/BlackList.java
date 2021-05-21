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
								group.sendMessage(quote.plus("[黑名单] 无法识别的QQ号"));
								return;
							}
							if (main.getConfig().isBlackList(qq)) {
								group.sendMessage(quote.plus("[黑名单] 此人已在黑名单内，无需重复添加"));
								return;
							}
							main.getConfig().addBlackList(qq);
							group.sendMessage(quote.plus("[黑名单] 已添加以下用户为机器人黑名单\n" + "  - " + qq + " "
									+ (group.getMembers().contains(qq) ? ("" + group.getMembers().get(qq).getNameCard())
											: "不在群内")));
							return;
						}
						if (args[0].contentToString().equalsIgnoreCase("remove")) {
							long qq = Util.strToLong(args[1].contentToString(), -1);
							if (qq < 10000) {
								group.sendMessage(quote.plus("[黑名单] 无法识别的QQ号"));
								return;
							}
							if (!main.getConfig().isBlackList(qq)) {
								group.sendMessage(quote.plus("[黑名单] 此人没有被添加到黑名单中，无需移除"));
								return;
							}
							main.getConfig().removeBlackList(qq);
							group.sendMessage(quote.plus("[黑名单] 已将以下用户从机器人黑名单中移除\n" + "  - " + qq + " "
									+ (group.getMembers().contains(qq)
											? ("群名片: " + group.getMembers().get(qq).getNameCard())
											: "不在群内")));
							return;
						}
					}
				}

				String result = "机器人黑名单列表:";
				for (Object s : this.main.getConfig().getBlackList()) {
					result = result + "\n" + "  - " + s + ""
							+ (group.getMembers().contains((long)s) ? ("群名片: " + group.get((long)s).getNameCard()) : "不在群内");
				}
				group.sendMessage(quote.plus(result));
			}
		}
	}

}
