package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Integer> {

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM page WHERE site_id = :siteId", nativeQuery = true)
    void deleteBySiteId(@Param("siteId") int siteId);

    @Query(value = "SELECT * FROM page WHERE path = :pageUrl LIMIT 1", nativeQuery = true)
    PageEntity searchPageInDB (@Param("pageUrl") String pageUrl);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO page (site_id, path, code, content)" +
            " VALUES(:siteId, :path, :code, :content)", nativeQuery = true)
    void savePage(@Param("siteId") int siteId, @Param("path") String path, @Param("code")int code, @Param("content") String content);


}
