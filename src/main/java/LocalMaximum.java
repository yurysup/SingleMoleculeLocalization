/**
 * Represents local maximum in dSTORM localization algorithm.
 * Every local maximum consists of its location in pixels and intensity.
 */
public class LocalMaximum {
    private int intensity;
    private int row;
    private int column;

    public LocalMaximum(int intensity, int row, int column) {
        this.intensity = intensity;
        this.row = row;
        this.column = column;
    }

    public int getIntensity() {
        return intensity;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }
}
