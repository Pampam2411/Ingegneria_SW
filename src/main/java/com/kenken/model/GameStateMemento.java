package com.kenken.model;

import com.kenken.model.dto.CageDefinition;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public record GameStateMemento(
        int N,
        List<CageDefinition> cageDefinitions, // le definizioni delle gabbie
        int[][] cellValues, //i valori attuali di ogni cella
        boolean[][] cellEditability, // se una cella è editabile o meno
        GameModel.GameState gameState, //lo stato corrente del gioco
        String difficulty,
        boolean realTimeValidationEnabled // se il controllo dei vincoli è attivo o meno
        ) implements Serializable {

    public GameStateMemento{
        if (N<0)throw new IllegalArgumentException("N deve essere un numero positivo.");
        if(cageDefinitions==null)throw new IllegalArgumentException("cageDefinitions non può essere null.");
        if(cellValues==null)throw new IllegalArgumentException("La matrice dei valori non può essere null.");
        if(cellEditability==null)throw new IllegalArgumentException("La matrice di editabilità delle celle non può essere null.");
        if(gameState==null)throw new IllegalArgumentException("Lo stato del gioco non può essere null.");

        if (N > 0 && (difficulty == null || difficulty.trim().isEmpty())) {
            throw new IllegalArgumentException("La difficoltà non può essere nulla o vuota per un gioco inizializzato (N > 0).");
        }

        if(N>0){
            if(cellValues.length!=N || cellEditability.length!=N)
                throw new IllegalArgumentException("La dimensione delle matrici non corrisponde al valore di N.");

            for(int i=0;i<N;i++){
                if(cellValues[i].length!=N || cellEditability[i].length!=N || cellValues[i]==null || cellEditability[i]==null)
                    throw new IllegalArgumentException("La dimensione delle matrici non corrisponde al valore di N o hanno valori null.");
            }
        }else{
            if(cellValues.length!=0 || cellEditability.length!=0)
                throw new IllegalArgumentException("Quando N=0, le matrici devono essere vuote.");
        }
    }

    public static GameStateMemento createNotInitializedMemento(boolean realTimeValidationEnabled) {
        return new GameStateMemento(
                0,
                Collections.emptyList(),
                new int[0][0],      // Matrice vuota per valori
                new boolean[0][0],  // Matrice vuota per editabilità
                GameModel.GameState.NOT_INITIALIZED,
                "NOT_INITIALIZED", // Stringa indicativa per la difficoltà
                realTimeValidationEnabled
        );
    }
}
