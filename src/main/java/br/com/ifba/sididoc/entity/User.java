package br.com.ifba.sididoc.entity;

import br.com.ifba.sididoc.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends PersistenceEntity{

    @Getter
    private String name;

    @Getter
    @Column(unique = true, nullable = false)
    private String email;

    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_sectors",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "sector_id")
    )
    private List<Sector> sectors = new ArrayList<>();

    private Boolean isFirstAccess;

}


