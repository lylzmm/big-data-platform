package udf;





import utils.StringUtil;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 还款
 */
public class Reimbursement {
    public static String reimbursement(String line) {
        if (line.contains("已开通") || line.contains("恭喜您开通白条")) return "";

        String result = "";
        String consumptionKeyWord = "";
        String msgOriginName = StringUtil.matchingSource(line);
        line = line.replace(",", "");

        // 去除还款
        line = line.replace("还款日", "");
        // 去除余额金额
        String newLine = line.replace(getMoney(line, "余额"), "");
        // 去除贷款金额
        newLine = wipeOffBalance(newLine, "贷款", "");
        // 去除年华利率金额
        newLine = wipeOffBalance(newLine, "年利率为", "");
        // 去除最低还款的金额
        newLine = newLine.replace(getMoney(newLine, "最低还款"), "");
        // 去除手续费的金额
        newLine = newLine.replace(getMoney(newLine, "手续费"), "");

        // 去消费
        if (line.contains("消费")) {
            Pattern p3 = Pattern.compile("消费\\d+(\\.(\\d{1,2}))");  // 1111.00
            Pattern p4 = Pattern.compile("消费(\\d{1,3}\\,)+(\\d{1,3})(\\.(\\d{0,2}))"); // 1,11.00
            Matcher m3 = p3.matcher(newLine.trim());
            Matcher m4 = p4.matcher(newLine.trim());
            if (m4.find()) consumptionKeyWord = m4.group();
            if (consumptionKeyWord.length() == 0 && m3.find()) consumptionKeyWord = m3.group();

            if (consumptionKeyWord.length() != 0) {
                return "";
            }
        }


        // 匹配的金额装进list
        ArrayList<String> list = new ArrayList<>();
        Pattern p1 = Pattern.compile("\\d+(\\.(\\d{1,2}))元");  // 1111.00
        Matcher m1 = p1.matcher(newLine.trim());
        while (m1.find()) list.add(m1.group().replace("元", ""));


        // 当没有在找到带元的在降低匹配规则
        if (list.size() == 0) {
            Pattern p3 = Pattern.compile("\\d+(\\.(\\d{1,2}))");  // 1111.00
            Matcher m3 = p3.matcher(newLine.trim());
            while (m3.find()) list.add(m3.group());
        }


        Map<Integer, String> map = new TreeMap<>();
        TreeSet<Integer> set = new TreeSet<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2.compareTo(o1);
            }
        });
        if (list.size() > 1) {
            int index = line.indexOf("还款");
            for (String s : list) {
                int i = line.indexOf(s);
                int abs = Math.abs(i - index);
                set.add(abs);
                map.put(abs, s);
            }
            result = map.get(set.last());
        } else if (list.size() == 1) {
            result = list.get(0);
        }
        return msgOriginName + "->4->" + result;
    }


    /**
     * 去除余额
     *
     * @param line
     * @return
     */
    private static String wipeOffBalance(String line, String keyWord, String suffix) {
        String balanceKeyWord = "";
        Pattern p1 = Pattern.compile(keyWord + "\\d+(\\.(\\d{1,2}))" + suffix);  // 1111.00
        Matcher m1 = p1.matcher(line.trim());
        if (m1.find()) balanceKeyWord = m1.group();
        return line.replace(balanceKeyWord, "");
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("input/还款.csv")));
        BufferedWriter bw = new BufferedWriter(new FileWriter("input/还款_tmp.csv"));
        String line = null;
        while ((line = br.readLine()) != null) {
//            line = "【马上消费金融】";
            String borrowing = reimbursement(line);
            // 信用卡 花呗 借呗 京东 拍拍贷
            if (borrowing.contains("其他")) {
//                bw.write(line);
//                bw.write("'\n");
                if (!(borrowing.split("->").length < 3)) {
//                    if (borrowing.split("->")[2].split("\\.")[0].length() >= 6) {
//                        System.out.println(line + "" + borrowing);
//                    }
                    System.out.println(line + "==" + borrowing);
                }
            }
        }
        br.close();
        bw.close();
    }

    public static String getMoney(String line, String keyWord) {
        String result = "";
        Pattern p = Pattern.compile("[0-9]");
        int startIndex = 0;
        int stopIndex = 0;
        int count = 0;
        boolean flag = false;
        boolean commaFlag = false;
        int start = line.indexOf(keyWord);
        if (start < 0) return result;
        for (int i = start; i < line.length(); i++) {
            String key = line.substring(i, i + 1);

            // 括号找完了 如：  支出(消费101店面)，这样还没有找到金额就可以跳出循环了
            if (!flag && (key.contains("，") || key.contains("；") || key.contains("。") || key.contains("、") || key.contains("：") || key.contains("！") || key.contains(","))) {

                break;
            }
            // 找完括号了在找金额
            if (p.matcher(key).find() && count == 0) {
                startIndex = i;
                flag = true;
                count++;
                continue;
            }
            if (flag) {
                if (p.matcher(key).find()) continue;
                if (key.contains(".")) {
                    continue;
                }
                stopIndex = i;
                break;
            }
        }

        if (startIndex != 0 && stopIndex == 0) {
            stopIndex = line.length();
        }

        if (startIndex == 0) {
            stopIndex = line.indexOf(keyWord) + keyWord.length();
        }


        return line.substring(start, stopIndex);
    }
}
//    public static String reimbursement(String line) {
//
//        if(line.contains("已开通")) return "";
//        line = line.replace("还款日", "");
//
//        String result = "";
//        Pattern p1 = Pattern.compile("\\d+(\\.(\\d{1,2}))");  // 1111.00
//        Pattern p2 = Pattern.compile("(\\d{1,3}\\,)+(\\d{1,3})(\\.(\\d{0,2}))"); // 1,11.00
//        Pattern p3 = Pattern.compile("(\\d{1,3}\\,)+(\\d{1,3})");// 1,111
//        Pattern p4 = Pattern.compile("[0-9]");
//        Matcher m1 = p1.matcher(line.trim());
//        Matcher m2 = p2.matcher(line.trim());
//        Matcher m3 = p3.matcher(line.trim());
//
//        String msgOriginName = getmsgOriginName(line);
//
//        // 信用卡换款
//        if (line.contains("信用卡")) {
//
//            if (line.contains("最低应还") && (line.indexOf("低应还") + 1) == line.indexOf("应还")) {
//                String newStr = line.replace("最低应还", "");
//                result = getM(newStr, "应还");
//                return msgOriginName + "信用卡->4->" + result;
//            }
//
//            if (line.contains("还款")) result = getM(line, "还款");
//            if (result.length() == 0 && (line.contains("应还"))) result = getM(line, "应还");
//            if (result.length() == 0 && (line.contains("偿还"))) result = getM(line, "偿还");
//
//            return msgOriginName + "信用卡->4->" + result;
//        }
//
//        // 花呗
//        if (line.contains("花呗")) return "花呗->4->" + StringUtil.matchingMoney(line);
//
//        // 借呗
//        if (line.contains("借呗")) return "借呗->4->" + StringUtil.matchingMoney(line);
//
//        // 京东
//        if (line.contains("京东")) return "京东->4->" + StringUtil.matchingMoney(line);
//
//        // 360借条
//        if (line.contains("360借条")) {
//            result = getM(line, "成功还款");
//            return "360借条->4->" + result;
//        }
//
//        // 拍拍贷
//        if (line.contains("拍拍贷")) return "拍拍贷->4->" + StringUtil.matchingMoney(line);
//
//        // 捷信公司
//        if (line.contains("捷信") && !line.contains("已逾期")) return "捷信->4->" + StringUtil.matchingMoney(line);
//
//
//        // 还款 余额  银行
//        if (msgOriginName.contains("银行") || msgOriginName.contains("农金") || msgOriginName.contains("农信")) {
//            if (line.contains("最低应还") && (line.indexOf("低应还") + 1) == line.indexOf("应还")) {
//                String newStr = line.replace("最低应还", "");
//                result = getM(newStr, "应还");
//                return msgOriginName + "->4->" + result;
//            }
//
//            if (line.contains("还款")) result = getM(line, "还款");
//            if (result.length() == 0 && (line.contains("应还"))) result = getM(line, "应还");
//            if (result.length() == 0 && (line.contains("偿还"))) result = getM(line, "偿还");
//            return msgOriginName + "->4->" + result;
//        }
//
//        // 包含最低应还
//        if (line.contains("最低应还")) {
//            if (line.contains("最低应还") && (line.indexOf("低应还") + 1) == line.indexOf("应还")) {
//                String newStr = line.replace("最低应还", "");
//                result = getM(newStr, "应还");
//                return msgOriginName + "->4->" + result;
//            }
//
//            if (line.contains("还款")) result = getM(line, "还款");
//            if (result.length() == 0 && (line.contains("应还"))) result = getM(line, "应还");
//            if (result.length() == 0 && (line.contains("偿还"))) result = getM(line, "偿还");
//            return msgOriginName + "->4->" + result;
//        }
//
//
//        return msgOriginName + "->4->" + StringUtil.matchingMoney(line);
//    }


//
//    public static String getM(String line, String keyWord) {
//        String result = "";
//        Pattern p = Pattern.compile("[0-9]");
//        int startIndex = 0;
//        int stopIndex = 0;
//        int count = 0;
//        boolean flag = false;
//        boolean bracketFlag = false;
//        boolean commaFlag = false;
//
//        int start = line.indexOf(keyWord);
//
//        if (start < 0) return result;
//
//        for (int i = start; i < line.length(); i++) {
//            String key = line.substring(i, i + 1);
//
//            // 循环字符
//            // 第一种情况 支出(消费101店面)， 提取金额
//
//            // 第二中情况 支出400.00元(消费) 提取金额
//
//
//            if (key.contains("(") || key.contains("（")) {
//                bracketFlag = true;
//                continue;
//            }
//
//            if (bracketFlag && (key.contains(")") || key.contains("）"))) {
//                bracketFlag = false;
//            }
//
//            // 括号找完了 如：  支出(消费101店面)，这样还没有找到金额就可以跳出循环了
//            if (!bracketFlag && !flag && (key.contains("，") || key.contains("；") || key.contains("。") || key.contains("、") || key.contains("：") || key.contains("！") || key.contains(","))) {
//                if (i + 1 <= line.length()) {
//                    String substring = line.substring(i + 1);
//                    if (substring.contains(keyWord)) {
//                        result = getM(substring, keyWord);
//                    }
//                }
//                break;
//            }
//
//
//            // 找完括号了在找金额
//            if (!bracketFlag && p.matcher(key).find() && count == 0) {
//                startIndex = i;
//                flag = true;
//                count++;
//                continue;
//            }
//
//            if (!bracketFlag && flag) {
//                if (p.matcher(key).find()) continue;
//                if (key.contains(",") && !commaFlag) continue;
//                if (key.contains(".")) {
//                    commaFlag = true;
//                    continue;
//                }
//                stopIndex = i;
//                break;
//            }
//        }
//
//        if (startIndex != 0 && stopIndex == 0) {
//            stopIndex = line.length();
//        }
//
//        // 找到了数字，但是要验证是否是金额。
//        if (startIndex != 0) {
//            result = line.substring(startIndex, stopIndex).replace(",", "").replace("[", "");
//            // 验证金额是否是需要的
//            int sp = stopIndex + 1;
//
//            if (sp <= line.length()) {
//                String unit = line.substring(stopIndex, sp);
//                if (unit.equals("元") || unit.equals("$")) {
//                    return result;
//                } else {
//                    result = "";
//                }
//            }
//        }
//        return result;
//    }
