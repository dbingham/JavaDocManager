package p1.p2;

/**
 * The SomeInterface class.
 *
 * @author Raymond P. Brandon
 * @version 1.0
 */
public interface SomeInterface {
    /**
     * Some JavaDoc here
     * @param one One
     * @param two Two
     * @return String
     */
    // Should say nothing
    public String anAbstractMethodWithJavaDoc(int one, long two);

    /**
     * Method anAbstractMethodWithoutJavaDoc
     */
    // Should say nothing
    public void anAbstractMethodWithoutJavaDoc();

    /**
     * Method overrideMee
     */
    // Should say nothing
    public void overrideMe();
}
