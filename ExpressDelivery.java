public class ExpressDelivery implements DeliveryService {
    public String getName() { return "Express"; }

    public void validate(Parcel parcel) { }

    public double calculateFee(double baseFee) { return baseFee * 1.5; }
}
