package com.suyeq.mapreduce.wordcount;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * @author : denglinhai
 * @date : 16:49 2023/4/10
 */
public class WordReduce extends Reducer<Text, IntWritable, Text, IntWritable> {
    //定义一个私有的对象，避免下面创建多个对象
    private IntWritable total = new IntWritable();

    /*
        Reducer的业务逻辑在reduce()方法中;

        接下来我们就要对"reduce(Text key, Iterable<IntWritable> values, Context context)"中个参数解释如下:
            key:
                还记得咱们自定义写的"context.write(this.word,this.one);"吗？
                这里的key指的是同一个单词(word),即相同的单词都被发送到同一个reduce函数进行处理啦。
            values:
                同理,这里的values指的是很多个数字1组成，每一个数字代表同一个key出现的次数。
            context:
                整个任务的上下文环境,我们写完Mapper或者Reducer都需要交给context框架去执行。
 */
    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {

        //定义一个计数器
        int sum = 0;

        //对同一个单词做累加操作,计算该单词出现的频率。
        for (IntWritable value:values){
            sum += value.get();
        }

        //包装结果
        total.set(sum);

        //将计算的结果交给context框架
        context.write(key, total);
    }
}
