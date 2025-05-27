package backend.academy.scrapper.repository.jpa.entity;

import backend.academy.scrapper.model.Link;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "link")
@AllArgsConstructor
@NoArgsConstructor
public class LinkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;

    @Column(name = "last_checked")
    private OffsetDateTime lastChecked;

    @Column(name = "last_updated")
    private OffsetDateTime lastUpdated;

    @OneToMany(mappedBy = "link", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TagEntity> tags = new HashSet<>();

    @OneToMany(mappedBy = "link", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FilterEntity> filters = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "links")
    private Set<ChatEntity> chats = new HashSet<>();

    public LinkEntity(String url, OffsetDateTime lastUpdated, OffsetDateTime lastChecked) {
        this.url = url;
        this.lastUpdated = lastUpdated;
        this.lastChecked = lastChecked;
    }

    public Link toDto() {
        return new Link(
                id,
                url,
                tags.stream().map(TagEntity::tag).collect(Collectors.toList()),
                filters.stream().map(FilterEntity::filter).collect(Collectors.toList()),
                lastChecked,
                lastUpdated);
    }
}
