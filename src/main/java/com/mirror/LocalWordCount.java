package com.mirror;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import java.io.IOException;

/**
 * 执行MR方式三：单机运行（window），【自测逻辑时使用】
 * a、解压hadoop安装  D:\software\hadoop\hadoop-2.10.1
 * https://archive.apache.org/dist/hadoop/common/hadoop-2.10.1/hadoop-2.10.1.tar.gz
 * b、设置环境变量：指定 HADOOP_HOME、HADOOP_USER_NAME
 *    HADOOP_HOME=D:\software\hadoop\hadoop-2.10.1
 *    HADOOP_USER_NAME=root
 * c、从winutils下载对应版本 替换 HADOOP_HOME\bin 目录下的内容
 *  https://github.com/steveloughran/winutils/tree/master/hadoop-2.8.3/bin
 *  【因为找不到2.10.1d的版本，因此选择了2.8.3的版本，试了之后也是ok】
 * d、从winutils下载的动态库（hadoop.dll）到 （C:\Windows\System32）下
 *
 * e、添加启动参数 Edit Configurations -> Program arguments
 *  格式： [指定本地单机运行] [输入路径] [输出路径]
 *  -D mapreduce.framework.name=local /user/root/data.txt /user/root/LocalWordCount/
 */
public class LocalWordCount {

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        // 1、读取配置文件
        Configuration conf = new Configuration(true);

        // 提取hadoop的相关配置，并设置到conf中
        GenericOptionsParser parser = new GenericOptionsParser(conf,args);
        // 获取自定义参数
        String[] otherArgs = parser.getRemainingArgs();

        // 在 idea直接启动，才需要指定，否则会直接导致 作业启动失败
        // 让框架知道是 windows异构平台 ，也可以通过启动参数指定
        conf.set("mapreduce.app-submission.cross-platform","true");

        // 2、根据配置文件，构建作业
        Job job = Job.getInstance(conf);

        // 3、 设置作业jar的启动类 和 作业名称
        job.setJarByClass(LocalWordCount.class);
        // 在 idea直接启动，才需要通过此行指定jar包位置，否则会报 ClassNotFoundException
        job.setJobName("LocalWordCount");

        // 4、设置输入路径
        Path inputPath = new Path(otherArgs[0]);
        TextInputFormat.setInputPaths(job,inputPath);

        // 5、设置输出路径
        Path outputPath = new Path(otherArgs[1]);
        if(outputPath.getFileSystem(conf).exists(outputPath)){
            outputPath.getFileSystem(conf).delete(outputPath,true);
        }
        TextOutputFormat.setOutputPath(job,outputPath);


        // 6、映射
        job.setMapperClass(MyMapperClass.class);

        // 7、输出
        // 7.1 因为hadoop是分布式的，强类型的，因此需要告知框架，
        // 要输出key/value的序列化信息
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        // 7.2 以组计算
        job.setReducerClass(MyReduceClass.class);

        // 8、提交作业
        job.waitForCompletion(true);

    }

}
