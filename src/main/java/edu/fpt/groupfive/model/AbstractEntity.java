package edu.fpt.groupfive.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public abstract class AbstractEntity<T> {

    private T id;
    private Date createdAt;
    private Date updatedAt;
}
