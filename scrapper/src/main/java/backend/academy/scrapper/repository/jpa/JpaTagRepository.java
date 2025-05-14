package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.repository.jpa.entity.LinkEntity;
import backend.academy.scrapper.repository.jpa.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JpaTagRepository extends JpaRepository<TagEntity, Long> {

    List<TagEntity> findByLink(LinkEntity link);

    List<TagEntity> findByTag(String tag);

    void deleteByLink(LinkEntity link);

}
