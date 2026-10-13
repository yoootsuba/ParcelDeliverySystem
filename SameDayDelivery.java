public class SameDayDelivery implements DeliveryService {
    private static final double MAX_CHARGEABLE_WEIGHT = 10;

    public String getName() { return "Same Day"; }

    public void validate(Parcel parcel) {
        if (parcel.getChargeableWeight() > MAX_CHARGEABLE_WEIGHT) {
            throw new ServiceNotAllowedException("Same Day delivery is only for parcels up to 10 kg!");
        }
    }

    public double calculateFee(double baseFee) { return baseFee * 2 + 100; }
}
