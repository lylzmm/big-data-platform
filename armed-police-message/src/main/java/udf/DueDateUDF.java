package udf;

import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DueDateUDF extends GenericUDF {
    /**
     * @param arguments 输入参数类型的鉴别器对象
     * @return 返回值类型的鉴别器对象
     * @throws UDFArgumentException
     */
    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        // 判断输入参数的个数
        if (arguments.length != 1) {
            throw new UDFArgumentLengthException("Input Args Length Error!!!");
        }
        // 判断输入参数的类型
        if (!arguments[0].getCategory().equals(ObjectInspector.Category.PRIMITIVE)) {
            throw new UDFArgumentTypeException(0, "Input Args Type Error!!!");
        }


        //函数本身返回值为int，需要返回int类型的鉴别器对象
        return PrimitiveObjectInspectorFactory.javaStringObjectInspector;
    }


    /**
     * 函数的逻辑处理
     *
     * @param arguments 输入的参数
     * @return 返回值
     * @throws HiveException
     */
    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        if (arguments[0].get() == null) {
            return "";
        }
        // 定义返回值
        String result = "";
        // 获取输入值
        String line = arguments[0].get().toString();
//        String callTime = arguments[1].get().toString();

        line = line.replace(" ", "");


        // 获取还款日期
        String dueDate = dueDate(line, "还款日");
        // 格式化
        // 例如：05月29日 => 05-29
        // 例如：2021年05月29日 => 2021-05-29
        result = formatting(dueDate);

        // 还款日期没有在获取 2021-05-01
        if (result.length() == 0) {
            result = matchingT2(line);
        }

        // 在没有日期 匹配 2021年20月21日
        if (result.length() == 0) {
            result = matchingT1(line);
        }
//        // 05-29 加年
//        if (result.length() != 0) {
//            if (result.split("-").length < 3) {
//                String year = callTime.substring(0, 5);
//                result = year + result;
//            }
//        }


        return result;
    }

    public static String matchingT2(String line) {
        String date = "";
        ArrayList<String> list = new ArrayList<>();
        Pattern p1 = Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2}");
        Matcher m1 = p1.matcher(line.trim());
        while (m1.find()) list.add(m1.group());

        Map<Integer, String> map = new TreeMap<>();
        TreeSet<Integer> set = new TreeSet<>(Comparator.reverseOrder());
        if (list.size() > 1) {
            int index = line.indexOf("还款");
            for (String s : list) {
                int i = line.indexOf(s);
                int abs = Math.abs(i - index);
                set.add(abs);
                map.put(abs, s);
            }
            date = map.get(set.last());
        } else if (list.size() == 1) {
            date = list.get(0);
        }

        // 格式化
        // 例如：05月29日 => 05-29
        // 例如：2021年05月29日 => 2021-05-29
        return date;
    }

    public static String matchingT1(String line) {
        String dueDates = "";
        ArrayList<String> list = new ArrayList<>();
        Pattern p1 = Pattern.compile("\\d{1,2}月\\d{1,2}日");
        Pattern p2 = Pattern.compile("\\d{4}年\\d{1,2}月{1,2}日");
        Matcher m1 = p1.matcher(line.trim());
        Matcher m2 = p2.matcher(line.trim());
        while (m1.find()) list.add(m1.group());
        while (m2.find()) list.add(m2.group());

        Map<Integer, String> map = new TreeMap<>();
        TreeSet<Integer> set = new TreeSet<>(Comparator.reverseOrder());
        if (list.size() > 1) {
            int index = line.indexOf("还款");
            for (String s : list) {
                int i = line.indexOf(s);
                int abs = Math.abs(i - index);
                set.add(abs);
                map.put(abs, s);
            }
            dueDates = map.get(set.last());
        } else if (list.size() == 1) {
            dueDates = list.get(0);
        }

        // 格式化
        // 例如：05月29日 => 05-29
        // 例如：2021年05月29日 => 2021-05-29
        return formatting(dueDates);
    }

    public static String formatting(String dueDate) {
        String result = "";
        if (dueDate.contains("年") && dueDate.contains("月") && dueDate.contains("日")) {
            int indexYear = dueDate.indexOf("年");
            int indexMonth = dueDate.indexOf("月");
            int indexDay = dueDate.indexOf("日");

            System.out.println(dueDate);
            String year = dueDate.substring(0, indexYear);
            String month = addZero(dueDate.substring(indexYear + 1, indexMonth));
            String day = addZero(dueDate.substring(indexMonth + 1, indexDay));
            result = year + "-" + month + "-" + day;

        } else if (dueDate.contains("月") && dueDate.contains("日")) {
            int indexMonth = dueDate.indexOf("月");
            int indexDay = dueDate.indexOf("日");
            String month = addZero(dueDate.substring(0, indexMonth));
            String day = addZero(dueDate.substring(indexMonth + 1, indexDay));
            result = month + "-" + day;
        }
        return result;
    }


    public static String addZero(String col) {
        if (col.length() < 2) {
            return "0" + col;
        }
        return col;
    }

    public static String dueDate(String line, String keyWord) {

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
                if (key.contains("年") && !commaFlag) continue;
                if (key.contains("月") && !commaFlag) continue;
                if (key.contains("日")) {
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

        return line.substring(startIndex, stopIndex);
    }


    @Override
    public String getDisplayString(String[] children) {
        return "";
    }
}
