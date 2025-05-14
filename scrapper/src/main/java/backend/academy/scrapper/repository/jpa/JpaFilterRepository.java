package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.repository.jpa.entity.FilterEntity;
import backend.academy.scrapper.repository.jpa.entity.LinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JpaFilterRepository extends JpaRepository<FilterEntity, Long> {

    List<FilterEntity> findByLink(LinkEntity link);;

    void deleteByLink(LinkEntity link);

}
