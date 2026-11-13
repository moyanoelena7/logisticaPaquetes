package com.logistica.ms_paquetes.domain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Table(name = "repartidor",
       uniqueConstraints = @UniqueConstraint(name = "uk_repartidor_username", columnNames = "username"))
@Getter
@Setter
@NoArgsConstructor
public class Repartidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 30)
    private String telefono;

    /** Usuario de Keycloak (claim preferred_username) que corresponde a este repartidor. */
    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private boolean disponible = true;
}