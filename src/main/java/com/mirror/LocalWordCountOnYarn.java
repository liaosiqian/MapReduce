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
 * 执行MR方式二：本地启动，MR在集群运行
 * 嵌入【linux、window】非 hadoop集群  on  yarn
 * a、设置环境变量：指定 HADOOP_USER_NAME
 *    HADOOP_USER_NAME=root
 * 【如果idea已经打开，需要重启后方能生效】

 * b、添加启动参数 Edit Configurations -> Program arguments
 *  格式：[本地jar包路径] [输入路径] [输出路径]
 *  -D mapreduce.job.jar=D:\work\bigdata\target\hadoop-1.0-SNAPSHOT.jar /user/root/data.txt /user/root/LocalWordCount/
 *
 * PS:每次代码有变动后，均需要重新打包，才能生效
 */
public class LocalWordCountOnYarn {

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        // 1、读取配置文件
        Configuration conf = new Configuration(true);

        // 提取hadoop的相关配置，并设置到conf中
        GenericOptionsParser parser = new GenericOptionsParser(conf,args);
        // 获取自定义参数
        String[] otherArgs = parser.getRemainingArgs();

        // 在 idea直接启动，才需要指定，否则会直接导致 作业启动失败
        // 让框架知道是 windows异构平台
        conf.set("mapreduce.app-submission.cross-platform","true");

        // 2、根据配置文件，构建作业
        Job job = Job.getInstance(conf);

        // 3、 设置作业jar的启动类 和 作业名称
        job.setJarByClass(LocalWordCountOnYarn.class);
        // 在 idea直接启动，才需要通过此行指定jar包位置，否则会报 ClassNotFoundException
        // job.setJar(args[0]);
        job.setJobName("LocalWordCountOnYarn");

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
