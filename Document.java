public class Document extends Parcel {
    public static final double MAX_WEIGHT = 0.5;

    private final int pages;

    public Document(String trackingNumber, String receiverName, double weight, int pages) {
        super(trackingNumber, receiverName, weight, MAX_WEIGHT);
        this.pages = validatePages(pages);
    }

    public static int validatePages(int pages) {
        if (pages < 1 || pages > 100) {
            throw new InvalidParcelException("Number of pages must be from 1 to 100!");
        }
        return pages;
    }

    public String getType() { return "Document"; }

    public double getChargeableWeight() { return getWeight(); }

    public double getBaseFee() { return 50; }

    public int getPages() { return pages; }
}
