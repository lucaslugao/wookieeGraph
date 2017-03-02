import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jdk.nashorn.internal.parser.JSONParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.util.EntityUtils;

import javax.swing.plaf.synth.SynthTextAreaUI;
import java.io.IOException;
import java.io.ObjectInput;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Lucas on 3/1/2017.
 */
public class jsonTest {
    public static void main(String[] args) {
        /**
         r = requests.get('http://starwars.wikia.com/api.php?action=query&format=json&prop=links'+plcontinue+'&pllimit=500&titles=' + quote(pageName.strip())).json()
         linksObj = list(r['query']['pages'].values())[0]['links']
         linksObj = [x['title'].replace('/Canon','').strip() for x in linksObj]
         linkNames = linkNames + [x for x in linksObj if
         'Template:Character' not in x and
         'File:' not in x and
         'Wookieepedia:' not in x and
         'Category:' not in x and
         '/Legends' not in x and
         '(episode)' not in x and
         x not in visited]
         */
        Set<String> characterNames = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
        try {
            for (String line : Files.readAllLines(Paths.get("charNames.csv"), StandardCharsets.UTF_8)) {
                characterNames.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            HttpResponse res = Request.Get("http://starwars.wikia.com/api.php?action=query&format=json&prop=links&pllimit=50&titles=Yoda")
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
                            System.out.println(title);
                    }
                    if(json.has("query-continue"))
                        System.out.println(json.get("query-continue").get("links").get("plcontinue").asText());
                    else
                        System.out.println("FINI");
                } catch (IOException e) {
                    System.out.println("Json error!");
                }
            }
        } catch (IOException e) {
            System.out.println("Connection error!");
        }

//
//        RequestConfig globalConfig = RequestConfig.custom()
//                .setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();
//        HttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(globalConfig).build();
//        Unirest.setHttpClient(httpclient);
//        try {
//            HttpResponse<JsonNode> jsonResponse = Unirest.get("http://starwars.wikia.com/api.php?action=query&format=json&prop=links{plcontinue}&pllimit=500&titles={titles}")
//                    .routeParam("titles", "Yoda").routeParam("plcontinue", "").asJson();
//
//            JSONObject pages = jsonResponse.getBody().getObject().getJSONObject("query").getJSONObject("pages");
//            JSONArray links = pages.getJSONObject(pages.names().getString(0)).getJSONArray("links");
//
//            for(Object link : links){
//                String title = ((JSONObject)link).getString("title");
//                title = title.replace("/Canon","").trim();
//                if(characterNames.contains(title))
//                    System.out.println(title);
//            }
//        } catch (UnirestException e) {
//            System.out.println(jsonResponse.getStatus());
//            e.printStackTrace();
//        }
    }
}
