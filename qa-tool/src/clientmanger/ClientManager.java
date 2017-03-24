package clientmanger;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class ClientManager {
    private final static String artifactString = "artifact";

    public ClientManager() {
    }

    public static ArrayList<String> getImagesFromArtifacts(String uri) {
        String content = "";
        String baseUrl = uri.substring(0, uri.indexOf(ClientManager.artifactString));
        try {
            content = readUrl(baseUrl + "api/json");
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject main = new JSONObject(content);
        ArrayList<String> images = new ArrayList<>();

        main.getJSONArray(ClientManager.artifactString + "s").forEach(
                j -> {
                    String path = ((JSONObject) j).get("relativePath").toString();
                    if (path.endsWith(".png") && path.contains("_actual") && !path.contains("result"))
                        images.add(baseUrl + ClientManager.artifactString + File.separator + path);
                });


        return images;
    }

    private static String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }
}
