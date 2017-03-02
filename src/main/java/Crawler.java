
/**
 * Created by Lucas on 2/24/2017.
 */


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Set;

public class Crawler implements Runnable {
    private final Set<String> characterNames;
    private final LinkProvider linkProvider;

    public Crawler(LinkProvider linkProvider, Set<String> characterNames) {
        this.linkProvider = linkProvider;
        this.characterNames = characterNames;
    }

    public void run() {
        while (true) {
            String url = linkProvider.getFromOrigin();
            if (url == null)
                break;
            else {
                ArrayList<String> newLinks = new ArrayList<>();

                String plcontinue = "";
                int maxRetry = 10;
                while (maxRetry > 0) {
                    boolean restartParse = false;
                    try {
                        String getURL = "http://starwars.wikia.com/api.php?action=query&format=json&prop=links" + plcontinue + "&pllimit=500&titles=" + URLEncoder.encode(url, "UTF-8");
                        HttpResponse res = Request.Get(getURL)
                                .connectTimeout(1000)
                                .socketTimeout(1000)
                                .execute().returnResponse();
                        if (res.getStatusLine().getStatusCode() == 200) {
                            String jsonString = EntityUtils.toString(res.getEntity());
                            try {
                                ObjectMapper mapper = new ObjectMapper();
                                JsonNode json = mapper.readTree(jsonString);
                                ArrayNode links = (ArrayNode) json.get("query").get("pages").fields().next().getValue().get("links");
                                for (JsonNode link : links) {
                                    String title = link.get("title").asText();
                                    title = title.replace("/Canon", "").trim();
                                    if (characterNames.contains(title))
                                        newLinks.add(title);
                                }
                                if (json.has("query-continue"))
                                    plcontinue = "&plcontinue=" + URLEncoder.encode(json.get("query-continue").get("links").get("plcontinue").asText(), "UTF-8");
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
                        plcontinue = "";
                        newLinks.clear();
                    }
                }
                if (maxRetry == 0) {
                    linkProvider.putInOrigin(url);
                } else {
                    linkProvider.putInDestiny(newLinks);
                }
            }
        }
    }
}
