package edu.fpt.groupfive.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cglib.core.Local;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
public abstract class AbstractEntity<T> {

    private T id;
    private LocalDate createdAt;
    private LocalDate updatedAt;

}
