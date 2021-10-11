package udf;


import java.io.IOException;
import java.util.Comparator;
import java.util.TreeSet;

public class Test {
    public static void main(String[] args) throws IOException {
        TreeSet<Integer> set = new TreeSet<>(Comparator.reverseOrder());
        set.add(1);
        set.add(2);
        set.add(3);
        System.out.println(set);
    }
}
