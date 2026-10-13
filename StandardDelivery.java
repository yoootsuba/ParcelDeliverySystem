public class StandardDelivery implements DeliveryService {
    public String getName() { return "Standard"; }

    public void validate(Parcel parcel) { }

    public double calculateFee(double baseFee) { return baseFee; }
}
