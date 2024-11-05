package web3.api.identity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.HashMap;

public class GetToken {

    private static final String OAUTH_DOMAIN = "https://oauth.codef.io";
    private static final String GET_TOKEN = "/oauth/token";

    public static void main(String[] args) {
        String clientId = "86640213-3b83-461a-97ab-2491d68a2052";
        String clientSecret = "8721d0b3-37ea-4484-8d65-6418a61fd1a1";

        // 토큰 발급
        HashMap<String, String> tokenMap = publishToken(clientId, clientSecret);

        if (tokenMap != null && tokenMap.containsKey("access_token")) {
            // 발급된 액세스 토큰
            String accessToken = tokenMap.get("access_token");
            System.out.println("Access Token: " + accessToken);
        } else {
            System.out.println("Failed to get access token");
        }
    }

    public static String getAccessToken(String clientId, String clientSecret) {
        HashMap<String, String> tokenMap = publishToken(clientId, clientSecret);
        if (tokenMap != null && tokenMap.containsKey("access_token")) {
            return tokenMap.get("access_token");
        } else {
            throw new RuntimeException("Failed to get access token");
        }
    }

    public static HashMap<String, String> publishToken(String clientId, String clientSecret) {
        BufferedReader br = null;
        try {
            URL url = new URL(OAUTH_DOMAIN + GET_TOKEN);
            String params = "grant_type=client_credentials&scope=read";

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String auth = clientId + ":" + clientSecret;
            byte[] authEncBytes = Base64.getEncoder().encode(auth.getBytes());
            String authStringEnc = new String(authEncBytes);
            String authHeader = "Basic " + authStringEnc;
            con.setRequestProperty("Authorization", authHeader);
            con.setDoInput(true);
            con.setDoOutput(true);

            OutputStream os = con.getOutputStream();
            os.write(params.getBytes());
            os.flush();
            os.close();

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                return null;
            }

            String inputLine;
            StringBuffer responseStr = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                responseStr.append(inputLine);
            }
            br.close();

            ObjectMapper mapper = new ObjectMapper();
            HashMap<String, String> tokenMap = mapper.readValue(URLDecoder.decode(responseStr.toString(), "UTF-8"),
                    new TypeReference<HashMap<String, String>>() {
                    });

            return tokenMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    // Ignored
                }
            }
        }
    }
}