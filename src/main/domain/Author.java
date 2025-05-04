package main.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class Author extends BaseEntity<Integer> {
    private String firstName;
    private String lastName;
}