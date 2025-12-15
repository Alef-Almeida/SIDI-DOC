package br.com.ifba.sididoc.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "document_categories")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class DocumentCategory extends PersistenceEntity{

    @Column(nullable = false, unique = true)
    private String name;
    private String description;

    @Column(nullable = false)
    private boolean active;

}
