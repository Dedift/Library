package main.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
@Setter
public class Reader extends BaseEntity<Integer> {
    private String firstName;
    private String lastName;
    private String email;
    private List<Book> books;
}