package backend.academy.scrapper.repository.jpa.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "chat")
public class ChatEntity {
    @Id
    private Long id;

    @ManyToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "chat_link",
            joinColumns = {@JoinColumn(name = "chat_id")},
            inverseJoinColumns = {@JoinColumn(name = "link_id")})
    private Set<LinkEntity> links = new HashSet<>();

    public ChatEntity(Long id) {
        this.id = id;
    }

    public void addLink(LinkEntity link) {
        links.add(link);
        link.chats().add(this);
    }

    public void removeLink(LinkEntity linkEntity) {
        links.removeIf(link -> link.id().equals(linkEntity.id()));
        linkEntity.chats().remove(this);
    }
}
