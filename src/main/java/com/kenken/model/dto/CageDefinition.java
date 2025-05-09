package com.kenken.model.dto;

import com.kenken.model.OperationType;

import java.util.List;

public record CageDefinition(int targetValue, OperationType operationType, List<Coordinates> cellsCoordinates){

}
