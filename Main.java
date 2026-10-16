import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;

public class Main {
    private static final Scanner IN = new Scanner(System.in);
    private static final ParcelRegistry REGISTRY = ParcelRegistry.getInstance();
    private static final List<DeliveryService> SERVICES =
            List.of(new StandardDelivery(), new ExpressDelivery(), new SameDayDelivery());
    private static final String SERVICE_PROMPT = "Delivery service (1. Standard  2. Express  3. Same Day): ";

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n=== PARCEL DELIVERY SYSTEM === (type 'exit' anytime to quit)");
            System.out.println("1. Add Parcel");
            System.out.println("2. View All Parcels");
            System.out.println("3. Change Delivery Service");
            System.out.println("4. Mark as Delivered");
            System.out.println("5. Exit");
            try {
                switch (choice("Choose: ", 5, "Invalid choice!")) {
                    case 1 -> addParcel();
                    case 2 -> viewParcels();
                    case 3 -> changeService();
                    case 4 -> markDelivered();
                    case 5 -> quit();
                }
            } catch (ParcelException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static void addParcel() {
        int type = choice("Parcel type (1. Document  2. Box  3. Fragile): ", 3, "Invalid parcel type!");
        String tracking = ask("Tracking number: ", s -> {
            String normalized = Parcel.validateTrackingNumber(s);
            REGISTRY.requireUnused(normalized);
            return normalized;
        });
        String receiver = ask("Receiver name: ", Parcel::validateReceiverName);
        double maxWeight = type == 1 ? Document.MAX_WEIGHT : type == 2 ? Box.MAX_WEIGHT : Fragile.MAX_WEIGHT;
        double weight = ask("Weight (kg): ", s -> Parcel.validateWeight(toDouble(s), maxWeight));

        Parcel parcel;
        if (type == 1) {
            int pages = askInt("Number of pages: ", Document::validatePages);
            parcel = new Document(tracking, receiver, weight, pages);
        } else {
            int length = askInt("Length (cm): ", v -> Box.validateDimension(v, "Length"));
            int width = askInt("Width (cm): ", v -> Box.validateDimension(v, "Width"));
            int height = askInt("Height (cm): ", v -> Box.validateDimension(v, "Height"));
            parcel = type == 2
                    ? new Box(tracking, receiver, weight, length, width, height)
                    : new Fragile(tracking, receiver, weight, length, width, height,
                                  ask("Handling note: ", Fragile::validateHandlingNote));
        }

        ask(SERVICE_PROMPT, s -> {
            DeliveryService service = toService(s);
            parcel.changeService(service);
            return service;
        });
        REGISTRY.add(parcel);
        System.out.printf(Locale.US, "Parcel added successfully! Delivery fee: Php %.2f%n", parcel.getDeliveryFee());
    }

    private static void viewParcels() {
        if (!hasParcels()) {
            return;
        }
        int nameWidth = Math.max("Receiver".length(),
                REGISTRY.getAll().stream().mapToInt(p -> p.getReceiverName().length()).max().getAsInt());
        String format = "%-8s  %-8s  %-" + nameWidth + "s  %10s  %-8s  %10s  %-9s";

        String header = String.format(format,
                "Tracking", "Type", "Receiver", "Weight(kg)", "Service", "Fee (Php)", "Status");
        System.out.println(header);
        System.out.println("-".repeat(header.length()));
        for (Parcel p : REGISTRY.getAll()) {
            System.out.println(String.format(format,
                    p.getTrackingNumber(), p.getType(), p.getReceiverName(), twoDecimals(p.getChargeableWeight()),
                    p.getService().getName(), twoDecimals(p.getDeliveryFee()), p.getStatus()));
        }
    }

    /** Prints a message and returns false when there is nothing to show or modify. */
    private static boolean hasParcels() {
        if (REGISTRY.getAll().isEmpty()) {
            System.out.println("No parcels yet.");
            return false;
        }
        return true;
    }

    private static String twoDecimals(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private static void changeService() {
        if (!hasParcels()) {
            return;
        }
        Parcel parcel = REGISTRY.find(Parcel.normalizeTrackingNumber(readLine("Tracking number: ")));
        if (parcel.isDelivered()) {
            System.out.println("Parcel already delivered!");
            return;
        }
        DeliveryService service = ask(SERVICE_PROMPT, Main::toService);
        parcel.changeService(service);
        System.out.printf(Locale.US, "Delivery service changed! New fee: Php %.2f%n", parcel.getDeliveryFee());
    }

    private static void markDelivered() {
        if (!hasParcels()) {
            return;
        }
        Parcel parcel = REGISTRY.find(Parcel.normalizeTrackingNumber(readLine("Tracking number: ")));
        if (parcel.isDelivered()) {
            System.out.println("Parcel was already delivered.");
            return;
        }
        parcel.markDelivered();
        System.out.println("Parcel delivered successfully!");
    }

    /** Re-prompts until the parser accepts the input. */
    private static <T> T ask(String prompt, Function<String, T> parser) {
        while (true) {
            try {
                return parser.apply(readLine(prompt));
            } catch (ParcelException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static int askInt(String prompt, IntUnaryOperator validator) {
        return ask(prompt, s -> validator.applyAsInt(toInt(s)));
    }

    private static int choice(String prompt, int max, String error) {
        return ask(prompt, s -> {
            int n = toInt(s);
            if (n < 1 || n > max) {
                throw new InvalidInputException(error);
            }
            return n;
        });
    }

    private static DeliveryService toService(String s) {
        int n = toInt(s);
        if (n < 1 || n > SERVICES.size()) {
            throw new InvalidInputException("Invalid delivery service!");
        }
        return SERVICES.get(n - 1);
    }

    private static int toInt(String s) {
        if (!s.matches("\\d{1,9}")) {
            throw new InvalidInputException("Please enter a positive whole number!");
        }
        return Integer.parseInt(s);
    }

    private static double toDouble(String s) {
        if (!s.matches("\\d{1,6}(\\.\\d+)?")) {
            throw new InvalidInputException("Please enter a positive number like 2.5!");
        }
        return Double.parseDouble(s);
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        if (!IN.hasNextLine()) {
            System.out.println();
            quit();
        }
        String line = IN.nextLine().trim();
        if (line.equalsIgnoreCase("exit")) {
            quit();
        }
        return line;
    }

    private static void quit() {
        System.out.println("Goodbye!");
        System.exit(0);
    }
}