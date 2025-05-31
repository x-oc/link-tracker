package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.repository.jpa.entity.LinkEntity;
import backend.academy.scrapper.repository.jpa.entity.TagEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaTagRepository extends JpaRepository<TagEntity, Long> {

    List<TagEntity> findByLink(LinkEntity link);

    List<TagEntity> findByTag(String tag);

    void deleteByLink(LinkEntity link);
}
