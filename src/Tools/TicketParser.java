package Tools;

import java.util.HashMap;
import java.util.Scanner;

/**
 *
 * @author Korisnik 
 * Scanner should return HashMap<String, String>: text, ticketNumber,
 * dealType, counterParty, dealDetails, nearAmount, farAmount, nearDate, farDate
 */
public class TicketParser {

    static String line;
    static String dealType;
    static HashMap<String, String> ticketFields = new HashMap<>();

    public static HashMap<String, String> scan(String text) {
        ticketFields.clear();
        try (Scanner scanner = new Scanner(text)) {
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                if (line.startsWith("Transactions ID:")) {
                    ticketFields.put("ticketNumber", line.split("No:")[1].trim());
                }
                if (line.startsWith("Deal Type")) {
                    line = scanner.nextLine();
                    dealType = line.split(" ")[0].trim();
                    ticketFields.put("dealType", dealType);
                    ticketFields.put("dealDetails", line.split(line.split(" ")[0].trim())[1].trim());
                    line = scanner.nextLine();
                    line = scanner.nextLine();
                    ticketFields.put("amount1", line.split(" ")[dealType.equals("DEPOSIT") ? 0 : 1].trim());
                    ticketFields.put("amount2", line.split(" ")[dealType.equals("DEPOSIT") ? 1 : 3].trim());
                    line = scanner.nextLine();
                    line = scanner.nextLine();
                    ticketFields.put("date1", line.split(" ")[1].trim());
                    ticketFields.put("date2", line.split(" ")[3].trim());
                }
                if (line.contains("Counterparty Code")) {
                    line = scanner.nextLine();
                    ticketFields.put("counterParty", line.replace("Counterparty", "").trim());
                }
            }
        }

        return ticketFields;
    }
}
