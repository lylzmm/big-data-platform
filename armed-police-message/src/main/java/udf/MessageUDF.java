package udf;


import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;

public class MessageUDF extends GenericUDF {


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
        line = line.replace(" ","");
        String result = "";
        try {

            if (line.contains("失败") || line.contains("已逾期") || line.contains("未成功")) return "";

            // 8 车贷
            if (line.contains("车贷")) {
                result = CarLoans.carLoans(line);
                return result;
            }
            // 7 房贷
            if (line.contains("房贷")) {
                result = HouseLoan.houseLoan(line);
                return result;
            }

            // 2 收入
            // 要去除借款 例如：借款收入
            // 要去除放款 例如：放款收入
            if (line.contains("收入") && !line.contains("借款") && !line.contains("放款")) { // !借款 !放款
                result = Income.income(line);
            }

            // 4 还款
            // 要去除 消费3799.64元，分6期还款，
            if (line.contains("还款")) { // !支出  !支付 !借款 !消费
                result = Reimbursement.reimbursement(line);
            }

            // 1 支出
            if (result.split("->").length < 3 && (line.contains("支出") || line.contains("支取") || line.contains("支付") || line.contains("消费"))) {
                return Spending.spending(line);
            }

            // 3 借款
            if (result.split("->").length < 3 && (line.contains("借款") || line.contains("取现到账") || line.contains("放款"))) {
                return Borrowing.borrowing(line);
            }


            return result;

        } catch (Exception e) {
            System.out.println("---------" + line);
            return "异常";
        }
    }


    @Override
    public String getDisplayString(String[] children) {
        return "";
    }
}
