import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/** Singleton: the single store of all parcels. */
public class ParcelRegistry {
    private static final ParcelRegistry INSTANCE = new ParcelRegistry();

    private final Map<String, Parcel> parcels = new LinkedHashMap<>();

    private ParcelRegistry() { }

    public static ParcelRegistry getInstance() { return INSTANCE; }

    public void requireUnused(String trackingNumber) {
        if (parcels.containsKey(trackingNumber)) {
            throw new DuplicateParcelException();
        }
    }

    public void add(Parcel parcel) {
        requireUnused(parcel.getTrackingNumber());
        parcels.put(parcel.getTrackingNumber(), parcel);
    }

    public Parcel find(String trackingNumber) {
        Parcel parcel = parcels.get(trackingNumber);
        if (parcel == null) {
            throw new ParcelNotFoundException();
        }
        return parcel;
    }

    public Collection<Parcel> getAll() { return parcels.values(); }
}
