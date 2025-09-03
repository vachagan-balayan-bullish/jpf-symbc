package sequential;

import gov.nasa.jpf.symbc.Debug;

public class SequentialTest1 {
    public static void main(String[] args) {
        int a = Debug.makeSymbolicInteger("a");
        int b = Debug.makeSymbolicInteger("b");
        test(a, b);
    }

    public static void test(int a, int b) {
        if(a != b) {
            if (Math.pow(b, 2) < a) {
                System.out.println("P001");
            } else {
                System.out.println("P002");
            }
        } else {
            System.out.println("P003");
        }
    }
}
