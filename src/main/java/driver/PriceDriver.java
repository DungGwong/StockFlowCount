package driver;

import mapper.PriceGroupMapper;
import mapper.PriceMapper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import reducer.PriceGroupReducer;
import reducer.PriceReducer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class PriceDriver {
    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException, URISyntaxException {
        //初始化conf参数
        Configuration conf = new Configuration();
        conf.set("mapreduce.output.textoutputformat.separator", " ");

        // 获取传入的参数
        String param1 = args[0]; // 第一个参数
        String param2 = args[1]; // 第二个参数

        // 配置 Job
        conf.set("param1", param1);  // 将参数传入 Job 配置
        conf.set("param2", param2);
        Job job = Job.getInstance(conf, "Price");

        //设定mapper

        job.setJarByClass(PriceDriver.class);
        job.setMapperClass(PriceMapper.class);

        //设定reducer
        job.setReducerClass(PriceReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        TextInputFormat.addInputPath(job, new Path("/data/am_hq_trade_spot.txt"));
        TextInputFormat.addInputPath(job, new Path("/data/pm_hq_trade_spot.txt"));

        TextOutputFormat.setOutputPath(job, new Path("/outputNotGroup.txt"));

        job.waitForCompletion(true);

        //配置job2
        Job job2 = Job.getInstance(conf, "Calculate Total Amount Per Customer");
        job2.setJarByClass(PriceDriver.class);

        //配置mapper和reducer
        job2.setMapperClass(PriceGroupMapper.class);
        job2.setReducerClass(PriceGroupReducer.class);
        job2.setMapOutputKeyClass(Text.class);
        job2.setMapOutputValueClass(Text.class);
        job2.setOutputKeyClass(Text.class);
        job2.setOutputValueClass(DoubleWritable.class);

        FileInputFormat.addInputPath(job2,new Path("/outputNotGroup.txt/part-r-00000"));
        FileOutputFormat.setOutputPath(job2, new Path("/outputFinal.txt"));

        System.exit(job2.waitForCompletion(true) ? 0 : 1);
    }
}