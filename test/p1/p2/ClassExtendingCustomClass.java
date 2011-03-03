package p1.p2;

public class ClassExtendingCustomClass extends ClassExtendingObject {

    /**
     * JavaDoc here
     */
    // Should say nothing because constructor doc does not have to match
    public ClassExtendingCustomClass() {
    }

    /**
     * Some JavaDoc here
     * @param xy the parameter
     */
    // Should say nothing because constructor doc does not have to match
    public ClassExtendingCustomClass(String xy) {
        // JavaDoc differs and even parameter names differ
        super(xy);
    }

    /**
     * Method methodWithoutJavaDoc
     *
     */
    // Should say: overridden method does not have JavaDoc
    public void methodWithoutJavaDoc() {
        super.methodWithoutJavaDoc();
    }

    /**
     * Some JavaDoc here
     * @return v
     */
    // Should say nothing
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Different JavaDoc here
     *
     * @return the string
     */
    // Should say: JavaDoc differs from JavaDoc in parent method
    public String toString() {
        return super.toString();
    }
}
