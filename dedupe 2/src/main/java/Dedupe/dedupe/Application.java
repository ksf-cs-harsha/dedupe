package Dedupe.dedupe;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Application {
    private static final String CSV_FILE_PATH = "/Users/harshavardhan/Downloads/UNI dedupe analysis.csv";
    private static final String API_ENDPOINT = "https://vision.qa2.creditsaison.xyz/api/v1/partner/applicant/check";
    private static final String AUTHORIZATION_HEADER = "Basic QGRNMU46UEAkJFcwckQ=";
    private static final String USERNAME_HEADER = "partnerapikey";
    private static final String COOKIE_HEADER = "JSESSIONID=13F0590A81AC7CF9D3AEB727348DC099";
    private static final Pattern PAN_PATTERN = Pattern.compile("[A-Z]{5}[0-9]{4}[A-Z]");
    private static final int BATCH_SIZE = 10;

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
                    batchBuilder.setLength(0);
                    count = 0;
                }
            }

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
        try {
            URL url = new URL(API_ENDPOINT);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", AUTHORIZATION_HEADER);
            connection.setRequestProperty("username", USERNAME_HEADER);
            connection.setRequestProperty("Cookie", COOKIE_HEADER);
            connection.setDoOutput(true);

            String payload = "{\"loanProduct\": \"UNC\", \"kycs\": [{\"kycType\": \"panCard\", \"kycValue\": \"" + pan + "\"}]}";
            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.writeBytes(payload);
                wr.flush();
            }

            int responseCode = connection.getResponseCode();
            StringBuilder response = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }

            connection.disconnect();

            return responseCode == 200 ? response.toString() : "";
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
