package udf;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 收入
 */
public class Income {

    public static String income(String line) {
        String result = "";
        Pattern p1 = Pattern.compile("\\d+(\\.(\\d{1,2}))");  // 1111.00
        Pattern p2 = Pattern.compile("(\\d{1,3}\\,)+(\\d{1,3})(\\.(\\d{0,2}))"); // 1,11.00
        Pattern p3 = Pattern.compile("(\\d{1,3}\\,)+(\\d{1,3})");// 1,111

        Matcher m1 = p1.matcher(line.trim());
        Matcher m2 = p2.matcher(line.trim());
        Matcher m3 = p3.matcher(line.trim());


        String msgOriginName = "";
        Pattern p5 = Pattern.compile("【.*?】"); // 【中山农商银行】
        Pattern p6 = Pattern.compile("\\[.*?\\]"); // 1111
        Matcher m5 = p5.matcher(line.trim());
        Matcher m6 = p6.matcher(line.trim());
        if (m5.find()) msgOriginName = m5.group();
        if (m6.find() && msgOriginName.equals("")) msgOriginName = m6.group();


        msgOriginName = msgOriginName.replace("【", "").replace("】", "").replace("[", "").replace("]", "");


        // 工资
        if (line.contains("工资")) {
            // 退伙补贴
            if (line.contains("退伙")) {
                result = getMoney(line, "收入").replace(",", "");
                return "退伙->2->" + result;
            }

            // 休假补贴
            if (line.contains("休假")) {
                result = getMoney(line, "收入").replace(",", "");
                return "休假->2->" + result;
            }

            // 探亲路费
            if (line.contains("探亲")) {
                result = getMoney(line, "收入").replace(",", "");
                return "探亲->2->" + result;
            }

            // 差旅
            if (line.contains("差旅")) {
                result = getMoney(line, "收入").replace(",", "");
                return "差旅费->2->" + result;
            }

            // 通信
            if (line.contains("通信")) {
                result = getMoney(line, "收入").replace(",", "");
                return "通信费->2->" + result;
            }


            // 纯工资的
            if (line.contains("余额") || line.contains("银行") || line.contains("农信") || line.contains("农金")) {
                result = getMoney(line, "收入").replace(",", "");
            }

            return "工资->2->" + result;
        }


        // ATM
        if (line.contains("ATM")) {
            result = getMoney(line, "收入").replace(",", "");
            return "ATM->2->" + result;
        }

        // 转账
        if (line.contains("转账")) {
            result = getMoney(line, "收入").replace(",", "");
            return "转账->2->" + result;
        }

        // 提现
        if (line.contains("提现")) {
            // 去除广告 如：
            // 【回收私服】年终结算：您8270账号截至17:22共收入99252130元宝+96852130钻石，提现 tst.cn/8FVRQ 回T退订
            // 【合成提现服】支付成功：您0433账号截至13:21收入99252130元宝+96852130钻石，提取 swv.cn/EGUSt 退订回T
            if (line.contains("余额") || line.contains("微信零钱提现") || line.contains("QQ钱包提现")) {
                result = getMoney(line, "收入").replace(",", "");
                return "提现->2->" + result;
            }
            return "提现->2->" + result;
        }


        // 去除广告和
        if ((p1.matcher(line.trim()).find() || p2.matcher(line.trim()).find() || p3.matcher(line.trim()).find())
                && (line.contains("银行") || line.contains("农信") || line.contains("农金") || line.contains("余额") && line.contains("转帐") || line.contains("快捷支付收入") || line.contains("账户") || line.contains("尾号") || line.contains("卡号") || line.contains("账号"))
                && !line.contains("中国移动")
                && !line.contains("买家")
                && !line.contains("商品")
                && !line.contains("收支记录")
                && !line.contains("放款")
                && !line.contains("珍爱网")
        ) {
            result = getMoney(line, "收入").replace(",", "");
            if (result.length() == 0 && line.contains("存入")) {
                result = getMoney(line, "存入").replace(",", "");
            }
        } else if (line.contains("工商银行") && line.contains("元")) {
            result = getMoney(line, "收入").replace(",", "");
            if (result.length() == 0 && line.contains("存入")) {
                result = getMoney(line, "存入").replace(",", "");
            }
        } else if ((line.contains("收入") && line.contains("余额") && (line.indexOf("收入") < line.indexOf("余额")))) {
            result = getMoney(line, "收入").replace(",", "");
        }

        return msgOriginName + "->2->" + result;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("input/收入.csv")));
        String line = null;
        while ((line = br.readLine()) != null) {
//            line = "余额宝12月21日19时25分向您尾号0496的储蓄卡账户银联入账收入人民币500.00元,活期余额797.02元。[建设银行]";
            String borrowing = income(line);
            if (borrowing.contains("->2->")) {
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

        if (startIndex != 0) {
            result = line.substring(startIndex, stopIndex).replace(",", "").replace("[", "");
            // 验证金额是否是需要的
            int sp = stopIndex + 1;

            // 如果金额后面是亿元
            if (sp <= line.length()) {
                String unit = line.substring(stopIndex, sp);
                if (unit.equals("亿") || unit.equals("钻") || unit.equals("万") || unit.equals("分") || unit.equals("金"))
                    result = "";
            }
        }
        return result;
    }
}
