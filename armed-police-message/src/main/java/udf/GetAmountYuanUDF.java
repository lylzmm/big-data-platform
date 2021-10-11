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

public class GetAmountYuanUDF extends GenericUDF {
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
        String line = arguments[0].get().toString();
        if (line.contains("已开通") || line.contains("恭喜您开通白条")) return "";
        line = line.replace(",", "");

        String result = "";
        String consumptionKeyWord = "";
        line = line.replace("还款日", "");
        // 去除余额金额后的字符串
        String wipeOffBalance = wipeOffBalance(line, "余额","");
        // 去除贷款的金额
        String newLine = wipeOffBalance(wipeOffBalance, "贷款","");
        // 去最小还款额
        newLine = wipeOffBalance(newLine, "最小还款额","");
        newLine = wipeOffBalance(newLine, "最低还款额为","");
        newLine = wipeOffBalance(newLine, "年利率为","");
        newLine = wipeOffBalance(newLine, "","%");

        // 去消费
        if (line.contains("消费")) {
            Pattern p3 = Pattern.compile("消费\\d+(\\.(\\d{1,2}))");  // 1111.00
            Pattern p4 = Pattern.compile("消费(\\d{1,3}\\,)+(\\d{1,3})(\\.(\\d{0,2}))"); // 1,11.00
            Matcher m3 = p3.matcher(newLine.trim());
            Matcher m4 = p4.matcher(newLine.trim());
            if (m3.find()) consumptionKeyWord = m3.group();
            if (consumptionKeyWord.length() == 0 && m4.find()) consumptionKeyWord = m4.group();

            if (consumptionKeyWord.length() != 0) {
                return "";
            }
        }


        // 匹配的金额装进list
        ArrayList<String> list = new ArrayList<>();
        Pattern p1 = Pattern.compile("\\d+(\\.(\\d{1,2}))元");  // 1111.00
        Pattern p2 = Pattern.compile("\\d+(\\.(\\d{1,2})) 元");  // 1111.00
        Matcher m1 = p1.matcher(newLine.trim());
        Matcher m2 = p2.matcher(newLine.trim());
        while (m1.find()) list.add(m1.group().replace("元",""));
        while (m2.find()) list.add(m2.group().replace(" 元",""));

        // 当没有在找到带元的在降低匹配规则
        if(list.size() == 0) {
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
        return result;
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


    @Override
    public String getDisplayString(String[] children) {
        return "";
    }
}
