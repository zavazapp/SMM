package utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
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

    /**
     * for tickets:
     * ddMMMyyyy + ticket
     * @return - dd.MM.yy
     */
    public static String getFormatedDate(String unformated) {
        String formatedDate = unformated;

        if (unformated.contains("/")) {
            formatedDate = getFormatedFromSlashed(unformated);
        } else if (unformated.contains(" ")) {
            formatedDate = getFormatedFromBlanks(unformated);
        } else if (unformated.contains("ticket")) {
            formatedDate = getFormatedFromTicket(unformated);
        } else if (unformated.contains("-")) {
            formatedDate = getFormatedFromMinusSign(unformated);
        } else if (unformated.contains(".")) {
            formatedDate = unformated; // date is already formated
        } else {
            formatedDate = getFormatedFromNonBlanks(unformated);
        }
        return formatedDate;
    }

    public static String getFormatedDate(LocalDate value) {
        Calendar c = Calendar.getInstance();
        c.set(value.getYear(), value.getMonthValue() - 1, value.getDayOfMonth());
        return getFormatedDate(c.getTimeInMillis());
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

        SimpleDateFormat inputFormat = new SimpleDateFormat("dd MMMMM yyyy", Locale.ENGLISH);
//        SimpleDateFormat inputFormat = new SimpleDateFormat(DateFormatSymbols.getInstance().getLocalPatternChars());

        try {
            Date date = inputFormat.parse(unformated);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yy", Locale.ENGLISH);

            temp = outputFormat.format(date);

        } catch (ParseException ex) {
            Logger.getLogger(DateUtils.class.getName()).log(Level.SEVERE, null, ex);
            return "NA";
        }
        return temp;
    }

    private static String getFormatedFromNonBlanks(String unformated) {
        String temp = unformated;

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);

        try {
            Date date = inputFormat.parse(unformated);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yy", Locale.ENGLISH);

            temp = outputFormat.format(date);

        } catch (ParseException ex) {
            Logger.getLogger(DateUtils.class.getName()).log(Level.SEVERE, null, ex);
            return "NA";
        }
        return temp;
    }

    private static String getFormatedFromMinusSign(String unformated) {
        String temp = unformated;

        SimpleDateFormat inputFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH);

        try {
            Date date = inputFormat.parse(unformated);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yy", Locale.ENGLISH);

            temp = outputFormat.format(date);

        } catch (ParseException ex) {
            Logger.getLogger(DateUtils.class.getName()).log(Level.SEVERE, null, ex);
            return "NA";
        }
        return temp;
    }

    private static String getFormatedFromTicket(String unformated) {
        String temp = unformated.replace("ticket", "");

        SimpleDateFormat inputFormat = new SimpleDateFormat("ddMMMyyyy", Locale.ENGLISH);

        try {
            Date date = inputFormat.parse(temp);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yy", Locale.ENGLISH);

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
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yy", Locale.ENGLISH);
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

    /**
     * 
     * @param date - dd.MM.yy
     * @return 
     */
    public static String getYearFromTicket(String date) {
        if (date == null || date.equals("") || date.equals("NA")) {
            return "NA";
        }
        return getYearFromMt(date);
    }

    public static String getYearFromMt(String dateReceived) {
        if (dateReceived.length() > 9) {
            dateReceived = getFormatedDate(dateReceived);
        }

        SimpleDateFormat inputFormat = new SimpleDateFormat("dd.MM.yy", Locale.ENGLISH);
        Date date;
        Calendar c = Calendar.getInstance();
        try {
            date = inputFormat.parse(dateReceived);
            c.setTime(date);
        } catch (ParseException ex) {
            Logger.getLogger(DateUtils.class.getName()).log(Level.SEVERE, null, ex);
            return "NA";
        }
        return String.valueOf(c.get(Calendar.YEAR));
    }

    public static String getMonthFromMt(String dateReceived) {
        if (dateReceived.length() > 9) {
            dateReceived = getFormatedDate(dateReceived);
        }
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd.MM.yy", Locale.ENGLISH);
        Date date;
        Calendar c = Calendar.getInstance();
        try {
            date = inputFormat.parse(dateReceived);
            c.setTime(date);
        } catch (ParseException ex) {
            Logger.getLogger(DateUtils.class.getName()).log(Level.SEVERE, null, ex);
            return "NA";
        }
        return String.valueOf(c.get(Calendar.MONTH) + 1);
    }

    public static String getMonthFromTicket(String dateReceived) {

        return getMonthFromMt(dateReceived);
    }

}
