package com.mirror;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.StringTokenizer;

/**
 * 映射
 */
public class MyMapperClass extends Mapper<Object, Text, Text, IntWritable> {

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        // 1、拆分读到的每行数据
        StringTokenizer itr = new StringTokenizer(value.toString());
        while (itr.hasMoreTokens()) {
            String wordStr = itr.nextToken();
            // 2、过滤指定单词
            if(wordStr.startsWith("h") || wordStr.startsWith("b")){
                word.set(wordStr);
                // 2、 hadoop，是一套 分布式框架，  数据在传输的过程中就会被，序列化 和 反序列化
                //     因此 hadoop有自己一套序列化类型  ： Text( -> String) ， IntWritable ( -> int)
                //     如果自己开发自定类型，则需要实现序列化接口，和 比较器接口
                //     排序 ->  字典序   和   数值序
                //    由于每次写入数据都会进行序列化，因此word 和 one可以复用
                //    不用担心前后两次迭代覆盖,因此在不影响最后输出结果前提下，
                //    可以将one 和 word 单独拎出来，避免因为频繁创建对象而导致gc频繁
                context.write(word, one);
            }

        }
    }

}
