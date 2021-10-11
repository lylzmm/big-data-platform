package udf;

import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;


/**
 * 火车票解析
 */
public class TrainTicketUDF extends GenericUDF {


    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        // 判断输入参数的个数
        if (arguments.length != 2) {
            throw new UDFArgumentLengthException("Input Args Length Error!!!");
        }
        // 判断输入参数的类型
        if (!arguments[0].getCategory().equals(ObjectInspector.Category.PRIMITIVE)) {
            throw new UDFArgumentTypeException(0, "Input Args Type Error!!!");
        }


        //函数本身返回值为int，需要返回int类型的鉴别器对象
        return PrimitiveObjectInspectorFactory.javaStringObjectInspector;
    }

    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        if (arguments[0].get() == null) {
            return "";
        }
        String col1 = arguments[0].get().toString();
        String col2 = arguments[1].get().toString();

        // 提取火车票
        return TrainTicket.extract(col1, col2);
    }


    @Override
    public String getDisplayString(String[] strings) {
        return "";
    }
}
