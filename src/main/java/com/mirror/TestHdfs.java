package com.mirror;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.URI;
import java.util.Objects;

public class TestHdfs {

    private Configuration conf = null;
    private FileSystem fs = null;



    @Before
    public void connect() throws IOException, InterruptedException {
        // 1、创建链接
        conf = new Configuration(true);
        // 2、获取文件系统操作对象
        fs = FileSystem.get(URI.create("hdfs://mycluster"), conf,"hadoop");
    }


    @Test
    public void mkdir() throws IOException {
        Path path = new Path("/first/bigdata1");
        // 存在则先删除(文件、文件夹均可删除)
        fs.deleteOnExit(path);
        // 创建文件
        //fs.create(path);
        // 创建目录
        fs.mkdirs(path);
    }

    @Test
    public void upload() throws Exception {
        // 读取文件
        BufferedInputStream input = new BufferedInputStream(new FileInputStream(new File("data/upload.txt")));

        // 上到传hdfsd的目标路径
        Path uploadPath = new Path("/first/hello.txt");
        FSDataOutputStream output = fs.create(uploadPath);

        // 批量上传数据
        IOUtils.copyBytes(input,output, conf,true);
    }

    @Test
    public void down() throws Exception {
        // 写入文件
        BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(new File("data/down.txt")));

        // 下载hdfsd的目标路径
        Path uploadPath = new Path("/first/hello.txt");
        FSDataInputStream input = fs.open(uploadPath);

        // 批量上传数据
        IOUtils.copyBytes(input,output,conf,true);
    }

    @Test
    public void block() throws IOException {
        Path path = new Path("/user/root/data.txt");
        // 1、获取文件元素据
        FileStatus fileStatus = fs.getFileStatus(path);
        // 2、根据要读取的文件位置，获取文件块的位置信息
        BlockLocation[] fileBlockLocations = fs.getFileBlockLocations(fileStatus, 0, fileStatus.getLen());
        // 3、遍历块分块信息，获取每个分块的前10个字节
        FSDataInputStream inputStream = fs.open(path);
        for (BlockLocation block : fileBlockLocations) {
            System.out.println("block信息："+block);
            Long offset = block.getOffset();
            // a、设置读取位置的起始值，以便于获取到具体的block信息，而不是从头开始再计算一遍
            // b、hdfs会自动获取最近的存有block的node节点
            inputStream.seek(offset);
            for(int i=0,j=10 ; i<j ; i++){
                System.out.println((char)inputStream.readByte());
            }
            System.out.println("---------------------------------");

        }

    }



    @After
    public  void close() throws IOException {
        if(Objects.nonNull(fs)){
            fs.close();
        }

    }

}
