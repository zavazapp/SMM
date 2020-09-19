package utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * receives date as found on swift messages. For example: 29 July 2000 - and
 * convert it to 29.07.2020
 *
 * @unformated
 * @author Miodrag Spasic
 */
public class DateUtils {

    public static String getFormatedDate(String unformated) {
        String formatedDate = unformated;

        if (unformated.contains("/")) {
            formatedDate = getFormatedFromSlashed(unformated);
        } else if (unformated.contains(" ")) {
            formatedDate = getFormatedFromBlanks(unformated);
        } else {
            formatedDate = getFormatedFromNonBlanks(unformated);
        }
        return formatedDate;
    }

    public static String getFormatedDate(long milis) {
        String formatedDate = getFormatedFromMilis(milis);

        return formatedDate;
    }

    private static String getFormatedFromSlashed(String unformated) {
        return unformated.trim().replace("/", ".");
    }

    private static String getFormatedFromBlanks(String unformated) {
        String temp = unformated;

        SimpleDateFormat inputFormat = new SimpleDateFormat("dd MMM yyyy");

        try {
            Date date = inputFormat.parse(unformated);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yy");

            temp = outputFormat.format(date);

        } catch (ParseException ex) {
            Logger.getLogger(DateUtils.class.getName()).log(Level.SEVERE, null, ex);
            return "NA";
        }
        return temp;
    }

    private static String getFormatedFromNonBlanks(String unformated) {
        String temp = unformated;

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd");

        try {
            Date date = inputFormat.parse(unformated);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yy");

            temp = outputFormat.format(date);

        } catch (ParseException ex) {
            Logger.getLogger(DateUtils.class.getName()).log(Level.SEVERE, null, ex);
            return "NA";
        }
        return temp;
    }

    private static String getFormatedFromMilis(long milis) {
        String temp = "";

        Date date = new Date(milis);
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yy");
        temp = outputFormat.format(date);
        return temp;
    }

    public static boolean isDoday(Date date) {
        Calendar c = Calendar.getInstance();

        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        Date today = c.getTime();

        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        Date userChoice = c.getTime();

        return today.equals(userChoice);
    }
}
