package com.example.demo;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("products")
public record Product(@Id Long id, String name, BigDecimal price) {
}
