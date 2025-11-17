package com.example.SINJA.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Tuple {
    Long key;
    Long address;


    @Override
    public String toString() {
        return "{ key=" + key + ", pos=" + address + " }";
    }
}
