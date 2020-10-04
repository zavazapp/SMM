package models;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;
import utils.DateUtils;
import utils.FileUtils;

/**
 *
 * @author Korisnik
 */
public class TicketEntity {

    private final File file;
    private final String text;
    private final String formatedDate;

    public TicketEntity(File file) throws IOException {
        this.file = file;
        this.text = new FileUtils().readPdf(file);
        this.formatedDate = getFormatedDateHere();
    }


    public File getFile() {
        return file;
    }

    public String getText() {
        return text;
    }

    private String getFormatedDateHere() {
        Scanner scanner = new Scanner(text);
        String line;
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            if (Pattern.matches("[A-Z]{3}\\s[\\d]{2}[A-Z]{3}[\\d]{4}[\\s][A-Z]{3}\\s[\\d]{2}[A-Z]{3}[\\d]{4}[\\s][A-Z]{2}", line.trim())) {
                return DateUtils.getFormatedDate(line.trim().split(" ")[3].concat("ticket"));
            }else if (Pattern.matches("[A-Z]{3}\\s[\\d]{2}[A-Z]{3}[\\d]{4}[\\s][A-Z]{3}\\s[\\d]{2}[A-Z]{3}[\\d]{4}", line.trim())) {
                return DateUtils.getFormatedDate(line.trim().split(" ")[3].concat("ticket"));
            }
        }
        return null;
    }

    public String getFormatedDate() {
        return formatedDate;
    }
    
    
}
