package udf;

import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AmountNumberUDF extends GenericUDF {
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
        line = line.replace(",", "");

        // 去除余额金额后的字符串
        String wipeOffBalance = wipeOffBalance(line,"余额");
        String newLine = wipeOffBalance(wipeOffBalance,"贷款");
        newLine = wipeOffBalance(newLine,"最小还款额");
        newLine = wipeOffBalance(newLine,"最低还款额");

        ArrayList<String> list = new ArrayList<>();
        Pattern p1 = Pattern.compile("\\d+(\\.(\\d{1,2}))");  // 1111.00

        Matcher m1 = p1.matcher(newLine.trim());


        while (m1.find()) list.add(m1.group());
        return list.size() + "->" + Arrays.toString(list.toArray());
    }

    /**
     * 去除余额
     *
     * @param line
     * @return
     */
    private static String wipeOffBalance(String line, String keyWord) {
        String balanceKeyWord = "";
        Pattern p1 = Pattern.compile(keyWord + "\\d+(\\.(\\d{1,2}))");  // 1111.00
        Matcher m1 = p1.matcher(line.trim());
        if (m1.find()) balanceKeyWord = m1.group();
        return line.replace(balanceKeyWord, "");
    }


    @Override
    public String getDisplayString(String[] children) {
        return "";
    }
}
