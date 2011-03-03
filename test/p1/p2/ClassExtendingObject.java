package p1.p2;

public class ClassExtendingObject {
    // Should say: method does not have JavaDoc
    public ClassExtendingObject() {
    }

    /**
     * Some JavaDoc here
     * @param x the parameter
     */
    // Should say nothing
    public ClassExtendingObject(String x) {
        System.out.println(x);
    }

    // Should say: method does not have JavaDoc
    public void methodWithoutJavaDoc() {
    }

    /**
     * JavaDoc for this method
     */
    // Should say nothing
    public void methodWithJavaDoc() {
    }

    /**
     * Method overridden from Object class
     * @return the string
     */
    // Should say nothing
    public String toString() {
        return super.toString();
    }
}
