package com.example.demo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
class Person {
    Integer id;
    String firstName;
    String lastName;
    Integer age;
}
