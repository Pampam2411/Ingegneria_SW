package com.kenken.model.dto;

import java.io.Serial;
import java.io.Serializable;

public record Coordinates(int row, int col) implements Serializable {
    @Serial
    private static final long serialVersionUID = 2024051401L;

}
