package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.repository.jpa.entity.FilterEntity;
import backend.academy.scrapper.repository.jpa.entity.LinkEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaFilterRepository extends JpaRepository<FilterEntity, Long> {

    List<FilterEntity> findByLink(LinkEntity link);

    void deleteByLink(LinkEntity link);
}
