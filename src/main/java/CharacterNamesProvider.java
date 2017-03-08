import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Lucas on 3/2/2017.
 */
public class CharacterNamesProvider {
    public static Set<String> fromFile(String charNamesPath) {
        Set<String> names = new HashSet<>();
        try {
            for (String line : Files.readAllLines(Paths.get(charNamesPath), StandardCharsets.UTF_8)) {
                names.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("charNames path not found, please use the SlowManager.");
        }
        return names;
    }

    public static Set<String> fromWiki() {
        System.out.println("Downloading all character names from http://starwars.wikia.com/");
        Set<String> names = new HashSet<>();

        String eicontinue = "";
        int stepSize = 500;

        int maxRetry = 10;
        while (maxRetry > 0) {
            boolean restartParse = false;
            try {
                String getURL = "http://starwars.wikia.com/api.php?format=json&action=query&list=embeddedin" + eicontinue + "&eititle=Template:Character&eilimit=" + stepSize;
                HttpResponse res = Request.Get(getURL)
                        .connectTimeout(1000)
                        .socketTimeout(1000)
                        .execute().returnResponse();
                if (res.getStatusLine().getStatusCode() == 200) {
                    String jsonString = EntityUtils.toString(res.getEntity());
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode json = mapper.readTree(jsonString);
                        ArrayNode links = (ArrayNode) json.get("query").get("embeddedin");
                        for (JsonNode link : links) {
                            String name = link.get("title").asText();
                            name = name.replace("/Canon", "").trim();
                            name = name.replace("/Legends", "").trim();

                            if (!name.contains(":"))
                                names.add(name);
                        }
                        if (json.has("query-continue"))
                            eicontinue = "&eicontinue=" + URLEncoder.encode(json.get("query-continue").get("embeddedin").get("eicontinue").asText(), "UTF-8");
                        else
                            break;
                    } catch (IOException e) {
                        maxRetry--;
                        restartParse = true;
                    }
                } else {
                    maxRetry--;
                    restartParse = true;
                }
            } catch (IOException e) {
                maxRetry--;
                restartParse = true;
            }
            if (restartParse) {
                System.out.println("Connection error! Retrying " + Integer.toString(maxRetry) + " more times.");
            }
            System.out.printf("%.2f%%\n", 100.0 * names.size() / 28775);
        }
        System.out.printf("%d character names downloaded.\n", names.size());
        return names;
    }
}
