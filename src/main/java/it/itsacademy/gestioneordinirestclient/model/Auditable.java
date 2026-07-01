package it.itsacademy.gestioneordinirestclient.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
/*
Dice esplicitamente a Hibernate: "NON creare una tabella chiamata auditable." Al contrario, agisce come un modello "fantasma".
Quando la tua classe Ordini estende Auditable, Hibernate prende automaticamente quelle quattro variabili e le copia come
colonne direttamente nella tua tabella del database ordini. Esiste esclusivamente per evitarti di dover dichiarare le
stesse quattro variabili in ogni singola entità del tuo progetto.
*/
@MappedSuperclass
/*
Automatizza la gestione dei campi di auditing. Prima di un repository.save(), intercetta l'operazione, rileva annotazioni
come @CreatedDate e @CreatedBy, inserisce automaticamente la data/ora corrente e l'utente autenticato, quindi procede con
il salvataggio. In questo modo non è necessario impostare manualmente questi valori nel service layer.
*/
@EntityListeners(AuditingEntityListener.class)
public class Auditable {
    @Column(name = "created_date", nullable = false, updatable = false)
    @CreatedDate
    private Long createdDate;

    @Column(name = "modified_date")
    @LastModifiedDate
    private Long modifiedDate;

    @Column(name = "created_by")
    @CreatedBy
    private String createdBy;

    @Column(name = "modified_by")
    @LastModifiedBy
    private String modifiedBy;
}