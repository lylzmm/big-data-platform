import udf.TrainTicket;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    public static void main(String[] args) {
        String line = "【同程旅行】客官，您在微信里预订的机票还未付款哦！乘客：杨贤泽，西安T2-安庆天柱山，预计2021-07-05 08:25:00起飞，总支付金额869.0元，请在2021-06-26 20:35之前戳这里支付https://s.ly.com/Pivwjh1kP 或者点击”微信钱包/支付--火车票机票--订单--飞机票”去付款哦";

        System.out.println(TrainTicket.getStationForAndDestination(line));


//        HashSet<String> set = new HashSet<>();
//        for (int i = 0; i < arr.length; i++) {
//            for (int j = i + 1; j < arr.length; j++) {
//                String k1 = arr[i].trim();
//                String k2 = arr[j].trim();
//                String stand = k1 + k2;
//                if (line.contains(stand)) set.add(stand);
//            }
//        }
//
//        ArrayList<String> list = new ArrayList<>(set);
//        if (set.size() < 2) {
//            for (String s : arr) {
//                for (String s1 : set) {
//                    if (!s1.contains(s.trim())) list.add(s);
//                }
//            }
//        }
//
//        System.out.println(list);


    }


//    public static String matchingT1(String line) {
//        String dueDates = "";
//        ArrayList<String> list = new ArrayList<>();
//        Pattern p1 = Pattern.compile("\\d{1,2}月\\d{1,2}日");
//        Pattern p2 = Pattern.compile("\\d{4}-\\d{1,2}-{1,2}");
//        Matcher m1 = p1.matcher(line.trim());
//        Matcher m2 = p2.matcher(line.trim());
//        while (m1.find()) list.add(m1.group());
//        while (m2.find()) list.add(m2.group());
//
//        Map<Integer, String> map = new TreeMap<>();
//        TreeSet<Integer> set = new TreeSet<>(Comparator.reverseOrder());
//        if (list.size() > 1) {
//            int index = line.indexOf("还款");
//            for (String s : list) {
//                int i = line.indexOf(s);
//                int abs = Math.abs(i - index);
//                set.add(abs);
//                map.put(abs, s);
//            }
//            dueDates = map.get(set.last());
//        } else if (list.size() == 1) {
//            dueDates = list.get(0);
//        }
//
//        // 格式化
//        // 例如：05月29日 => 05-29
//        // 例如：2021年05月29日 => 2021-05-29
//        return formatting(dueDates);
//    }
}



