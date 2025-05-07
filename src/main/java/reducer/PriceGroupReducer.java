package reducer;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.TreeMap;

//问题，这个group的序号是否按顺序？我把组号放在了mapper key的第一位，是否可以按顺序，清理后再放入数据。
public class PriceGroupReducer extends Reducer<Text, Text, Text, Text> {
    //用于存储数据并排序输出
    private TreeMap<Integer, String> sortByGroup = new TreeMap<>();
    private boolean headerWritten = false; // 标志表头是否已写入

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        // 输出表头
        if (!headerWritten) {
            String header = "主力净流入,主力流入,主力流出,超大买单成交量,超大买单成交额,超大卖单成交量,超大卖单成交额,大买单成交量,大买单成交额,大卖单成交量,大卖单成交额,中买单成交量,中买单成交额,中卖单成交量,中卖单成交额,小买单成交量,小买单成交额,小卖单成交量,小卖单成交额,时间区间";
            context.write(new Text(header), null);
            headerWritten = true;
        }
    }


    public void reduce(Text key,Iterable <Text> values,Context context)throws IOException,InterruptedException {
        //接收values
        //sumResult用于储存 对每笔成交单的目标结果进行加和的最终结果
        double[] sumResult = new double[19];
        String[] group = key.toString().split(",");
        for(Text value : values){
            String[] data = value.toString().split("\t");
            for(int i = 0; i < 19 ; i++){
                sumResult[i] += Double.parseDouble(data[i]);
            }

        }
        StringBuilder outputResult = new StringBuilder();
        //将sumResult的结果用逗号拼接（即输出为csv）
        for(double d : sumResult){
            outputResult.append(d).append(",");
        }
        outputResult.append(group[1]);
        sortByGroup.put(Integer.parseInt(group[0]),outputResult.toString());


    }
    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        //按组号升序输出
        for (Integer amount : sortByGroup.keySet()) {
            context.write(new Text(sortByGroup.get(amount)), null);
        }
    }
}
