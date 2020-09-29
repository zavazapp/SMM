package utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import models.MTMessages.MTEntity;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * Class that handles renaming and archiving messages
 *
 * @author Miodrag Spasic
 */
public class FileUtils {

    public FileUtils() {
    }

    public MTEntity readMt(File file) throws IOException {
        MTEntity mt = null;

        //Workaround for releasing resources in try block for document load - PDDocument.load(file)
        //TODO - test reducing sleep
//        try {
//            Thread.sleep(10);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
//        }
        try (PDDocument doc = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc).trim();
            doc.close();

            String msgType = getMsgType(text);
            String sender = getBank(text);
            String bene = getBene58A(text).equals("NA") ? getBene57A(text) : getBene58A(text);
            String field20 = getField20(text);
            String field21 = getField21(text);
            String dateReceived = getDateReceived(text);
            String valueDate = getDate(text, msgType);
            String currency = getCurrency(text, msgType);
            String amount = getAmount(text);
            boolean selected = false;
            if (bene.length() > 14) {
                bene = bene.substring(0, 12);
            }

            mt = new MTEntity(
                    msgType, sender,
                    (msgType.contains("FIN 950") || msgType.contains("FIN 940")) ? msgType : bene,
                    field20,
                    (msgType.contains("FIN 950") || msgType.contains("FIN 940")) ? getReceiver(text) : field21,
                    dateReceived,
                    (msgType.contains("FIN 195")) ? "NA" : valueDate,
                    currency,
                    (msgType.contains("FIN 950") || msgType.contains("FIN 940")) ? getSequence(text) : amount,
                    text,
                    file,
                    isRenamed(file),
                    selected);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return mt;
    }

    /**
     * Separate method for renaming account statements since those msgs have
     * different "F" field in its body. All other swift msgs are renamed usin
     * renameMt(MTEntity mt) method.
     *
     * @param resultFolder - folder to save renamed file
     * @param file - file to be renamed
     * @return - File with a new name
     */
    public File renameAccountStatement(String resultFolder, File file) {
        File newFile = null;
        String fileName = "";
        try (PDDocument doc = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc).trim();

            String msgType = getMsgType(text);
            String bank = getBank(text);
            String receiver = getReceiver(text);
            String currency = getCurrency(text, msgType);
            String date = getDate(text, msgType);
            String statementNumber = getSequence(text);

            fileName = bank.concat("_").concat(receiver).concat("_").concat(currency).concat("_").concat(date).concat("_").concat(statementNumber);
            doc.close();

            if (bank.isEmpty() || currency.isEmpty() || date.isEmpty() || statementNumber.isEmpty()) {
                System.out.println("File name " + fileName + " is not correct.");
            } else {
                //createResultFolder(resultFolder + File.separator + date);
                if (fileName.contains("/")) {
                    fileName.replace("/", "-");
                }
                newFile = new File(resultFolder + File.separator + fileName + ".pdf");
                if (newFile.exists()) {
                    System.out.println("Destination file already exists !");
                    return null;
                }

                if (!file.renameTo(newFile)) {
                    System.out.println("Can not rename file.");
                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return newFile;
    }

    public MTEntity renameMt(MTEntity mt) throws IOException {

        String msgType = getMsgType(mt.getText());
        String fileName;

        if (msgType.contains("99") || msgType.contains("19")) {
            fileName = mt.getSender().concat("_").
                    concat(mt.getField20()).concat("_").
                    concat(mt.getField21()).concat("_").
                    concat(mt.getDateReceived().substring(0, 8)).replace("/", ".");
        } else {
            fileName = mt.getSender().concat("_").
                    concat(mt.getBene()).concat("_").
                    concat(mt.getField20()).concat("_").
                    concat(mt.getValueDate()).concat("_").
                    concat(mt.getCurrency()).concat("_").
                    concat(mt.getAmount());
        }

        if (fileName.contains("/")) {
            fileName = fileName.replace("/", "-");
        }

        File newFile = new File(mt.getFile().getParent() + File.separator + fileName + ".pdf");
        if (newFile.exists() && !mt.isRenamed()) {
            newFile = getUniqueFileName(newFile);
            System.out.println("Destination file already exists !\nTrying to create unique name...");
            if (newFile == null) {
                System.out.println("Can not rename file.");
                return null;
            }
        }

        if (mt.getFile().renameTo(newFile)) {
            mt.setFile(newFile);
            return mt;
        }

        return null;
    }

    /**
     *
     * @param mt
     * @param clientName - entered by user
     * @return
     */
    public MTEntity renamePension(MTEntity mt, String clientName) {

        String fileName;
        fileName = clientName;

        if (fileName.contains("/")) {
            fileName.replace("/", "-");
        }

        File newFile = new File(mt.getFile().getParent() + File.separator + fileName + ".pdf");
        if (newFile.exists() && !mt.isRenamed()) {
            newFile = getUniqueFileName(newFile);
            System.out.println("Destination file already exists !\nTrying to create unique name...");
            if (newFile == null) {
                System.out.println("Can not rename file.");
                return null;
            }
        }

        if (mt.getFile().renameTo(newFile)) {
            mt.setFile(newFile);
        }

        return mt;
    }

    /**
     *
     * @param resultFolder - folder to store swift message
     * @param file - file to be stored
     * @param subfolder
     * @return 1 if file is successfully archived (used to count messages)
     */
    public int archiveMt(String resultFolder, File file, String subfolder) {
        if (subfolder.equals("NA")) {
            JOptionPane.showMessageDialog(null, "Can not determin date!", "Action aborted", JOptionPane.ERROR_MESSAGE);
            return 0;
        }
        File archiveFolder = new File(resultFolder + File.separator + subfolder);

        if (!archiveFolder.exists()) {
            archiveFolder.mkdir();
        }

        int counter = 0;

        String newFileName = file.getName();
        if (newFileName.contains("/")) {
            newFileName.replace("/", "-");
        }
        File newFile = new File(archiveFolder + File.separator + newFileName);

        if (newFile.exists()) {
            newFile = getUniqueFileName(newFile);
            System.out.println("Destination file already exists !\nTrying to create unique name...");
            if (newFile == null) {
                System.out.println("Can not rename file.");
                return 0;
            }
        }

        try {
            if (file.renameTo(newFile)) {
                counter++;
            }
        } catch (Exception e) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.ALL, null, e);
        }

        return counter;
    }

//    public int archiveTicket(String resultFolder, File file, String subfolder) {
//        if (subfolder.equals("NA")) {
//            JOptionPane.showMessageDialog(null, "Can not determin date!", "Action aborted", JOptionPane.ERROR_MESSAGE);
//            return 0;
//        }
//        File archiveFolder = new File(resultFolder + File.separator + subfolder);
//
//        if (!archiveFolder.exists()) {
//            archiveFolder.mkdir();
//        }
//
//        int counter = 0;
//
//        String newFileName = file.getName();
//        if (newFileName.contains("/")) {
//            newFileName.replace("/", "-");
//        }
//        File newFile = new File(archiveFolder + File.separator + newFileName);
//
//        if (newFile.exists()) {
//            newFile = getUniqueFileName(newFile);
//            System.out.println("Destination file already exists !\nTrying to create unique name...");
//            if (newFile == null) {
//                System.out.println("Can not rename file.");
//                return 0;
//            }
//        }
//
//        try {
//            if (file.renameTo(newFile)) {
//                counter++;
//            }
//        } catch (Exception e) {
//            Logger.getLogger(FileUtils.class.getName()).log(Level.ALL, null, e);
//        }
//
//        return counter;
//    }
    /**
     * Checks if file is renamed to prevent user to archive not renamed files
     *
     * @param file
     * @return
     */
    private boolean isRenamed(File file) {
        return file.getName().split("_").length >= 3;
    }

    //Methods for extracting required text from the swift message
    //TODO - maybe separate class and better handling of posible exceptions
    private String getDate(String text, String type) {
        int tempStartIndex = 0;
        int tempEnd = 0;
        int startIndex = 0;
        String textString = null;
        String tempText = null;
        int endIndex = 0;

        if (type == null || type.equals("FIN 195") || type.equals("FIN 199")) {
            return "NA";
        }

        if (type.equals("FIN 300") || type.equals("FIN 320")) {
            try {
                tempStartIndex = text.lastIndexOf("30V: Value Date");

                startIndex = text.indexOf("\n", tempStartIndex) + 1;
                tempEnd = text.indexOf("\n", startIndex);
                tempText = text.substring(startIndex, tempEnd).trim();

                textString = tempText.substring(0, 8);
                endIndex = textString.indexOf("\n");

                return DateUtils.getFormatedDate(textString.trim());
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return "NA";
            }

        } else {
            try {
                tempStartIndex = text.lastIndexOf("Date");
                startIndex = text.indexOf(":", tempStartIndex) + 1;
                textString = text.substring(startIndex, startIndex + 20);
                endIndex = textString.indexOf("\n");
                if (endIndex == -1) {
                    return "NA";
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
                return "NA";
            }
        }

        String temp = text.substring(startIndex, startIndex + endIndex);

        return DateUtils.getFormatedDate(temp.trim());
    }

    private String getDateReceived(String text) {
        try {
            int startIndex = 0;
            String textString = text.substring(startIndex, startIndex + 17);
            return DateUtils.getFormatedDate(textString.trim());

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "NA";
        }
    }

    private String getCurrency(String text, String type) {
        int tempStartIndex = text.lastIndexOf("Currency");
        if (tempStartIndex == -1) {
            return "NA";
        }

        if (type == null || type.equals("FIN 195") || type.equals("FIN 199")) {
            return "NA";
        }

        int endIndex = text.indexOf("\n", tempStartIndex);

        String temp = text.substring(tempStartIndex, endIndex);
        int indexOfBracket = temp.indexOf("(");
        if (indexOfBracket == -1) {
            return "NA";
        }
        return temp.substring(indexOfBracket - 4, indexOfBracket - 1).trim();
    }

    private String getSequence(String text) {
        try {
            int tempStartIndex = text.indexOf("28C:");
            int startIndex = text.indexOf("\n", tempStartIndex) + 1;
            String textString = text.substring(startIndex, startIndex + 50);
            int endIndex = textString.indexOf("\n");

            String temp = text.substring(startIndex, startIndex + endIndex);
            return temp.replace("/", "_").trim();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "NA";
    }

    private String getBank(String text) {
        int tempStartIndex = text.indexOf("Sender");
        int endIndex = text.indexOf("\n", tempStartIndex);
        return text.substring(endIndex - 12, endIndex).replace("XXX", "").trim();
    }

    private String getReceiver(String text) {
        int tempStartIndex = text.indexOf("Receiver");
        int endIndex = text.indexOf("\n", tempStartIndex);
        return text.substring(endIndex - 12, endIndex).replace("XXX", "").trim();
    }

    private String getBene58A(String text) {
        try {
            int tempStartIndex = text.indexOf("58A:");
            int startIndex = text.indexOf("\n", tempStartIndex) + 1;
            String textString = text.substring(startIndex, startIndex + 50);
            int endIndex = textString.indexOf("\n");
            String temp = "NA";
            String returnValue = "NA";
            if (endIndex == -1) {
                returnValue = getBene58D(text);
                if (returnValue.startsWith("/")) {
                    returnValue = getBene58ANextLine(text);
                }
                return returnValue.replace("XXX", "").trim();
            } else {
                temp = text.substring(startIndex, startIndex + endIndex);
                if (temp.contains("/") || temp.startsWith("_")) {
                    temp = getBene58ANextLine(text);
                }
                return temp.replace("/", "_").replace("XXX", "").trim();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, e);
        }
        return "NA";
    }

    private String getBene58ANextLine(String text) {
        try {
            int tempStartIndex = text.indexOf("58A:");
            int temp2StartIndex = text.indexOf("\n", tempStartIndex) + 1;
            int startIndex = text.indexOf("\n", temp2StartIndex) + 1;
            String textString = text.substring(startIndex, startIndex + 50);
            int endIndex = textString.indexOf("\n");
            String temp;
            if (endIndex == -1) {
                return getBene58D(text).replace("XXX", "").trim();
            } else {
                temp = text.substring(startIndex, startIndex + endIndex);
                return temp.replace("/", "_").replace("XXX", "").trim();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, e);
        }
        return "NA";
    }

    private String getBene57A(String text) {
        try {
            int tempStartIndex = text.indexOf("57A:");
            int startIndex = text.indexOf("\n", tempStartIndex) + 1;
            String textString = text.substring(startIndex, startIndex + 50);
            int endIndex = textString.indexOf("\n");
            String temp = "NA";
            String returnValue = "NA";
            if (endIndex == -1) {
                returnValue = getBene58D(text);
                if (returnValue.startsWith("/")) {
                    returnValue = getBene57ANextLine(text);
                }
                return returnValue.replace("XXX", "").trim();
            } else {
                temp = text.substring(startIndex, startIndex + endIndex);
                if (temp.contains("/") || temp.startsWith("_")) {
                    temp = getBene57ANextLine(text);
                }
                return temp.replace("/", "_").replace("XXX", "").trim();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, e);
        }
        return "NA";
    }

    private String getBene57ANextLine(String text) {
        try {
            int tempStartIndex = text.indexOf("57A:");
            int temp2StartIndex = text.indexOf("\n", tempStartIndex) + 1;
            int startIndex = text.indexOf("\n", temp2StartIndex) + 1;
            String textString = text.substring(startIndex, startIndex + 50);
            int endIndex = textString.indexOf("\n");
            String temp;
            if (endIndex == -1) {
                return getBene58D(text).replace("XXX", "").trim();
            } else {
                temp = text.substring(startIndex, startIndex + endIndex);
                return temp.replace("/", "_").replace("XXX", "").trim();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, e);
        }
        return "NA";
    }

    private String getBene58D(String text) {
        try {
            int tempStartIndex = text.indexOf("58D:");
            int temp2StartIndex = text.indexOf("\n", tempStartIndex) + 1;
            int startIndex = text.indexOf("\n", temp2StartIndex) + 1;
            String textString = text.substring(startIndex, startIndex + 50);
            int endIndex = textString.indexOf("\n");

            if (endIndex == -1) {
                return "NA";
            }
            String temp = text.substring(startIndex, startIndex + endIndex);
            return temp.replace("/", "_").trim();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, e);
        }
        return "NA";
    }

    private String getField20(String text) {
        try {
            int tempStartIndex = text.indexOf("20: ");
            if (tempStartIndex == -1) {
                return "NA";
            }
            int startIndex = text.indexOf("\n", tempStartIndex) + 1;
            String textString = text.substring(startIndex, startIndex + 50);
            int endIndex = textString.indexOf("\n");

            String temp = text.substring(startIndex, startIndex + endIndex);
            return temp.replace("/", "_").trim();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "NA";
    }

    private String getField21(String text) {
        try {
            int tempStartIndex = text.indexOf("21: ");
            if (tempStartIndex == -1) {
                return "NA";
            }
            int startIndex = text.indexOf("\n", tempStartIndex) + 1;
            String textString = text.substring(startIndex, startIndex + 50);
            int endIndex = textString.indexOf("\n");

            String temp = text.substring(startIndex, startIndex + endIndex);
            return temp.replace("/", "_").trim();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "NA";
    }

    private String getAmount(String text) {
        int tempStartIndex = text.indexOf("Amount  ");
        if (tempStartIndex == -1) {
            return "NA";
        }
        int endIndex = text.indexOf("\n", tempStartIndex);
        String temp = text.substring(tempStartIndex, endIndex);

        int indexOfFirstHash = temp.indexOf("#");
        int indexOfLastHash = temp.lastIndexOf("#");

        return temp.substring(indexOfFirstHash + 1, indexOfLastHash - 1).trim();
    }

    public int findBlank(String text, int startIndex) {
        for (int i = 0; i < 100; i++) {
            if (Character.isSpaceChar(text.charAt(i))) {
                startIndex += i;
                break;
            }
        }
        return startIndex + 1;
    }

    public int findNonBlank(String text, int startIndex) {
        for (int i = 0; i < 3000; i++) {
            Character c = text.charAt(i);
            Character.UnicodeBlock block = Character.UnicodeBlock.of(c);

            if (block != null && block != Character.UnicodeBlock.SPECIALS) {
                startIndex += i;
                break;
            }
        }
        return startIndex;
    }

    private String getMsgType(String text) {
        return text.subSequence(text.indexOf("FIN "), text.indexOf("FIN ") + 7).toString();
    }
    //END of - Methods for extracting required text from the swift message

    /**
     * Makes sure that if file with the same name already exists, new file with
     * a unique name is created Used if File already exists exception is
     * catched.
     *
     * @param oldFile
     * @return - new, unique file adding "_" + file version at the end of the
     * file
     */
    private File getUniqueFileName(File oldFile) {
        String path = oldFile.getPath();
        File newFile = null;

        int ver = 0;
        while (oldFile.exists()) {
            ver++;
            newFile = new File(path.substring(0, path.lastIndexOf(".")).concat("_").concat(String.valueOf(ver)).concat(".pdf"));
            oldFile = newFile;
        }

        return newFile;
    }

    public String readPdf(File file) throws IOException {
        String text = "Can not read file !";
        try (PDDocument doc = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            text = stripper.getText(doc).trim();
            doc.close();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return text;
    }

}
