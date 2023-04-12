package com.suyeq.mapreduce.wordcount;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * @author : denglinhai
 * @date : 16:51 2023/4/10
 */
public class WordCountSubmit {

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        //获取一个Job实例
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "WordCount");

        //设置我们的当前Driver类路径(classpath)
        job.setJarByClass(WordCountSubmit.class);

        //设置自定义的Mapper类路径(classpath)
        job.setMapperClass(WordMap.class);

        //设置自定义的Reducer类路径(classpath)
        job.setReducerClass(WordReduce.class);

        //设置自定义的Mapper程序的输出类型
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        //设置自定义的Reducer程序的输出类型
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        //设置输入数据
        FileInputFormat.setInputPaths(job, "/test/word.txt");

        //设置输出数据，输出路径不能存在
        FileOutputFormat.setOutputPath(job, new Path("/test/output/"));

        //提交我们的Job,返回结果是一个布尔值
        boolean result = job.waitForCompletion(true);

        //如果程序运行成功就打印"Task executed successfully!!!"
        if(result){
            System.out.println("Task executed successfully!!!");
        }else {
            System.out.println("Task execution failed...");
        }

        //如果程序是正常运行就返回0，否则就返回1
        System.exit(result ? 0 : 1);
    }
}
