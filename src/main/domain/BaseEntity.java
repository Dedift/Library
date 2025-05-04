package main.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public abstract class BaseEntity <PK extends Serializable>{
    protected PK id;
}
