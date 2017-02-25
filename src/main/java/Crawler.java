
/**
 * Created by Lucas on 2/24/2017.
 */


import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class Crawler implements Runnable {
    private final DoubleBlockingQueue doubleBlockingQueue;

    public Crawler(DoubleBlockingQueue doubleBlockingQueue) {
        this.doubleBlockingQueue = doubleBlockingQueue;
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            String url = doubleBlockingQueue.getFromOrigin();
            if (Thread.currentThread().isInterrupted())
                break;
            ArrayList<String> newLinks = new ArrayList<String>();
            String formatedName = "";
            Boolean isCharacter = false;
            try {
                Document doc = Jsoup.connect("http://starwars.wikia.com/wiki/" + url).get();
                isCharacter = doc.select("#mw-content-text > aside").hasClass("pi-theme-character");
                if (isCharacter) {
                    formatedName = doc.select("#WikiaPageHeader > div.header-container > div.header-column.header-title > h1").text();
                    Elements links = doc.select("#mw-content-text a");
                    for (Element link : links) {
                        String href = link.attr("href");
                        if (href.contains("/wiki/") && href.charAt(0) == '/')
                            newLinks.add(href.split("\\?")[0]);
                    }
                }
            } catch (Exception e) {
                doubleBlockingQueue.putInOrigin(url);
            }
            if (isCharacter)
                doubleBlockingQueue.putInSolution(formatedName);
            doubleBlockingQueue.putInDestiny(newLinks);
            break;
        }
    }
}
