import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Pattern;

public abstract class Parcel {
    public enum Status {
        PENDING("Pending"), DELIVERED("Delivered");

        private final String label;

        Status(String label) { this.label = label; }

        @Override
        public String toString() { return label; }
    }

    public static final double MIN_WEIGHT = 0.01;

    private static final Pattern NAME_FORMAT = Pattern.compile("\\p{L}[\\p{L} .'-]{0,29}");
    private static final Pattern TRACKING_FORMAT = Pattern.compile("[A-Za-z]{3}\\d{4}");

    private final String trackingNumber;
    private final String receiverName;
    private final double weight;
    private DeliveryService service = new StandardDelivery();
    private Status status = Status.PENDING;

    protected Parcel(String trackingNumber, String receiverName, double weight, double maxWeight) {
        this.trackingNumber = validateTrackingNumber(trackingNumber);
        this.receiverName = validateReceiverName(receiverName);
        this.weight = validateWeight(weight, maxWeight);
    }

    public static String normalizeTrackingNumber(String trackingNumber) {
        return trackingNumber.toUpperCase(Locale.ROOT);
    }

    /** Accepts 3 letters followed by 4 digits in any case; returns it in uppercase. */
    public static String validateTrackingNumber(String trackingNumber) {
        if (!TRACKING_FORMAT.matcher(trackingNumber).matches()) {
            throw new InvalidParcelException("Tracking number must be 3 letters followed by 4 digits!");
        }
        return normalizeTrackingNumber(trackingNumber);
    }

    public static String validateReceiverName(String receiverName) {
        if (!NAME_FORMAT.matcher(receiverName).matches()) {
            throw new InvalidParcelException("Receiver name must be 1 to 30 characters: letters, spaces, . ' - only!");
        }
        return receiverName;
    }

    public static double validateWeight(double weight, double maxWeight) {
        if (!(weight >= MIN_WEIGHT)) {
            throw new InvalidParcelException("Weight must be at least " + kg(MIN_WEIGHT) + " kg!");
        }
        if (weight > maxWeight) {
            throw new InvalidParcelException("Weight must not exceed " + kg(maxWeight) + " kg for this parcel type!");
        }
        return weight;
    }

    private static String kg(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    public abstract String getType();

    public abstract double getChargeableWeight();

    public abstract double getBaseFee();

    public double getDeliveryFee() {
        return service.calculateFee(getBaseFee());
    }

    public void changeService(DeliveryService newService) {
        newService.validate(this);
        service = newService;
    }

    public void markDelivered() {
        status = Status.DELIVERED;
    }

    public boolean isDelivered() { return status == Status.DELIVERED; }
    public String getTrackingNumber() { return trackingNumber; }
    public String getReceiverName() { return receiverName; }
    public double getWeight() { return weight; }
    public DeliveryService getService() { return service; }
    public Status getStatus() { return status; }
}
