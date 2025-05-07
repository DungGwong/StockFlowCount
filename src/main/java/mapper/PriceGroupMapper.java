package mapper;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class PriceGroupMapper extends Mapper<LongWritable, Text, Text, Text> {

    //
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        // 解析大表数据
        String[] fields = value.toString().split("\t");
        String group = fields[0];
        //构建出输出数据
        StringBuilder data = new StringBuilder();
        for(int i = 1; i < fields.length; i++){
            data.append(fields[i]);
            data.append("\t");
        }
        //输出key是组别信息（包括组号和对应时间区间），value是对每个主动单计算出的目标数据
        context.write(new Text(group), new Text(data.toString()));

    }
}