/** Strategy: each delivery service decides its own fee and eligibility. */
public interface DeliveryService {
    String getName();

    /** Throws ServiceNotAllowedException if the parcel cannot use this service. */
    void validate(Parcel parcel);

    double calculateFee(double baseFee);
}
