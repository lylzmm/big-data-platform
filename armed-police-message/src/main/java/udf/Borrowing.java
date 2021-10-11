package udf;



import utils.StringUtil;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * * 借款
 */
public class Borrowing {
    public static String borrowing(String line) {
        if (line.contains("已开通")) return "";
        String result = "";
        String msgOriginName = StringUtil.matchingSource(line);


        Pattern p1 = Pattern.compile("\\d+(\\.(\\d{1,2}))");  // 1111.00
        Pattern p2 = Pattern.compile("(\\d{1,3}\\,)+(\\d{1,3})(\\.(\\d{0,2}))"); // 1,11.00
        Matcher m1 = p1.matcher(line.trim());
        Matcher m2 = p2.matcher(line.trim());

        // 匹配金额
        if (m2.find()) result = m2.group();
        if (result.length() == 0 && m1.find()) result = m1.group();

        if (result.split("\\.").length < 3) result = "";
        return msgOriginName + "->3->" + result;
    }


//    public static String borrowing(String line) {
//        String result = "";
//        Pattern p = Pattern.compile("\\d{1,8}");
//
//        // 匹配来源
//        String msgOriginName = StringUtil.matchingSource(line);
//
//        // 蚂蚁借呗
//        if (line.contains("借呗")) {
//            if (line.contains("已到")) result = StringUtil.matchingMoney(line);
//            if (line.contains("正在借款")) result = getM(line, "正在借款");
//            return "蚂蚁借呗->3->" + result;
//        }
//
//        //
//        if (line.contains("京东金融")) {
//            // 如果包含 如:【京东金融】（借款到账）您申请的金条借款1000.00元，在尾号7214的银行卡中已到账，查看
//            if (StringUtil.matchingParentheses(line).length() != 0) result = getM(line, "借款");
//            return "京东金融->3->" + result;
//        }
//
//        if (line.contains("借款") && p.matcher(line.trim()).find()
//                && !line.contains("失败")
//                && !line.contains("已结清")
//                && !line.contains("开通")
//                && !line.contains("额度")
//                && !line.contains("立享")
//                && !line.contains("领取")
//                && !line.contains("中国移动")
//                && !line.contains("中国联通")
//                && !line.contains("中国电信")
//                && !line.contains("可领")
//                && !line.contains("蚂蚁借呗")
//                && !line.contains("未通过审核")
//                && !line.contains("放款")
//        ) {
//            result = getM(line, "借款");
//        } else if (line.contains("蚂蚁借呗")) {
//            result = getM(line, "借呗");
//        } else if ((line.length() > 10 && line.contains("放款") && !line.contains("已获得") && !line.contains("立即领"))) {
//            result = getM(line, "放款");
//        }
//
//        return msgOriginName + "->3->" + result;
//    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("input/借款.csv")));
        String line = null;
        while ((line = br.readLine()) != null) {
//             line = "【极速贷】";
            String borrowing = borrowing(line);
            if (borrowing.contains("京东金融")) {

                if ((borrowing.split("->").length < 3)) {
                    System.out.println(line + "==" + borrowing);
                }
            }
        }
        br.close();
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


            if (key.contains("(") || key.contains("（") || key.contains("【")) {
                bracketFlag = true;
                continue;
            }

            if (bracketFlag && (key.contains(")") || key.contains("）") || key.contains("】"))) {
                bracketFlag = false;
            }

            // 括号找完了 如：  支出(消费101店面)，这样还没有找到金额就可以跳出循环了
            if (!bracketFlag && !flag && (key.contains("，") || key.contains("；") || key.contains("。") || key.contains("、") || key.contains("！") || key.contains(",")))
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

        if (startIndex != 0) {
            result = line.substring(startIndex, stopIndex).replace(",", "").replace("[", "");
            // 验证金额是否是需要的
            int st = stopIndex;
            int sp = st + 1;

            // 如果金额后面是亿元
            if (sp <= line.length()) {
                String unit = line.substring(st, sp);
                if (!unit.equals("元"))
                    result = "";
                else {
                    // 获取关键字到金额中间的字符
                    String middle = line.substring(line.indexOf(keyWord) + keyWord.length(), startIndex);
                    if (middle.contains("额度")
                            || middle.contains("最高")
                            || middle.contains("费率")
                            || middle.contains("达标再送")
                            || middle.contains("领取")
                            || middle.contains("还款")
                            || middle.contains("调整")
                            || middle.contains("信用")
                            || middle.contains("应还")
                            || middle.contains("日息")
                            || middle.contains("提升")
                    ) result = "";
                }
            }
        }
        return result;
    }
}
