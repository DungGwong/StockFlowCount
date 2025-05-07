package mapper;

import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Objects;

import static mapper.TimeGrouping.findTimeGroup;

public class PriceMapper extends Mapper<LongWritable, Text, Text, Text> {
    //创建变量接受参数-securityID
    static String securityID ;
    //创建变量接受参数-TWindow
    static String param2;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        // 从 Job 配置中获取参数
        Configuration conf = context.getConfiguration();
        securityID = conf.get("param1");
        param2 = conf.get("param2");
    }

    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        // 解析成交表中数据
        //以“\t”为分割符将成交表中的每行数据切分，存为字符串数组
        String[] fields = value.toString().split("\t");
        //创建TradeTime和valid_Trade并接收数据
        String TradeTime = fields[15];
        String valid_Trade = TradeTime.substring(8);

        //初步筛选符合要求的数据：指定securityID、F表示成交单中确定成交的数据、成交时间落在目标时间区间
        if (fields[8].equals(securityID) && fields[14].equals("F") &&findTimeGroup(valid_Trade)!=null ) {
            //创建变量接收买单委托索引、卖单委托索引、单价、成交量
            String BidNum = fields[10];
            String OfferNum = fields[11];
            String Price = fields[12];
            String TradeQty = fields[13];

            //创建TimeGrouping.SimpleEntry，用于储存当前成交单根据时间分组的组别信息（组号、对应的时间段）
            TimeGrouping.SimpleEntry<Integer, String> group = findTimeGroup(valid_Trade);
            //创建输出变量
            String KeyForNext;
            //判断买卖方委托索引皆不为0
            if (!Objects.equals(BidNum, "0") && !Objects.equals(OfferNum, "0")) {
                //通过添加key的组成改变key的认定，可以更好地区分不同组别
                //首位：group的下标
                //末尾数字：1为主动卖单的标记,0为主动买单的标记

                //买方委托索引大于卖方委托索引，则该成交单为主动买单；反之卖单
                if(Long.parseLong(BidNum) <= Long.parseLong(OfferNum)){
                    KeyForNext = group.getKey().toString() + "," + group.getValue() + "," + OfferNum + "," + 1;
                    context.write(new Text(KeyForNext), new Text(Price + "," + TradeQty));
                } else {
                    KeyForNext = group.getKey().toString() + "," + group.getValue() + "," + BidNum + "," + 0;
                    context.write(new Text(KeyForNext), new Text(Price + "," + TradeQty));
                }
        }
    }
}}
