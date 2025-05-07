package mapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeGrouping {

    private static final double tWindow = Double.parseDouble(PriceMapper.param2); // Example value, you should use your global parameter here

    public static SimpleEntry<Integer, String> findTimeGroup(String time) {
        // 转换输入字符串
        int secondsSinceMidnight = Integer.parseInt(time.substring(0, 2)) * 3600 +
                Integer.parseInt(time.substring(2, 4)) * 60 +
                Integer.parseInt(time.substring(4, 6));

        // 定义时间边界
        int morningStart = 9 * 3600 + 30 * 60; // 09:30
        int morningEnd = 11 * 3600 + 30 * 60; // 11:30
        int afternoonStart = 13 * 3600; // 13:00
        int afternoonEnd = 15 * 3600; // 15:00

        // 计算上午的最大组数
        int maxMorningGroups = (morningEnd - morningStart) / (int)tWindow;

        // 计算所属组号
        int groupNumber;
        if (secondsSinceMidnight >= morningStart && secondsSinceMidnight < morningEnd) {
            // 上午
            groupNumber = (int) ((secondsSinceMidnight - morningStart) / tWindow) + 1;
        } else if (secondsSinceMidnight >= afternoonStart && secondsSinceMidnight < afternoonEnd) {
            // 下午
            groupNumber = (int) ((secondsSinceMidnight - afternoonStart) / tWindow) + maxMorningGroups + 1;
        } else if(secondsSinceMidnight == morningEnd){
            groupNumber = maxMorningGroups;
        } else if (secondsSinceMidnight == afternoonEnd) {
            groupNumber = maxMorningGroups * 2;
        } else {
            return null;
        }


        // 计算该组区间的开始时间
        int startTimeOfDay = groupNumber <= maxMorningGroups ? morningStart : afternoonStart;
        int groupStartTime = startTimeOfDay + (groupNumber - (groupNumber > maxMorningGroups ? maxMorningGroups : 0) - 1) * (int)tWindow;

        // 构建时间区间的表达
        LocalDate fixedDate = LocalDate.of(2019, 1, 2);
        LocalTime startTime = LocalTime.ofSecondOfDay(groupStartTime);
        LocalTime endTime = LocalTime.ofSecondOfDay(groupStartTime + (int)tWindow);

        /*DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d HH:mm");
        String timeRange = fixedDate.atTime(startTime).format(formatter) + " - " +
                fixedDate.atTime(endTime).format(formatter);*/
        // 创建日期时间格式化器，精确到毫秒
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

        // 生成时间范围字符串
        String timeRange = fixedDate.atTime(startTime).format(formatter) + " to " +
                fixedDate.atTime(endTime).format(formatter);

        return new SimpleEntry<>(groupNumber, timeRange);
    }

    // 键值对
    public static class SimpleEntry<K, V> implements java.util.Map.Entry<K, V> {
        private final K key;
        private V value;

        public SimpleEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() { return key; }
        @Override
        public V getValue() { return value; }
        @Override
        public V setValue(V value) { throw new UnsupportedOperationException(); }
    }


}