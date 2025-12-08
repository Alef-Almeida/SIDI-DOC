package br.com.ifba.sididoc.entity;

import br.com.ifba.sididoc.enums.DocumentType;
import br.com.ifba.sididoc.enums.ProcessingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "documents")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Document extends PersistenceEntity{
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private LocalDateTime uploadDate;
    @Column(nullable = false)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Integer type;

    @ElementCollection
    @CollectionTable(name = "document_metadata",
            joinColumns = @JoinColumn(name = "document_id"))
    @MapKeyColumn(name = "meta_key")
    @Setter(AccessLevel.NONE)
    @Column(name = "meta_value")
    private Map<String, String> metaData = new HashMap<>();

    @Column(nullable = false)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Integer status;
    private String extractedText;
    private Float ocrConfidence;

    public DocumentType getType() {
        return DocumentType.fromCode(this.type);
    }

    public void setType(DocumentType type) {
        if (type != null) {
            this.type = type.getCode();
        } else {
            this.type = null;
        }
    }

    public ProcessingStatus getStatus() {
        return ProcessingStatus.fromCode(this.status);
    }

    public void setStatus(ProcessingStatus status) {
        if (status != null) {
            this.status = status.getCode();
        } else {
            this.status = null;
        }
    }
}
