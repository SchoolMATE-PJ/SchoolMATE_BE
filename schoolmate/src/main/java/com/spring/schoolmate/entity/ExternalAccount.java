package com.spring.schoolmate.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "external_account")
@NoArgsConstructor
public class ExternalAccount {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ea_id")
    private Long eaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "provider_name", nullable = false)
    private String pName;

    @Column(name = "provider_id", nullable = false, unique = true)
    private String pId;
}
