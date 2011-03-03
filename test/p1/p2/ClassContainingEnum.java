package p1.p2;

enum MyColorEnum { Red, Green, Blue}

public class ClassContainingEnum {

    private MyColorEnum myColorEnum;

    public static void main(String[] args) {
        MyColorEnum myRedColorEnum = MyColorEnum.Red;
        MyColorEnum myGreenColorEnum = MyColorEnum.Green;
        MyColorEnum myBlueColorEnum = MyColorEnum.Blue;
    }
}
