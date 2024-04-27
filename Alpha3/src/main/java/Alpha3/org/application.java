package Alpha3.org;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class application {
    private static final String CSV_FILE_PATH = "/Users/harshavardhan/Downloads/UNI dedupe analysis.csv";
    private static final String API_ENDPOINT = "https://vision.qa2.creditsaison.xyz/api/v1/partner/applicant/check";
    private static final String AUTHORIZATION_HEADER = "Basic QGRNMU46UEAkJFcwckQ=";
    private static final String USERNAME_HEADER = "partnerapikey";
    private static final String COOKIE_HEADER = "JSESSIONID=13F0590A81AC7CF9D3AEB727348DC099";
    private static final Pattern PAN_PATTERN = Pattern.compile("[A-Z]{5}[0-9]{4}[A-Z]");
    private static final int BATCH_SIZE = 10; // Adjust batch size as needed

    public static void main(String[] args) {
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
            String line;
            int count = 0;
            StringBuilder batchBuilder = new StringBuilder();
            while ((line = br.readLine()) != null) {
                batchBuilder.append(line).append("\n");
                count++;
                if (count == BATCH_SIZE) {
                    processBatch(batchBuilder.toString());
                    batchBuilder.setLength(0); // Clear the batch buffer
                    count = 0; // Reset count
                }
            }
            // Process any remaining lines
            if (batchBuilder.length() > 0) {
                processBatch(batchBuilder.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processBatch(String batch) {
        Matcher matcher = PAN_PATTERN.matcher(batch);
        while (matcher.find()) {
            String pan = matcher.group();
            String jsonResponse = sendRequest(pan);
            String status = (jsonResponse != null && !jsonResponse.isEmpty()) ? "success" : "failure";
            System.out.println(pan + "," + status + "," + jsonResponse);
        }
    }

    private static String sendRequest(String pan) {
        return null;
    }
}
