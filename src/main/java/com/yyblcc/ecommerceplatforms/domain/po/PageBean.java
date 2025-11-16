package com.yyblcc.ecommerceplatforms.domain.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageBean<T> implements Serializable {
    private Long total;
    private List records;
}
