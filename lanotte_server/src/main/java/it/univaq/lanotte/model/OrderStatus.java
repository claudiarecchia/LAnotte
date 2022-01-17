package it.univaq.lanotte.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
public enum OrderStatus {
    placed,
    preparing,
    prepared,
    collected
}
