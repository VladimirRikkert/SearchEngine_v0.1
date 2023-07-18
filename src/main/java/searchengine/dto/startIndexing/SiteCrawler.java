package searchengine.dto.startIndexing;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.util.concurrent.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SiteCrawler implements Runnable {

	private String siteUrl;
	private SiteRepository siteRepository;
	private PageRepository pageRepository;
	private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

	//    private  ForkJoinPool forkJoinPool;
	@Override
	public void run() {
		try {
			searchAllPages(siteUrl);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public void searchAllPages(String siteUrl) throws InterruptedException {
		Connection connection = Jsoup.connect(siteUrl).userAgent("Mozilla/5.0");
		try {
			Document document = connection.get();
			Elements elements = document.select("a[href]");
			for (Element element : elements) {
				String link = element.absUrl("href");
				synchronized (pageRepository) {
					if (pageRepository.searchPageInDB(link) == null && link.startsWith("http")) {
						PageEntity page = new PageEntity();
						String sUrl = link.substring(0, link.indexOf("/", 8));
						SiteEntity site = siteRepository.findIdByUrl(sUrl);
						boolean isCorrect = link.startsWith(sUrl) && !link.endsWith(".pdf") && !link.endsWith(".PDF")
								&& !link.endsWith(".doc") && !link.endsWith(".DOC") && !link.endsWith("#") && !link.endsWith(".JPEG")
								&& !link.endsWith(".jpeg") && !link.endsWith(".jpg") && !link.endsWith(".JPG")
								&& !link.endsWith(".png") && !link.endsWith(".PNG");
						if (site != null && isCorrect) {
							page.setSite(site);
							page.setPath(link);
							page.setCode(connection.response().statusCode());
							page.setContent(element.ownerDocument().html());
							pageRepository.savePage(page.getSite().getId(), page.getPath(), page.getCode(), page.getContent());
							System.out.println("Page: " + page.getPath() + " is saved!");
							executorService.execute(new SiteCrawler(link, siteRepository, pageRepository, executorService));

						}
					}
				}
			}
		} catch (Exception exception) {
			exception.getMessage();

		}
	}
}