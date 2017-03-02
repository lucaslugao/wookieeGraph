
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

/*
def getLinks(pageName):
    global visited
    plcontinue = ''
    linkNames = []

    #start = time.clock()
    while(True):
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
        for x in linkNames:
            visited.add(x)
        if('query-continue' in r.keys()):
            plcontinue = '&plcontinue='+ r['query-continue']['links']['plcontinue']
        else:
            break
    #print("getLinks took %f seconds" % (time.clock()-start))
    return linkNames

 */
public class FastCrawler implements Runnable {
    private final Set<String> characterNames;
    private final DoubleBlockingQueue doubleBlockingQueue;

    public FastCrawler(DoubleBlockingQueue doubleBlockingQueue, Set<String> characterNames) {
        this.doubleBlockingQueue = doubleBlockingQueue;
        this.characterNames = characterNames;
    }

    public void run() {
        while (true) {
            String url = doubleBlockingQueue.getFromOrigin();
            if (url == null)
                break;
            ArrayList<String> newLinks = new ArrayList<>();
            String plcontinue = "";

            int maxRetry = 10;
            while (maxRetry > 0) {
                boolean restartParse = false;
                try {
                    String getURL = "http://starwars.wikia.com/api.php?action=query&format=json&prop=links" + plcontinue + "&pllimit=500&titles=" + URLEncoder.encode(url, "UTF-8");
                    //System.out.println(getURL);
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
                doubleBlockingQueue.putInOrigin(url);
            } else {
//                System.out.printf("Found %d links!\n", newLinks.size());
                doubleBlockingQueue.putInSolution(url);
                doubleBlockingQueue.putInDestiny(newLinks);
            }
            //String url = doubleBlockingQueue.getFromOrigin();
            //doubleBlockingQueue.putInOrigin(url);
            //doubleBlockingQueue.putInDestiny(null);
        }
    }
}
