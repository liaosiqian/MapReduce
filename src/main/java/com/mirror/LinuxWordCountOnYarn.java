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
 * 执行MR方式一：hadoop集群节点启动，MR在集群运行
 * 流程： 开发 -> 打jar包 -> 上传到hadoop集群某一节点
 * -> 执行命令： hadoop jar *.jar  Main方法全限定名  inputPath  outputPath
 */
public class LinuxWordCountOnYarn {

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        // 1、读取配置文件
        Configuration conf = new Configuration(true);
        // 提取hadoop的相关配置，并设置到conf中
        GenericOptionsParser parser = new GenericOptionsParser(conf,args);
        // 获取自定义参数
        String[] otherArgs = parser.getRemainingArgs();

        // 2、根据配置文件，构建作业
        Job job = Job.getInstance(conf);

        // 3、 设置作业jar的启动类 和 作业名称
        job.setJarByClass(LinuxWordCountOnYarn.class);
        job.setJobName("LinuxWordCountOnYarn");

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
