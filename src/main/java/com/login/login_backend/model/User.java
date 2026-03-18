package com.login.login_backend.model;



import jakarta.persistence.*;

import lombok.AllArgsConstructor;

import lombok.Builder;

import lombok.Getter;

import lombok.NoArgsConstructor;

import lombok.Setter;

import java.time.LocalDateTime;



@Entity

@Table(name = "users")

@Getter

@Setter

@NoArgsConstructor

@AllArgsConstructor

@Builder

public class User {



    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;



    @Column(nullable = false, length = 60)

    private String firstName;



    @Column(nullable = false, length = 80)

    private String lastName;



    @Column(nullable = false, unique = true, length = 120)

    private String email;



    @Column(nullable = false)

    private String password;



    @Column(nullable = false)

    private String role;



    @Column(nullable = false)

    private boolean enabled;



    @Column(nullable = true)

    private String resetToken;



    @Column(nullable = true)

    private LocalDateTime resetTokenExpiry;



    @Column(nullable = true)

    private LocalDateTime passwordChangedAt;

}



