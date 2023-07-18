package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteEntity;

import java.util.List;

@Repository
public interface SiteRepository extends JpaRepository<SiteEntity, Integer> {

@Query(value = "SELECT * from site WHERE url LIKE :url LIMIT 1", nativeQuery = true)
SiteEntity findIdByUrl (@Param("url") String url);
}
