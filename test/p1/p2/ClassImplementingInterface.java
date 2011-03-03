package p1.p2;

import java.util.List;

public class ClassImplementingInterface implements ExtendingSomeInterface {
    private boolean myMockField;
    // Should say: method implementation does not have JavaDoc
    public int aMethodWithoutJavaDoc(String s) {
        // Class without JavaDoc implements interface definition
        return 0;
    }

    /**
     * The JavaDoc
     *
     * @param system the system
     * @return byte array
     */
    // Should say nothing
    public byte[] aMethodWithJavaDoc(System system) {
        return new byte[0];
    }

    /**
     * @see p1.p2.SomeInterface#anAbstractMethodWithoutJavaDoc()
     */
    // Should say nothing
    public void anAbstractMethodWithoutJavaDoc() {
        Object o = new Object() {
            public String toString() {
                return super.toString();
            }
        };
        System.out.println(o);
    }

    /**
     * Some different JavaDoc here
     *
     * @param one One
     * @param two Two
     * @return String
     *
     * @see SomeInterface#anAbstractMethodWithJavaDoc(int,long)
     */
    // Should say: JavaDoc differs from JavaDoc in parent method
    public String anAbstractMethodWithJavaDoc(int one, long two) {
        return null;
    }

    // Should say: method implementation does not have Javadoc
    public List aDifferentMethodWithJavaDoc(List objects) {
        return null;
    }

    /**
     * Method overrideMe
     */
    // Should say: JavaDoc differs from JavaDoc in parent method
    public void overrideMe() {
    }
}
