# Natsuko
一只基于 Mirai 的 QQ 机器人插件  

之前一直要承诺把我的机器人开源的，一直没写配置文件让机器人更好编辑  
现在懒得写了，写了一半不到的配置文件，剩下一些功能自己去源码改，  
因为这个原因我不会给出编译好的jar，若要使用务必自行编译  

# 功能
当前 Natsuko 的功能如下
* 看涩图（要自己加图进去
* 黑名单
* 机器人收到私信自动转发给主人
* 操控机器人发消息
* mcban查询
* Minecraft服务器状态查询 (支持 SRV)
* 百度新闻
* 自动回复消息
* 需要管理手动触发的人机验证 (需要自己加题目)
* _[已弃用且停止维护]~~Minecraft服务器挂机机器人~~_

and so on.  

如果需要一点默认配置的资源可以到本仓库的 Releases 查看

# 使用库

* [alibaba/fastjson](https://github.com/alibaba/fastjson)  
* [dnsjava/dnsjava](https://github.com/dnsjava/dnsjava)  
* [google/gson](https://github.com/google/gson)  
* [jamietech/MinecraftServerPing](https://github.com/jamietech/MinecraftServerPing)  
* [mamoe/mirai](https://github.com/mamoe/mirai)  
* [mamoe/mirai-console](https://github.com/mamoe/mirai-console)  
* [mrxiaom/mirai-utils](https://github.com/mrxiaom/mirai-utils)  
* [netty/netty](https://github.com/netty/netty)  
* [Steveice10/MCProtocolLib](https://github.com/Steveice10/MCProtocolLib) (我做了点修改，屏蔽了区块错误的报错等等，修改后的jar请见 Releases)  
