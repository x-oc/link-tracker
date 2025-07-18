package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.repository.jpa.entity.TagEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface JpaTagRepository extends JpaRepository<TagEntity, Long> {

    List<TagEntity> findByLinks_IdAndChat_Id(long linkId, long chatId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = """
        INSERT INTO tag (name, chat_id)
        SELECT :tag, :chatId
        WHERE NOT EXISTS (
            SELECT 1 FROM tag WHERE name = :tag AND chat_id = :chatId
        );
        INSERT INTO link_tag (link_id, tag_id)
        SELECT :linkId, t.id
        FROM tag t
        WHERE t.name = :tag AND t.chat_id = :chatId
        AND NOT EXISTS (
            SELECT 1 FROM link_tag lt
            WHERE lt.link_id = :linkId AND lt.tag_id = t.id
        );
    """)
    void add(long linkId, String tag, long chatId);

    void deleteByLinks_Id(long linkId);
}
