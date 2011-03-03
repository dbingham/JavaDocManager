package p1.p2;

public enum ColorEnum {
    Red(255, 0, 0),
    Green(0, 255, 0),
    Blue(0, 0, 255);

    private int r;
    private int g;
    private int b;

    /**
     * Constructor ColorEnum creates a new ColorEnum instance.
     *
     * @param r of type int
     * @param g of type int
     * @param b of type int
     */
    ColorEnum(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    /**
     * Method getR returns the r of this ColorEnum object.
     *
     * @return int the r of this ColorEnum object.
     */
    public int getR() {
        return r;
    }

    /**
     * Method getG returns the g of this ColorEnum object.
     *
     * @return int the g of this ColorEnum object.
     */
    public int getG() {
        return g;
    }

    /**
     * Method getB returns the b of this ColorEnum object.
     *
     * @return int the b of this ColorEnum object.
     */
    public int getB() {
        return b;
    }
}
