package searchengine.services;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.config.Task;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.startIndexing.SiteCrawler;
import searchengine.dto.startIndexing.StartIndexingResponse;
import searchengine.model.SiteEntity;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class StartIndexingServiceImpl implements StartIndexingService {

    private final SitesList sites;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private ArrayList<Thread> list = new ArrayList<>();

    @Override
    public StartIndexingResponse startIndexing() {
        long start = System.currentTimeMillis();
        deletingSitesFromDB(sites);
        createSites(sites);
        findPages(sites);
        StartIndexingResponse response = new StartIndexingResponse();
        response.setResult(true);
        System.err.println("Индексация сайтов завершена");
        System.out.println("Индексация заняла " + ((System.currentTimeMillis() - start) / 1000) + " секунд" );
        return response;
    }

    public void deletingSitesFromDB(SitesList sites) {
        for (Site site : sites.getSites()) {
            SiteEntity entity = siteRepository.findIdByUrl(site.getUrl());
            if (entity != null) {
                pageRepository.deleteBySiteId(entity.getId());
                siteRepository.deleteById(entity.getId());
            }
        }
    }

    public void createSites(SitesList sites) {
        for (Site site : sites.getSites()) {
            SiteEntity entity = new SiteEntity();
            entity.setUrl(site.getUrl());
            entity.setName(site.getName());
            entity.setStatus(SiteEntity.SiteStatus.INDEXING);
            entity.setStatusTime(LocalDateTime.now());
            siteRepository.save(entity);
        }
    }

    public void findPages(SitesList sites) {
        for (Site s : sites.getSites()) {
            SiteCrawler site = new SiteCrawler(s.getUrl(), siteRepository, pageRepository, Executors.newFixedThreadPool(12));
            Thread thread = new Thread(site);
            list.add(thread);
        }
        list.forEach(Thread::start);
    }
}
