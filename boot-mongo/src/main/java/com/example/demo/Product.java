package com.example.demo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonRepresentation;

import java.math.BigDecimal;

@BsonDiscriminator("products")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Product {
    @BsonId @BsonRepresentation(BsonType.OBJECT_ID)
    String id;
    String name;
    BigDecimal price;

    Product withId(String newId) {
        return new Product(newId, this.name, this.price);
    }
}


