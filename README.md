## MapReduce三种执行方式

### 方式一：hadoop集群节点上启动
开发 -> 打jar包 -> 上传到hadoop集群某一节点 
->  hadoop jar *.jar  Main方法全限定名  inputPath  outputPath

### 方式二：本地启动(idea)，MR在集群运行
  嵌入【linux、window】非 hadoop集群  on  yarn
#### a、设置环境变量：指定 HADOOP_USER_NAME
```
HADOOP_USER_NAME=root
```
【如果idea已经打开，需要重启后方能生效】

#### b、添加启动参数 Edit Configurations -> Program arguments
   格式：[本地jar包路径] [输入路径] [输出路径]

```
   -D mapreduce.job.jar=D:\work\bigdata\target\hadoop-1.0-SNAPSHOT.jar /user/root/data.txt /user/root/LocalWordCount/
```
 
#### PS:每次代码有变动后，均需要重新打包，才能生效

### 方式三：单机运行（idea），【自测逻辑时使用】
#### a、解压hadoop安装  
```
D:\software\hadoop\hadoop-2.10.1
  
```
[https://archive.apache.org/dist/hadoop/common/hadoop-2.10.1/hadoop-2.10.1.tar.gz]()

#### b、设置环境变量：指定 HADOOP_HOME、HADOOP_USER_NAME
```
HADOOP_HOME=D:\software\hadoop\hadoop-2.10.1
HADOOP_USER_NAME=root
```
#### c、从winutils下载对应版本 替换 HADOOP_HOME\bin 目录下的内容
[https://github.com/steveloughran/winutils/tree/master/hadoop-2.8.3/bin]()   
   【因为找不到2.10.1d的版本，因此选择了2.8.3的版本，试了之后也是ok】
#### d、从winutils下载的动态库（hadoop.dll）到 （C:\Windows\System32）下  
 
#### e、添加启动参数 Edit Configurations -> Program arguments
```
# 格式： [指定本地单机运行] [输入路径] [输出路径]
-D mapreduce.framework.name=local /user/root/data.txt /user/root/LocalWordCount/
```
