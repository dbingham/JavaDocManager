package p1.p2;

import java.util.List;

/**
 * The ExtendingSomeInterface class.
 *
 * @author Raymond P. Brandon
 * @version 1.0
 */
public interface ExtendingSomeInterface extends SomeInterface {
    // Should say: Interface method does not have JavaDoc
    public int aMethodWithoutJavaDoc(String s);

    /**
     * The JavaDoc
     *
     * @param system the system
     * @return byte array
     */
    // Should say nothing
    public byte[] aMethodWithJavaDoc(System system);

    /**
     * @param objects objects
     * @return bills
     */
    // Should say nothing
    public List aDifferentMethodWithJavaDoc(List objects);

    // Should say: overridden method does not have any JavaDoc
    public void overrideMe();
}
