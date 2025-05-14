package com.kenken.model.dto;

import com.kenken.model.OperationType;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public record CageDefinition(int targetValue, OperationType operationType, List<Coordinates> cellsCoordinates) implements Serializable {
    @Serial
    private static final long serialVersionUID = 20240514L;
    public CageDefinition {
        // Validazione degli argomenti
        Objects.requireNonNull(operationType, "OperationType non può essere nullo in CageDefinition.");
        Objects.requireNonNull(cellsCoordinates, "La lista delle coordinate delle celle non può essere nulla in CageDefinition.");

        // Se una gabbia DEVE avere coordinate, puoi aggiungere questo controllo:
        // if (cellsCoordinates.isEmpty()) {
        //     throw new IllegalArgumentException("La lista delle coordinate delle celle non può essere vuota in CageDefinition.");
        // }

        // Crea una copia difensiva della lista per assicurare l'immutabilità
        // se la lista passata al costruttore fosse modificabile esternamente.
        // List.copyOf() restituisce una lista non modificabile.
        cellsCoordinates = List.copyOf(cellsCoordinates);
    }
}
