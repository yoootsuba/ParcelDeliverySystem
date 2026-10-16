public class Fragile extends Box {
    public static final double MAX_WEIGHT = 30;
    public static final int MAX_NOTE_LENGTH = 50;

    private final String handlingNote;

    public Fragile(String trackingNumber, String receiverName, double weight,
                   int length, int width, int height, String handlingNote) {
        super(trackingNumber, receiverName, weight, length, width, height, MAX_WEIGHT);
        this.handlingNote = validateHandlingNote(handlingNote);
    }

    public static String validateHandlingNote(String handlingNote) {
        if (handlingNote.isBlank() || handlingNote.length() > MAX_NOTE_LENGTH) {
            throw new InvalidParcelException("Handling note must be 1 to " + MAX_NOTE_LENGTH + " characters!");
        }
        return handlingNote;
    }

    @Override
    public String getType() { return "Fragile"; }

    @Override
    public double getBaseFee() { return super.getBaseFee() + 100; }

    public String getHandlingNote() { return handlingNote; }
}
