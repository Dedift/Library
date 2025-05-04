package main.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
@Setter
public class Author extends BaseEntity<Integer> {
    private String firstName;
    private String lastName;
    private List<Book> books;
}