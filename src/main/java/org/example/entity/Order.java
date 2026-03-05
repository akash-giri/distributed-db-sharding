package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "t_order")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId; 
    
    private String productDescription;
    private Double amount;
}