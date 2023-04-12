package com.suyeq.mapreduce.wordcount;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * @author : denglinhai
 * @date : 16:38 2023/4/10
 */
public class WordMap extends Mapper<LongWritable, Text, Text, IntWritable> {
    //注意哈，我们这里仅创建了2个对象，即word和one。
    private Text word = new Text();
    private IntWritable one = new IntWritable(1);

    /*
    Mapper中的业务逻辑写在map()方法中;

    接下来我们就要对"map(LongWritable key, Text value, Context context)"中个参数解释如下:
        key:
            依旧是某一行的偏移量
        value:
            对应上述偏移量的行内容
        context:
            整个任务的上下文环境,我们写完Mapper或者Reducer都需要交给context框架去执行。
     */
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        //拿到行数据
        String line = value.toString();

        //将行数据按照逗号进行切分
        String[] words = line.split(",");

        //遍历数组，把单词变成(word,1)的形式交给context框架
        for (String word : words){
            /*
            我们将输出KV发送给context框架,框架所需要的KV类型正式我们定义在继承Mapper时指定的输出KV类型,即Text和IntWritable类型。

            为什么不能写"context.write(new Text(word),new IntWritable(1));"？
                上述写法并不会占用过多的内存，因为JVM有回收机制;
                但是上述写法的确会大量生成对象,这回导致回收垃圾的时间占比越来越长,从而让程序变慢。
                因此，生产环境中并不推荐大家这样写。

            推荐大家参考apache hadoop mapreduce官方程序的写法,提前创建2个对象(即上面提到的word和one)，然后利用这两个对象来传递数据。
             */
            this.word.set(word);
            context.write(this.word, this.one);
        }
    }

}
