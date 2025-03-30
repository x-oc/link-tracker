package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.repository.jpa.entity.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaChatRepository extends JpaRepository<ChatEntity, Long> {}
