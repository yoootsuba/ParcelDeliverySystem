public class Box extends Parcel {
    public static final double MAX_WEIGHT = 50;
    public static final int MIN_DIMENSION = 5;
    public static final int MAX_DIMENSION = 120;

    private final int length;
    private final int width;
    private final int height;

    public Box(String trackingNumber, String receiverName, double weight, int length, int width, int height) {
        this(trackingNumber, receiverName, weight, length, width, height, MAX_WEIGHT);
    }

    protected Box(String trackingNumber, String receiverName, double weight,
                  int length, int width, int height, double maxWeight) {
        super(trackingNumber, receiverName, weight, maxWeight);
        this.length = validateDimension(length, "Length");
        this.width = validateDimension(width, "Width");
        this.height = validateDimension(height, "Height");
    }

    public static int validateDimension(int value, String name) {
        if (value < MIN_DIMENSION || value > MAX_DIMENSION) {
            throw new InvalidParcelException(name + " must be from " + MIN_DIMENSION + " to " + MAX_DIMENSION + " cm!");
        }
        return value;
    }

    public String getType() { return "Box"; }

    public double getVolumetricWeight() {
        return ((long) length * width * height) / 3500.0;
    }

    public double getChargeableWeight() {
        return Math.ceil(Math.max(getWeight(), getVolumetricWeight()));
    }

    public double getBaseFee() { return 40 * getChargeableWeight(); }
}
