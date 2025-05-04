package main.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString(exclude = "readers")
@Getter
@Setter
public class Book extends BaseEntity<Integer> {
    private String title;
    private Integer author_id;
    private Integer publishedYear;
    private List<Reader> readers;
}
