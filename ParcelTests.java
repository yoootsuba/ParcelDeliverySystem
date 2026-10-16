/** Dependency-free tests. Run: java -cp out ParcelTests */
public class ParcelTests {
    private static int failures = 0;

    public static void main(String[] args) {
        specExample();
        chargeableWeightRounding();
        actualWeightBeatsVolumetric();
        sameDayLimit();
        documentFees();
        weightLimits();
        inputRules();
        registry();

        if (failures > 0) {
            System.out.println(failures + " test(s) failed");
            System.exit(1);
        }
        System.out.println("All tests passed");
    }

    private static void specExample() {
        Parcel p = new Fragile("ABC0001", "Ana", 1.8, 40, 20, 10, "Glass");
        p.changeService(new ExpressDelivery());
        assertEquals("spec: chargeable weight", 3, p.getChargeableWeight());
        assertEquals("spec: base fee", 220, p.getBaseFee());
        assertEquals("spec: express fee", 330, p.getDeliveryFee());
    }

    private static void chargeableWeightRounding() {
        assertEquals("volumetric exactly 2.0 -> 2", 2, new Box("ABC0002", "Bo", 1, 7, 10, 100).getChargeableWeight());
        assertEquals("volumetric 2.1 -> 3", 3, new Box("ABC0003", "Bo", 1, 7, 10, 105).getChargeableWeight());
    }

    private static void actualWeightBeatsVolumetric() {
        Box b = new Box("ABC0004", "Bo", 5, 5, 5, 5);
        assertEquals("actual weight used when larger", 5, b.getChargeableWeight());
        assertEquals("box base fee = 40 x chargeable", 200, b.getBaseFee());
    }

    private static void sameDayLimit() {
        Box atLimit = new Box("ABC0005", "Cy", 10, 5, 5, 5);
        atLimit.changeService(new SameDayDelivery());
        assertEquals("same day at 10 kg allowed, fee", 900, atLimit.getDeliveryFee());

        Box overLimit = new Box("ABC0006", "Cy", 11, 5, 5, 5);
        assertThrows("same day at 11 kg rejected", ServiceNotAllowedException.class,
                () -> overLimit.changeService(new SameDayDelivery()));
        assertEquals("rejected parcel keeps standard fee", 440, overLimit.getDeliveryFee());
    }

    private static void documentFees() {
        Document d = new Document("ABC0007", "Di", 0.3, 10);
        assertEquals("document standard", 50, d.getDeliveryFee());
        d.changeService(new ExpressDelivery());
        assertEquals("document express", 75, d.getDeliveryFee());
        d.changeService(new SameDayDelivery());
        assertEquals("document same day", 200, d.getDeliveryFee());
    }

    private static void weightLimits() {
        check("document 0.5 kg allowed", new Document("ABC0008", "Di", 0.5, 1) != null);
        assertThrows("document 0.51 kg rejected", InvalidParcelException.class,
                () -> new Document("ABC0009", "Di", 0.51, 1));
        assertThrows("box 51 kg rejected", InvalidParcelException.class,
                () -> new Box("ABC0010", "Bo", 51, 10, 10, 10));
        assertThrows("fragile 31 kg rejected", InvalidParcelException.class,
                () -> new Fragile("ABC0011", "Fe", 31, 10, 10, 10, "Glass"));
        assertThrows("weight below minimum rejected", InvalidParcelException.class,
                () -> Parcel.validateWeight(0.001, 50));
        assertThrows("NaN weight rejected", InvalidParcelException.class,
                () -> Parcel.validateWeight(Double.NaN, 50));
    }

    private static void inputRules() {
        check("tracking uppercased", Parcel.validateTrackingNumber("xyz9999").equals("XYZ9999"));
        assertThrows("tracking with 3 digits rejected", InvalidParcelException.class,
                () -> Parcel.validateTrackingNumber("ABC123"));
        assertThrows("tracking with digit prefix rejected", InvalidParcelException.class,
                () -> Parcel.validateTrackingNumber("1BC1234"));
        check("receiver with spaces allowed", Parcel.validateReceiverName("Juan dela Cruz") != null);
        assertThrows("numeric receiver rejected", InvalidParcelException.class,
                () -> Parcel.validateReceiverName("12345"));
        assertThrows("31-char receiver rejected", InvalidParcelException.class,
                () -> Parcel.validateReceiverName("A".repeat(31)));
        assertThrows("dimension 4 cm rejected", InvalidParcelException.class,
                () -> Box.validateDimension(4, "Length"));
        assertThrows("dimension 121 cm rejected", InvalidParcelException.class,
                () -> Box.validateDimension(121, "Length"));
        assertThrows("101 pages rejected", InvalidParcelException.class,
                () -> Document.validatePages(101));
    }

    private static void registry() {
        ParcelRegistry registry = ParcelRegistry.getInstance();
        check("registry is a singleton", registry == ParcelRegistry.getInstance());
        registry.add(new Document("abc1000", "Ana", 0.3, 10));
        check("stored in uppercase", registry.find("ABC1000").getTrackingNumber().equals("ABC1000"));
        assertThrows("duplicate ignores case", DuplicateParcelException.class,
                () -> registry.requireUnused(Parcel.validateTrackingNumber("AbC1000")));
        assertThrows("unknown parcel", ParcelNotFoundException.class, () -> registry.find("ZZZ0000"));
    }

    private static void assertEquals(String name, double expected, double actual) {
        check(name + " (expected " + expected + ", got " + actual + ")", Math.abs(expected - actual) < 0.001);
    }

    private static void assertThrows(String name, Class<? extends RuntimeException> type, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException e) {
            check(name, type.isInstance(e));
            return;
        }
        check(name + " (nothing thrown)", false);
    }

    private static void check(String name, boolean ok) {
        if (!ok) {
            failures++;
        }
        System.out.println((ok ? "PASS " : "FAIL ") + name);
    }
}
