package utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
    public static String getCurrentDay() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
        System.out.println();// new Date()为获取当前系统时间

        return df.format(new Date());
    }

    public static List<String> getColumn(String line) {

        List<String> columns = new ArrayList<>();
        if (!line.equals("") && !line.contains(",")) {
            String regEx = "[\\u3002\\uff0c\\uff1f\\uff1a\\uff01\\uff1b]";

            Pattern p = Pattern.compile(regEx);
            int start = 0;
            int stop = 0;
            for (int i = 0; i < line.length(); i++) {

                if (p.matcher(line.substring(i, i + 1)).find()) {
                    if (start == 0) {
                        columns.add(line.substring(start, i));
                        start = i;
                        stop = i;
                    } else {
                        start = stop;
                        stop = i;
                        columns.add(line.substring(start + 1, stop));
                    }
                }
            }
            columns.add(line.substring(stop + 1));
        } else if (line.contains(",")) {
            columns.addAll(Arrays.asList(line.split(",")));
        }
        return columns;
    }

    // 匹配括号
    public static String matchingParentheses(String line) {
        // 蚂蚁借呗首次10.00元借款已到中国银行尾号5637银行卡，该借款最近还款日期为2019-04-19。【蚂蚁借呗】
        String result = "";
        Pattern p5 = Pattern.compile("（.*?）"); // （借款到账）
        Pattern p6 = Pattern.compile("\\(.*?\\)"); // [中山农商银行]
        Matcher m5 = p5.matcher(line.trim());
        Matcher m6 = p6.matcher(line.trim());
        if (m5.find()) result = m5.group();
        if (m6.find() && result.equals("")) result = m6.group();
        return result.replace("（", "").replace("）", "").replace("(", "").replace(")", "");
    }

    // 匹配来源
    public static String matchingSource(String line) {
        // 蚂蚁借呗首次10.00元借款已到中国银行尾号5637银行卡，该借款最近还款日期为2019-04-19。【蚂蚁借呗】
        String msgOriginName = "";
        Pattern p5 = Pattern.compile("【.*?】"); // 【中山农商银行】
        Pattern p6 = Pattern.compile("\\[.*?\\]"); // 1111
        Matcher m5 = p5.matcher(line.trim());
        Matcher m6 = p6.matcher(line.trim());
        ArrayList<String> list = new ArrayList<>();
        while (m5.find()) list.add(m5.group());
        while (m6.find()) list.add(m6.group());

        if (list.size() > 1) {
            for (String s : list) if (s.contains("银行") || s.contains("农信") || s.contains("农金")) msgOriginName = s;
            if (msgOriginName.length() == 0) msgOriginName = list.get(1);

        } else if (list.size() == 1) {
            msgOriginName = list.get(0);
        }
        return msgOriginName.replace("【", "").replace("】", "").replace("[", "").replace("]", "");

    }

    // 匹配金额
    public static String matchingMoney(String line) {
        String result = "";
        Pattern p1 = Pattern.compile("\\d+(\\.(\\d{1,2}))");  // 1111.00
        Matcher m1 = p1.matcher(line.trim());

        if (m1.find()) result = m1.group();
        return result;
    }

    public static String getM(String line, String keyWord) {
        String result = "";
        Pattern p = Pattern.compile("[0-9]");
        int startIndex = 0;
        int stopIndex = 0;
        int count = 0;
        boolean flag = false;
        boolean bracketFlag = false;
        boolean commaFlag = false;


        int start = line.indexOf(keyWord);

        for (int i = start; i < line.length(); i++) {
            String key = line.substring(i, i + 1);

            // 循环字符
            // 第一种情况 支出(消费101店面)， 提取金额

            // 第二中情况 支出400.00元(消费) 提取金额


            if (key.contains("(") || key.contains("（")) {
                bracketFlag = true;
                continue;
            }

            if (bracketFlag && (key.contains(")") || key.contains("）"))) {
                bracketFlag = false;
            }

            // 括号找完了 如：  支出(消费101店面)，这样还没有找到金额就可以跳出循环了
            if (!bracketFlag && !flag && (key.contains("，") || key.contains("；") || key.contains("。") || key.contains("、") || key.contains("：") || key.contains("！")))
                break;

            // 找完括号了在找金额
            if (!bracketFlag && p.matcher(key).find() && count == 0) {
                startIndex = i;
                flag = true;
                count++;
                continue;
            }

            if (!bracketFlag && flag) {
                if (p.matcher(key).find()) continue;
                if (key.contains(",") && !commaFlag) continue;
                if (key.contains(".")) {
                    commaFlag = true;
                    continue;
                }
                stopIndex = i;
                break;
            }
        }

        if (startIndex != 0 && stopIndex == 0) {
            stopIndex = line.length();
        }

        // 找到了数字，但是要验证是否是金额。
        if (startIndex != 0) {
            result = line.substring(startIndex, stopIndex).replace(",", "").replace("[", "");
            // 验证金额是否是需要的
            int sp = stopIndex + 1;

            // 如果金额后面是亿元或者其他的就不是金额。
            if (sp <= line.length()) {
                String unit = line.substring(stopIndex, sp);
                if (unit.equals("亿")
                        || unit.contains("钻")
                        || unit.contains("万")
                        || unit.contains("天")
                        || unit.equals("分")
                        || unit.equals("金")
                        || unit.contains("%")
                        || unit.contains("折")
                        || unit.contains("银")
                        || unit.contains("年")
                        || unit.contains("-")
                        || unit.contains("千")
                        || unit.contains("的")
                        || unit.contains("手")
                        || unit.contains("月")
                        || unit.contains("信")
                        || unit.contains("期")
                ) {
                    result = "";
                }

            }
        }
        return result;
    }

    // 找有多少关键字
    public static int findKeyWordCount(String line, String keyWord) {
        int sum = 0;
        if (line.contains(keyWord)) {
            int i = 1;
            String substring = line.substring(line.indexOf(keyWord) + keyWord.length());
            sum = findKeyWordCount(substring, keyWord) + i;
        }
        return sum;
    }


}
