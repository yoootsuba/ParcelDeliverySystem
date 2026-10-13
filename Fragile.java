public class Fragile extends Box {
    public static final double MAX_WEIGHT = 30;

    private final String handlingNote;

    public Fragile(String trackingNumber, String receiverName, double weight,
                   int length, int width, int height, String handlingNote) {
        super(trackingNumber, receiverName, weight, length, width, height, MAX_WEIGHT);
        this.handlingNote = validateHandlingNote(handlingNote);
    }

    public static String validateHandlingNote(String handlingNote) {
        if (handlingNote.isBlank()) {
            throw new InvalidParcelException("Handling note must not be empty!");
        }
        return handlingNote;
    }

    @Override
    public String getType() { return "Fragile"; }

    @Override
    public double getBaseFee() { return super.getBaseFee() + 100; }

    public String getHandlingNote() { return handlingNote; }
}
