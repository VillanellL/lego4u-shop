# 1.nacos 启动

命令：startup.cmd -m standalone

默认端口：http://localhost:8848/nacos



# 2.redis  启动

命令：redis-server.exe

默认端口：6379



# 3.rockmq启动

命令：start mqnamesrv.cmd

命令：start mqbroker.cmd -n 127.0.0.1:9876 autoCreateTopicEnable=true

启动控制台：java -jar rocketmq-dashboard-1.0.1-SNAPSHOT.jar

端口：9999（可在 rockmq-dashboard中的yml中修改）



# 4.xxl-job启动

运行jar包

配置端口:8080