package br.com.ifba.sididoc.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sectors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sector extends PersistenceEntity{

    private String name;
    private String code;

    @Column(length = 1000)
    private String description;

}
