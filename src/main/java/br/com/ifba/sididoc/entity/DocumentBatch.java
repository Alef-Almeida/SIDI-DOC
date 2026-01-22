package br.com.ifba.sididoc.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
@Table(name = "document_batches")
public class DocumentBatch extends PersistenceEntity {
    @Column(unique = true, nullable = false)
    private String code;
    private String description;
    @JsonIgnore
    @OneToMany(mappedBy = "batch", fetch = FetchType.LAZY)
    private List<Document> documents = new ArrayList<>();
}
