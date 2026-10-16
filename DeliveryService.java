public interface DeliveryService {
    String getName();

    void validate(Parcel parcel);

    double calculateFee(double baseFee);
}
