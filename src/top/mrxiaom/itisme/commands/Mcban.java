package top.mrxiaom.itisme.commands;

import com.alibaba.fastjson.JSONObject;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.message.data.SingleMessage;
import top.mrxiaom.itisme.Natsuko;
import top.mrxiaom.itisme.Util;
import top.mrxiaom.miraiutils.CommandModel;
import top.mrxiaom.miraiutils.CommandSender;
import top.mrxiaom.miraiutils.CommandSenderGroup;

public class Mcban extends CommandModel{
	Natsuko main;
	public Mcban(Natsuko main) {
		super("mcban");
		this.main = main;
	}
	
	@Override
	public void onCommand(CommandSender sender, SingleMessage[] args) {
		if(this.main.getConfig().isBlackList(sender.getSenderID())) return;
		if(sender instanceof CommandSenderGroup) {
			CommandSenderGroup senderGroup = (CommandSenderGroup) sender;
			Group group = senderGroup.getGroup();
			if(group.getId() == 241973735L ||
				group.getId() == 951534513L) {
				QuoteReply quote = new QuoteReply(senderGroup.getMessageSource());
				if (args.length == 1) {
					String qq = args[0].contentToString();
					String json = Util.sendGet("https://top.timewk.cn/api/blacklist/query/" + qq);
					//System.out.println(json);
					JSONObject j = JSONObject.parseObject(json);
					
					if (j.getString("status").contains("suc")) {
						JSONObject result = j.getJSONObject("result");
						String id = result.getString("id");
						String level = result.getString("level");
						String date = result.getString("date");
						String note = result.getString("note");
						group.sendMessage(quote.plus("\n【我的世界云端黑名单查询】\nQQ: " + qq + "\n"
										+ "云黑序号: " + id + "\n" + "黑名单等级: " + level + "级\n" + "黑名单时间: " + date + "\n"
										+ "黑名单原因: " + note + "\n\n请停止交易\n * mcban.cn"));

					} else if (j.getString("status").contains("err")) {
						JSONObject result = j.getJSONObject("result");
						if (result.containsKey("info")) {
							group.sendMessage(quote.plus(
									"\n【我的世界云端黑名单查询】\nQQ: " + qq + "\n" + j.getJSONObject("result").getString("info")
											.replace("Cannot find this user.", "该QQ尚未被录入，交易前别忘了核实提供商品的相关信息哦~")));
						}
						if (result.containsKey("msg")) {
							group.sendMessage(
									quote.plus("\n【我的世界云端黑名单查询】\nQQ: " + qq + "\n" + j.getJSONObject("result")
											.getString("msg").replace("Missing parameters", "缺少参数")));
						}
					} else {
						group.sendMessage(quote.plus("\n [mcban] 未知错误"));
					}
				} else {
					group.sendMessage(quote.plus("\n [mcban] 参数无效"));
				}
			}
		}
	}
}
