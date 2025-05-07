package reducer;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

//问题，这个group的序号是否按顺序？我把组号放在了mapper key的第一位，是否可以按顺序，清理后再放入数据。
public class PriceReducer extends Reducer<Text, Text, Text, Text> {


    //定义基本盘
    double qtyBase = 17170245800.0;
    public void reduce(Text key,Iterable <Text> values,Context context)throws IOException,InterruptedException {
        //接受key并切分其中数据
        String[] types = key.toString().split(",");
        int group ;
        //几种变量的设置
        double LargeBidQty = 0;
        double LargeBidPrice = 0;
        double BigBidQty = 0;
        double BigBidPrice = 0;
         double MidBidQty=0;
         double MidBidPrice=0;
         double SmallBidQty=0;
         double SmallBidPrice=0;
         double LargeOfferQty=0;
         double LargeOfferPrice = 0;
         double BigOfferQty=0;
         double BigOfferPrice = 0;
         double MidOfferQty=0;
         double MidOfferPrice=0;
         double SmallOfferQty=0;
         double SmallOfferPrice=0;
        group = Integer.parseInt(types[0]);

        //创建变量用于储存成交量和成交额
        double local_qty = 0;
        double local_price = 0;

        //遍历values
        for (Text value:values){
            //计算并更新成交量和成交额
            String[] parts = value.toString().split(",");
            local_qty += Double.parseDouble(parts[1]);
            local_price += Double.parseDouble(parts[0])*Double.parseDouble(parts[1]);
        }
        //判断超大单大单中单小单买卖单类型
        if (local_qty >= 200000 || local_price >= 1000000 || local_qty/qtyBase >= 0.003){
            //判断买卖
            if (types[3].equals("0") ) {
                LargeBidQty = local_qty;
                LargeBidPrice = local_price;
            }
            if (types[3].equals("1")) {
                LargeOfferQty = local_qty;
                LargeOfferPrice = local_price;
            }
        }else if(local_qty >= 60000  || local_price >= 300000  ||
                (local_qty/qtyBase >= 0.001 && local_qty/qtyBase < 0.003) ){
            //判断买卖
            if (types[3].equals("0") ) {
                BigBidQty = local_qty;
                BigBidPrice = local_price;
            }
            if (types[3].equals("1")) {
                BigOfferQty = local_qty;
                BigOfferPrice = local_price;
            }
        }else if (local_qty >= 10000  || local_price >= 50000 ||
                (local_qty/qtyBase >= 0.00017 && local_qty/qtyBase < 0.001)){
            //这个是判断买卖
            if (types[3].equals("0")) {
                MidBidQty = local_qty;
                MidBidPrice = local_price;
            }
            if (types[3].equals("1")) {
                MidOfferQty = local_qty;
                MidOfferPrice = local_price;
            }
        }else{
            //这个是判断买卖
            if (types[3].equals("0")) {
                SmallBidQty = local_qty;
                SmallBidPrice = local_price;
            }
            if (types[3].equals("1")) {
                SmallOfferQty = local_qty;
                SmallOfferPrice = local_price;
            }
        }
        //输出文件为对每笔符合筛选条件的成交单按最终需求进行计算的若干结果，同时包括每笔成交单所属于的时间区间的组别
        String output = (LargeBidPrice + BigBidPrice - LargeOfferPrice - BigOfferPrice)
                +"\t"+ (LargeBidPrice + BigBidPrice)+"\t" + (LargeOfferPrice + BigOfferPrice)
                +"\t"+ LargeBidQty +"\t"+LargeBidPrice+"\t"+LargeOfferQty+"\t"+LargeOfferPrice
                +"\t"+ BigBidQty +"\t"+BigBidPrice+"\t"+BigOfferQty+"\t"+BigOfferPrice
                +"\t"+ MidBidQty +"\t"+MidBidPrice+"\t"+MidOfferQty+"\t"+MidOfferPrice
                +"\t"+ SmallBidQty +"\t"+SmallBidPrice+"\t"+SmallOfferQty+"\t"+SmallOfferPrice;
        context.write(new Text(group + "," + types[1] + "\t"),new Text(output));
        //
    }
}
