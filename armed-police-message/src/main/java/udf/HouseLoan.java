package udf;




import utils.StringUtil;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HouseLoan {
    public static String houseLoan(String line) {
        String result = "";
        // 去除年月日数字，年月日数字影响提取金额
        Pattern date = Pattern.compile("\\d{4}年\\d{2}月\\d{2}");
        Matcher dt = date.matcher(line.trim());
        if (dt.find()) {
            String key = dt.group();
            line = line.replace(key, "");
        }

        if (StringUtil.matchingSource(line).contains("银行")) {
            if (line.contains("江苏")) {
                result = getMoney(line, "还款");
            }
            if (result.length() == 0 && (line.contains("本息"))) result = getMoney(line, "本息");
            if (result.length() == 0 && (line.contains("房贷"))) result = getMoney(line, "房贷");
            if (result.length() == 0 && line.contains("还款")) result = getMoney(line, "还款");
        }

        // 验证金额是否正常，
        if (3 <= result.split("\\.")[0].length() && result.split("\\.")[0].length() <= 5 && result.contains(".")) {
            String msgOriginName = StringUtil.matchingSource(line);
            return msgOriginName + "->7->" + result;
        } else {
            // 有异常的金额打印出来
            System.out.println(line + "================" + result);
            return "异常";
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("input/房贷.csv")));
        String line = null;
        while ((line = br.readLine()) != null) {
            String borrowing = houseLoan(line);
            if (borrowing.contains("->7->")) {
                if ((borrowing.split("->").length < 3)) {

                    System.out.println(line + "==" + borrowing);
                }
            }
        }
        br.close();
    }

    public static String getMoney(String line, String keyWord) {
        String result = "";
        Pattern p = Pattern.compile("[0-9]");
        int startIndex = 0;
        int stopIndex = 0;
        int count = 0;
        boolean flag = false;
        boolean bracketFlag = false;
        boolean commaFlag = false;

        int start = line.indexOf(keyWord);

        if (start < 0) return result;

        // 找金额
        for (int i = start; i < line.length(); i++) {
            String key = line.substring(i, i + 1);

            if (key.contains("(") || key.contains("（")) {
                bracketFlag = true;
                continue;
            }

            if (bracketFlag && (key.contains(")") || key.contains("）"))) {
                bracketFlag = false;
            }

            // 括号找完了 如：  支出(消费101店面)，这样还没有找到金额就可以跳出循环了
            // 括号找完了 如：  支出(消费101店面)，这样还没有找到金额就可以跳出循环了
            if (!bracketFlag && !flag && (key.contains("，") || key.contains("；") || key.contains("。") || key.contains("、") || key.contains("：") || key.contains("！") || key.contains(","))) {
                if (i + 1 <= line.length()) {
                    String substring = line.substring(i + 1);
                    if (substring.contains(keyWord)) {
                        result = getMoney(substring, keyWord);
                    }
                }
                break;
            }

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

//            if (sp <= line.length()) {
//                String unit = line.substring(stopIndex, sp);
//                if (unit.equals("元") || unit.equals("$") || unit.contains(",")) {
//                    return result;
//                } else {
//                    result = "";
//                }
//            }
        }
        return result;
    }

}
