package br.com.etl.painel_macroeconomico.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "Users", schema = "test")
public class UserModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "email", nullable = false, unique = true, length = 250)
    private String email;

    @Column(name = "senha", nullable = false, length = 100)
    private String senha;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    public UserModel(String nome, String email, String senha, LocalDate localDate) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.dataNascimento = localDate;
    }
}
