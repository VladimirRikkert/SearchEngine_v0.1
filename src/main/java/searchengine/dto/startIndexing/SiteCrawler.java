package searchengine.dto.startIndexing;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.flogger.Flogger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.config.Task;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.logging.Logger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SiteCrawler implements Runnable {

    private String siteUrl;
    private SiteRepository siteRepository;
    private PageRepository pageRepository;
//    private static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
      ForkJoinPool forkJoinPool;
    @Override
    public void run() {
        try {
            searchAllPages(siteUrl);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public void searchAllPages(String siteUrl) throws InterruptedException {
        synchronized (pageRepository) {
            Connection connection = Jsoup.connect(siteUrl).userAgent("Mozilla/5.0");
            try {
                Document document = connection.get();
                Elements elements = document.select("a[href]");
                for (Element element : elements) {
                    String link = element.absUrl("href");
                    if (pageRepository.searchPageInDB(link) == null && link.startsWith("http")) {
                        PageEntity page = new PageEntity();
                        String sUrl = link.substring(0, link.indexOf("/", 8));
                        SiteEntity site = siteRepository.findIdByUrl(sUrl);
                        if (site != null) {
                            page.setSite(site);
                            page.setPath(link);
                            page.setCode(connection.response().statusCode());
                            page.setContent("Test");
                            pageRepository.savePage(page.getSite().getId(), page.getPath(), page.getCode(), page.getContent());
                            System.out.println("Page: " + page.getPath() + " is saved!");
                            if (link.startsWith(sUrl) && !link.endsWith(".pdf")) {
                                SiteCrawler siteCrawler = new SiteCrawler(link, siteRepository, pageRepository, forkJoinPool);
//                                executorService.submit(siteCrawler);
                                forkJoinPool.execute(siteCrawler);
                            }

                        }
                    }
                }
            } catch (Exception exception) {

            }
        }

//    public void searchAllPages(String siteUrl) throws InterruptedException {
//        Connection connection = Jsoup.connect(siteUrl).userAgent("Mozilla/5.0");
//        Thread.sleep(150);
//        try {
//            Document document = connection.get();
//            Elements elements = document.select("a");
//            ArrayList<String> pages = new ArrayList<>();
//            elements.forEach(element -> pages.add(element.absUrl("href")));
//            if (pageRepository.searchPageInDB(siteUrl) == null) {
//                PageEntity page = new PageEntity();
//                String sUrl = document.baseUri() + "/";
//                sUrl = sUrl.substring(0, sUrl.indexOf("/", 8));
//                SiteEntity site = siteRepository.findIdByUrl(sUrl);
//                if (site != null) {
//                    page.setSite(site);
//                    page.setPath(siteUrl);
//                    page.setCode(connection.response().statusCode());
//                    page.setContent("Test");
//                    pageRepository.savePage(page.getSite().getId(), page.getPath(), page.getCode(), page.getContent());
//                    System.out.println("Page: " + page.getPath() + " is saved!");
//                    for (String s : pages) {
//                        if (!s.equals(siteUrl) && s.startsWith(sUrl)) {
//                            SiteCrawler siteCrawler = new SiteCrawler(s, siteRepository, pageRepository);
//                            executorService.submit(siteCrawler);
//
//                        }
//                    }
//                } else {
//                    System.err.println("ОШИБКА! Другой сайт: " + siteUrl);
//                }
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
    }
}
