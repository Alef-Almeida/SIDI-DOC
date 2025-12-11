package br.com.ifba.sididoc.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "document_categories")
public class DocumentCategory extends PersistenceEntity{

    @Column(nullable = false)
    private String name;
    private String description;

}
