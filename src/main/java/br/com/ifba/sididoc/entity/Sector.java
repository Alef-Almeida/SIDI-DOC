package br.com.ifba.sididoc.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sectors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String code;

    @Column(length = 1000)
    private String description;

    private String createBy;
    private String lastModifiedBy;

    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

}
