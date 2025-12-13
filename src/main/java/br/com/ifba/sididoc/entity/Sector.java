package br.com.ifba.sididoc.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "sectors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sector extends PersistenceEntity{

    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false, unique = true)
    private String code;
    @Column(length = 1000)
    private String description;
    @Column(nullable = false)
    private boolean active = true;

    @ManyToMany(mappedBy = "sectors", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();
}
