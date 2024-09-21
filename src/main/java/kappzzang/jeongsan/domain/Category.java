package kappzzang.jeongsan.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String color;
}
