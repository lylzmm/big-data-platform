package udf;


import utils.StringUtil;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class Spending {

    public static String spending(String line) {
        if (line.contains("已开通")) return "";

        String result = "";
        if (line.contains("支出") && !line.contains("银行") && !line.contains("元") && !line.contains("农信") && !line.contains("农金") && !line.contains("尾号")) {
            result = "";
        } else if (line.contains("支出") && !line.contains("收支记录") && !line.contains("交易明细") && !line.contains("账单明细") && !line.contains("还款")) {
            result = StringUtil.getM(line, "支出");
        } else if (line.contains("支取")) {
            result = StringUtil.getM(line, "支取");
        } else if (line.contains("消费")) {
            result = StringUtil.getM(line, "消费");
        }

        String msgOriginName = StringUtil.matchingSource(line);
        return msgOriginName + "->1->" + result;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("input/支出.csv")));
        String line = null;
        while ((line = br.readLine()) != null) {
            line = " 望江中队 短信内容：您尾号3360的储蓄卡7月5日0时54分消费支出人民币29.90元,活期余额12321.82元。[建设银行]   ";
//            System.out.println(line + "============");
            String borrowing = spending(line);
            if (borrowing.contains("->1->")) {
                if (!(borrowing.split("->").length < 3)) {
                    System.out.println(line + "" + borrowing);
                }
            }
        }
        br.close();
    }
}