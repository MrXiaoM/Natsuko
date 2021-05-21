package top.mrxiaom.itisme;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.console.command.Command;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.extension.PluginComponentStorage;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.internal.QQAndroidBot;
import net.mamoe.mirai.message.data.MessageSource;
import net.mamoe.mirai.message.data.OnlineMessageSource.Incoming.FromGroup;
import net.mamoe.mirai.utils.BotConfiguration;
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol;
import top.mrxiaom.itisme.commands.About;
import top.mrxiaom.itisme.commands.AcgImg;
import top.mrxiaom.itisme.commands.BlackList;
import top.mrxiaom.itisme.commands.FriendMessageReceived;
import top.mrxiaom.itisme.commands.FriendSend;
import top.mrxiaom.itisme.commands.GroupSend;
import top.mrxiaom.itisme.commands.Help;
import top.mrxiaom.itisme.commands.MXServer;
import top.mrxiaom.itisme.commands.Mcban;
import top.mrxiaom.itisme.commands.RandomNews;
import top.mrxiaom.itisme.commands.Status;
import top.mrxiaom.itisme.commands.Verify;
import top.mrxiaom.itisme.commands.Wallpaper;
import top.mrxiaom.miraiutils.CommandListener;

public class Natsuko extends JavaPlugin {
    private static Natsuko instance;
    public static Natsuko getInstance() {
        return instance;
    }
    public Natsuko() {
        super(new JvmPluginDescriptionBuilder(
            "top.mrxiaom.testplugin",
            "1.0.0"
        )
        .name("TestPlugin")
        .author("Mr_Xiao_M")
        .info("懒怠的小猫的 Mirai Natsuko 机器人插件，仅自用，不面向普通用户，懒得写配置文件了，要用就自己改代码罢")
        .build()
        );
        instance = this;
    }

    public About about;
    public AcgImg acgimg;
    public BlackList blackListCmd;
    public FriendMessageReceived fme;
    public FriendSend fs;
    public GroupSend gs;
    public Help help;
    public Mcban mcban;
    public MXServer mxServer;
    public Status status;
    public top.mrxiaom.itisme.commands.Thread thread;
    public Verify verify;
    public Wallpaper wallpaper;
    public RandomNews randomNews;
    
    private void registerCommands() {
    	this.about = new About(this);
    	this.acgimg = new AcgImg(this);
    	this.blackListCmd = new BlackList(this);
    	this.fme = new FriendMessageReceived(this);
    	this.fs = new FriendSend(this);
    	this.gs = new GroupSend(this);;
    	this.help = new Help(this);
    	this.mcban = new Mcban(this);
    	this.mxServer = new MXServer(this);
    	this.status = new Status(this);
    	this.thread = new top.mrxiaom.itisme.commands.Thread(this);
    	this.verify = new Verify(this);
    	this.wallpaper = new Wallpaper(this);
    	this.randomNews = new RandomNews(this);
    	
		this.cmdListener = new CommandListener("/");
		this.cmdListener.registerCommand(about);
		this.cmdListener.registerCommand(acgimg);
		this.cmdListener.registerCommand(blackListCmd);
		this.cmdListener.registerCommand(fs);
		this.cmdListener.registerCommand(gs);
		this.cmdListener.registerCommand(help);
		this.cmdListener.registerCommand(mcban);
		this.cmdListener.registerCommand(mxServer);
		this.cmdListener.registerCommand(status);
		this.cmdListener.registerCommand(thread);
		this.cmdListener.registerCommand(verify);
		this.cmdListener.registerCommand(wallpaper);
		this.cmdListener.registerCommand(randomNews);
    }
	public File logFile;
	public static boolean a=false;
	public void log(String content) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd [HH:mm:ss]");
		String logText = df.format(new Date()) + "[INFO]: "+content;
		System.out.println(logText);
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;

        try {
            if (!logFile.exists()) {
                boolean hasFile = logFile.createNewFile();
                if(hasFile){
                    //System.out.println("file not exists, create new file");
                }
                fos = new FileOutputStream(logFile);
            } else {
                //System.out.println("file exists");
                fos = new FileOutputStream(logFile, true);
            }

            osw = new OutputStreamWriter(fos, "utf-8");
            osw.write(logText); //写入内容
            osw.write("\r\n");  //换行
        } catch (Exception e) {
            e.printStackTrace();
        }finally {   //关闭流
            try {
                if (osw != null) {
                    osw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}
	
	public boolean isManager(long id) {
		return this.config.getJSON().getJSONArray("managers").contains(String.valueOf(id));
	}
	
	public boolean isGroupListenRecall(Group group) {
		JSONObject settings = this.config.getJSON().getJSONObject("recall-settings");
		return settings.getJSONArray("listen-groups").contains(String.valueOf(group.getId()));
	}
	
	public boolean isGroupAccess(Group group, Class<?> clazz) {
		JSONObject array = this.config.getJSON().getJSONObject("access-groups");
		if(array.containsKey(String.valueOf(group.getId()))) {
			JSONObject groupConfig = array.getJSONObject(String.valueOf(group.getId()));
			if(groupConfig.containsKey("blacklist-cmd")) {
				return !groupConfig.getJSONArray("blacklist-cmd").contains(clazz.getTypeName().toLowerCase());
			}
			else if (groupConfig.containsKey("whitelist-cmd")) {
				return groupConfig.getJSONArray("whitelist-cmd").contains(clazz.getTypeName().toLowerCase());
			}
			return true;
		}
		return false;
	}
	
	EventHost events;
	@Override
	public void onLoad(PluginComponentStorage pcs) {
		this.getLogger().info("插件配置路径: " + this.getDataFolder().getAbsoluteFile().getPath());
		
		this.config = new ConfigManager(this);
		this.config.reloadConfig();
		
		this.logFile = new File(this.getDataFolder().getAbsoluteFile().getPath() + "\\run.log");
	}
	Timer timer;
	public static Timer timer2;
	public QQAndroidBot bot;
	public CommandListener cmdListener;
	
	@Override
	public void onEnable() {
		this.getLogger().info("Plugin loaded!");
		JSONObject json = config.getJSON();
		long qqNumber = json.getLong("qq");
		String qqPW = json.getString("password");
		if(qqNumber < 10000) {
			this.getLogger().warning("qq号码不正确，取消载入");
			return;
		}
		if(qqPW.equalsIgnoreCase("在这里填写密码")) {

			this.getLogger().warning("未填写qq密码，取消载入");
			return;
		}
		this.registerCommands();
		events = new EventHost(this);
		if(json.getBoolean("unregister-default-commands")) {
			this.getLogger().info("注销所有命令");
			for (Command c : CommandManager.INSTANCE.getAllRegisteredCommands()) {
				this.getLogger().info("已注销 " + c.getPrimaryName());
				CommandManager.INSTANCE.unregisterCommand(c);
			}
		}
		// QQAndroid.INSTANCE.newBot(qqNumber, qqPW, BotConfiguration.getDefault());
		bot = (QQAndroidBot) BotFactory.INSTANCE.newBot(qqNumber, qqPW, new BotConfiguration() {
            {

                //保存设备信息到文件
                fileBasedDeviceInfo("device.json");
                // setLoginSolver();
                // setBotLoggerSupplier();
            }
        });
		MiraiProtocol protocol = MiraiProtocol.ANDROID_PHONE;
		this.getLogger().info("切换协议为 " + protocol.name());
		bot.getConfiguration().setProtocol(protocol);
		bot.login();
		
		bot.getEventChannel().registerListenerHost(events);
		bot.getEventChannel().registerListenerHost(cmdListener);
		
		timer = new Timer();
		timer.schedule(tickTask = new TickTask(this), 500, 500);
	}
	public TickTask tickTask;
	long nexttime = 0;
	long nexttime_ = 0;
	HashMap<Integer, FromGroup> messageRecord = new HashMap<Integer, FromGroup>();
	ConfigManager config;
	public void clearOutdataMessage(int nowtime) {
		for(int key : messageRecord.keySet()) {
			MessageSource source = messageRecord.get(key);
			if(source.getTime() + 600 < nowtime) {
				//System.out.println("[DEBUG] 已删除超过10分钟的消息" + source.getId());
				messageRecord.remove(key);
				// 这是针对 HashMap 的特性才做的遍历
				// 如果不这样做会报错
				clearOutdataMessage(nowtime);
				break;	
			}
		}
	}
	public File getFile(String path) {
		return new File(this.getDataFolder().getAbsoluteFile().getPath() + path);
	}
	@Override
	public void onDisable() {
		
		timer.cancel();
		timer = null;
	}
	
	public ConfigManager getConfig() {
		return config;
	}
	
	public static String clearColor(String text) {
		return text
				.replace("§a", "")
				.replace("§b", "")
				.replace("§c", "")
				.replace("§d", "")
				.replace("§e", "")
				.replace("§f", "")
				.replace("§r", "")
				.replace("§l", "")
				.replace("§m", "")
				.replace("§n", "")
				.replace("§o", "")
				.replace("§k", "")
				.replace("§0", "")
				.replace("§1", "")
				.replace("§2", "")
				.replace("§3", "")
				.replace("§4", "")
				.replace("§5", "")
				.replace("§6", "")
				.replace("§7", "")
				.replace("§8", "")
				.replace("§9", "")
				.replace("§A", "")
				.replace("§B", "")
				.replace("§C", "")
				.replace("§D", "")
				.replace("§E", "")
				.replace("§F", "")
				.replace("§R", "")
				.replace("§L", "")
				.replace("§M", "")
				.replace("§N", "")
				.replace("§O", "")
				.replace("§K", "");
	}
}