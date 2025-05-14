package com.kenken.generator;

import com.kenken.model.Cage;
import com.kenken.model.Cell;
import com.kenken.model.Grid;
import com.kenken.model.OperationType;
import com.kenken.model.dto.CageDefinition;
import com.kenken.model.dto.Coordinates;
import com.kenken.solver.KenKenSolver;

import java.util.*;

public class PuzzleGenerator {

    public static final String DIFFICULTY_EASY = "EASY";
    public static final String DIFFICULTY_MEDIUM = "MEDIUM";
    public static final String DIFFICULTY_HARD = "HARD";
    private static final int DEFAULT_MAX_GENERATION_ATTEMPTS = 100;

    static class DifficultyConfiguration{

        private static final Set<OperationType> EASY_OPERATIONS= EnumSet.of(OperationType.ADD, OperationType.SUB, OperationType.NONE);
        private static final Set<OperationType> MEDIUM_OPERATIONS= EnumSet.of(OperationType.ADD, OperationType.SUB, OperationType.MUL, OperationType.NONE);
        private static final Set<OperationType> HARD_OPERATIONS= EnumSet.of(OperationType.ADD, OperationType.SUB, OperationType.MUL, OperationType.DIV, OperationType.NONE);

        public static Set<OperationType> getAllowedOperators(int N, String difficulty){
            if(N<3 || N>6)
                throw new IllegalArgumentException("La dimensione del puzzle deve essere compresa tra 3 e 6");
            if(difficulty==null || difficulty.trim().isEmpty())
                throw new IllegalArgumentException("La difficoltà non può essere nulla o vuota");

            String normalizedDifficulty =difficulty.trim().toUpperCase(); //serve a far matchare la stringa se fosse scritta in minuscolo
           switch (normalizedDifficulty){
                case DIFFICULTY_EASY:
                    return EASY_OPERATIONS;
                case DIFFICULTY_MEDIUM:
                    if(N>=4)
                        return MEDIUM_OPERATIONS;
                    else
                        throw new IllegalArgumentException("La difficoltà MEDIUM è supportata solo per puzzle di dimensione 4 o più.");

                case DIFFICULTY_HARD:
                    if(N==6)
                        return HARD_OPERATIONS;
                    else{
                        String difficult=(N>=4)? DIFFICULTY_MEDIUM : DIFFICULTY_EASY;
                        throw new IllegalArgumentException("Per puzzle di dimensione "+N+" la difficoltà più elevata supportata è "+difficult+".");
                    }
                default:
                    throw new IllegalArgumentException("Livello di difficoltà non supportato: "+difficulty+".\n" +
                            "I livelli di difficoltà supportati sono: "+DIFFICULTY_EASY+", "+DIFFICULTY_MEDIUM+", "+DIFFICULTY_HARD+".");
            }
        }
    }

    record PuzzleVerificationResult(boolean isValid, boolean hasUniqueSolution, int numberOfSolutions) {
    }

    private final Random randomGenerator;

    public PuzzleGenerator(){
        this.randomGenerator=new Random();
    }

    public List<CageDefinition> generatePuzzle(int N, String difficulty){

        Set<OperationType> allowedOperations= DifficultyConfiguration.getAllowedOperators(N,difficulty);

        //Generazione massimo 100 griglie soluzione tipo Sudoku
        for(int attemp=1;attemp<=DEFAULT_MAX_GENERATION_ATTEMPTS;attemp++) {
            Grid solvedGrid = generateSolvedGrid(N);
            if (solvedGrid == null)
                throw new RuntimeException("Impossibile generare la griglia soluzione per puzzle di dimensione " + N);

            solvedGrid.printToConsole();


            //Generazione delle gabbia della griglia
            List<List<Coordinates>> cagePartitions = partitionGridIntoCages(N, difficulty, allowedOperations);

            List<CageDefinition> cageDefinitions = defineCageConstraints(solvedGrid, cagePartitions, allowedOperations);

            PuzzleVerificationResult verificationResult = verifyPuzzle(N, cageDefinitions);

            if (verificationResult.isValid()) {
                return cageDefinitions; // Puzzle valido trovato!
            } else { // !verificationResult.isValid()
                System.err.println("Puzzle generato NON è valido (nessuna soluzione trovata dal solver). Si ritenta...");
                // Continua il loop per un altro tentativo
            }
        }

        throw new RuntimeException("Impossibile generare il puzzle con le impostazioni specificate.");
    }

    private PuzzleVerificationResult verifyPuzzle(int N, List<CageDefinition> cageDefinitions){
        Grid puzzleGridToSolve=new Grid(N);
        List<Cage> actualCage= new ArrayList<>();

        for(CageDefinition def:cageDefinitions){
            Cage newCage=new Cage(def.targetValue(),def.operationType());
            for(Coordinates coord:def.cellsCoordinates()) {
                Cell cellInPuzzle = puzzleGridToSolve.getCell(coord.row(), coord.col());
                newCage.addCell(cellInPuzzle);
            }
            actualCage.add(newCage);
        }
        KenKenSolver solver=new KenKenSolver(puzzleGridToSolve,actualCage, N);
        List<Grid> solutions=solver.solve(DEFAULT_MAX_GENERATION_ATTEMPTS);
        return new PuzzleVerificationResult(!solutions.isEmpty(),solutions.size()==1,solutions.size());

    }

    private List<CageDefinition> defineCageConstraints(Grid solvedGrid, List<List<Coordinates>> cagePartitions, Set<OperationType> allowedOperations) {
        List<CageDefinition> cageDefinitions=new ArrayList<>();

        for(List<Coordinates> cageCoords:cagePartitions){
            if(cageCoords.isEmpty())continue;

            List<Integer> valuesInCage=new ArrayList<>();
            for(Coordinates coord:cageCoords){
                valuesInCage.add(solvedGrid.getCell(coord.row(),coord.col()).getValue());
            }
            OperationType chosenOperator;
            int targetValue;
            int cageSize=cageCoords.size();


            //Scelta operatore in base alle celle della gabbia
            if(cageSize==1){
                chosenOperator=OperationType.NONE;
            } else if (cageSize==2) {
                List<OperationType>possibleOps=new ArrayList<>();
                if (allowedOperations.contains(OperationType.ADD)) possibleOps.add(OperationType.ADD);
                if (allowedOperations.contains(OperationType.SUB)) possibleOps.add(OperationType.SUB);
                if (allowedOperations.contains(OperationType.MUL)) possibleOps.add(OperationType.MUL);
                if (allowedOperations.contains(OperationType.DIV)) {
                    int v1=valuesInCage.get(0), v2=valuesInCage.get(1);
                    if((v1%v2==0 && v1/v2>0) || (v2%v1==0 && v2/v1>0 || v1==v2))
                        possibleOps.add(OperationType.DIV);
                }
                if(possibleOps.isEmpty())
                    chosenOperator=OperationType.ADD;
                else chosenOperator = possibleOps.get(randomGenerator.nextInt(possibleOps.size()));
            }
            else{//significa che ci sono più di 2 gabbie allora solo add e mul
                List<OperationType>possibleOps=new ArrayList<>();
                if (allowedOperations.contains(OperationType.ADD)) possibleOps.add(OperationType.ADD);
                if (allowedOperations.contains(OperationType.MUL)) possibleOps.add(OperationType.MUL);

                if (possibleOps.isEmpty()) {
                    chosenOperator = OperationType.ADD; // Fallback
                } else {
                    chosenOperator = possibleOps.get(randomGenerator.nextInt(possibleOps.size()));
                }
            }

            switch (chosenOperator){
                case ADD:
                    targetValue=0;
                    for(int val:valuesInCage)targetValue+=val;
                    break;
                case SUB:
                    targetValue=Math.abs(valuesInCage.get(0)-valuesInCage.get(1));
                    break;
                case MUL:
                    targetValue=1;
                    for(int val:valuesInCage)targetValue*=val;
                    break;
                case DIV:
                    int v1=valuesInCage.get(0), v2=valuesInCage.get(1);
                    if(v1>v2) targetValue=v1/v2;
                    else targetValue=v2/v1;
                    if(v1==v2)targetValue=1;
                    break;
                case NONE:
                    targetValue=valuesInCage.getFirst();
                    break;
                default:
                    throw new RuntimeException("Operatore inaspettato: "+chosenOperator);
            }
            cageDefinitions.add(new CageDefinition(targetValue,chosenOperator,cageCoords));
        }
        return cageDefinitions;
    }

    private Grid generateSolvedGrid(int N) {
        Grid grid=new Grid(N);
        if(fillGridRecursive(grid,0,0,N))
            return grid;
        return null;
    }

    private boolean fillGridRecursive(Grid grid, int row, int col, int N) {
        if(col==N) {
            col=0;
            row++;
            if(row==N)
                return true;
        }

        if(grid.getCell(row,col).getValue()!=0)
            return fillGridRecursive(grid,row,col+1,N);

        //creo una lista dei possibili numeri da inserire e li mischio
        List<Integer> numberToTry= new ArrayList<>();
        for(int i=1;i<=N;i++)
            numberToTry.add(i);
        Collections.shuffle(numberToTry, this.randomGenerator);

        //provo inserimento del numero
        for(int num:numberToTry){

            if(isSafeToPlace(grid,row,col,num,N)){
                grid.getCell(row,col).setValue(num);
                //passo alla cella successiva in modo ricorsivo
                if(fillGridRecursive(grid,row,col+1,N))
                    return true;
                //se non funziona la scelta tolgo il valore della cella
                grid.getCell(row,col).setValue(0);
            }
        }
        //se nessun numero è valido da questo punto eseguo il backtrack
        return false;
    }

    private boolean isSafeToPlace(Grid grid, int row, int col, int num, int N){
        //check colonna
        for(int c=0;c<N;c++){
            if(grid.getCell(row,c).getValue()==num)
                return false;
        }
        //check riga
        for(int r=0;r<N;r++){
            if(grid.getCell(r,col).getValue()==num)
                return false;
        }
        return true;
    }

    private List<List<Coordinates>> partitionGridIntoCages(int N, String difficulty, Set<OperationType> allowedOperations){
        List<List<Coordinates>> allCageCoordinates=new ArrayList<>();
        boolean[][] isCellAssigned=new boolean[N][N]; //serve a tenere traccia delle celle che ho già assegnato


        int minCageSizeForAddMul=2;
        int maxCageSizeForAddMul=2;
        if(N>3){
            if(N>4 && (difficulty.equals(DIFFICULTY_EASY) || difficulty.equals(DIFFICULTY_MEDIUM)))maxCageSizeForAddMul=3;
            else if(N==6 && difficulty.equals(DIFFICULTY_HARD))maxCageSizeForAddMul=5;
            else maxCageSizeForAddMul=4;
        }

        for(int r=0;r<N;r++){
            for(int c=0;c<N;c++){
                if(!isCellAssigned[r][c]){
                    //inizio a creare una nuova gabbia da questa cella
                    List<Coordinates> currentCageCoords=new ArrayList<>();
                    OperationType chosenOpForThisCage;
                    int targetSizeThisCage;

                    //Creo lista possibili operatori
                    List<OperationType> candidateOpsForStart=new ArrayList<>(allowedOperations);
                    if(countUnassignedCells(isCellAssigned)<2){
                        candidateOpsForStart.remove(OperationType.SUB);
                        candidateOpsForStart.remove(OperationType.DIV);
                    }

                    if(candidateOpsForStart.isEmpty())
                        candidateOpsForStart.add(OperationType.NONE);

                    chosenOpForThisCage=candidateOpsForStart.get(randomGenerator.nextInt(candidateOpsForStart.size()));

                    switch (chosenOpForThisCage){
                        case NONE: targetSizeThisCage=1; break;
                        case SUB:
                        case DIV: targetSizeThisCage=2; break;
                        case ADD:
                        case MUL:

                            if(minCageSizeForAddMul == maxCageSizeForAddMul)
                                targetSizeThisCage= minCageSizeForAddMul;
                            else
                                targetSizeThisCage=randomGenerator.nextInt((maxCageSizeForAddMul - minCageSizeForAddMul +1)+ minCageSizeForAddMul);
                            break;
                        default: throw new IllegalArgumentException("Operatore inaspettato: "+chosenOpForThisCage);
                    }

                    targetSizeThisCage = Math.min(targetSizeThisCage, (N * N - countAssignedCells(isCellAssigned)));
                    if (targetSizeThisCage <= 0) targetSizeThisCage = 1;

                    Deque<Coordinates> queue=new ArrayDeque<>();
                    queue.offer(new Coordinates(r,c));
                    isCellAssigned[r][c]=true;
                    currentCageCoords.add(new Coordinates(r,c));

                    //avvio una ricerca di possibili celle vicine da aggiungere alla gabbia
                    while(!queue.isEmpty() && currentCageCoords.size()<targetSizeThisCage){
                        Coordinates currentCellToExpandFrom=queue.poll(); //prendo le coordinate della cella a cui devo trovare i vicini

                        //direzione vicini
                        int[] dr={-1,1,0,0};
                        int[] dc={0,0,-1,1};

                        List<Integer> dirOrder=new ArrayList<>();
                        for(int i=0;i<4;i++) dirOrder.add(i);
                        Collections.shuffle(dirOrder, this.randomGenerator);

                        //prova ad aggiungere i vicini
                        for(int directionIndex:dirOrder){

                            if(currentCageCoords.size()>=targetSizeThisCage) break;//se abbiamo raggiunto le celle per questa gabbia ci fermiamo

                            int nextR=currentCellToExpandFrom.row()+dr[directionIndex];
                            int nextC=currentCellToExpandFrom.col()+dc[directionIndex];

                            if(nextR>=0 && nextR<N && nextC>=0 && nextC<N && !isCellAssigned[nextR][nextC]){
                                isCellAssigned[nextR][nextC]=true;
                                Coordinates newCoord=new Coordinates(nextR,nextC);
                                currentCageCoords.add(newCoord);
                                queue.offer(newCoord);
                            }
                        }
                    }
                    allCageCoordinates.add(currentCageCoords);
                }
            }
        }
        for (int r_idx = 0; r_idx < N; r_idx++) {
            for (int c_idx = 0; c_idx < N; c_idx++) {
                if (!isCellAssigned[r_idx][c_idx]) {
                    List<Coordinates> singleCellCage = new ArrayList<>();
                    singleCellCage.add(new Coordinates(r_idx, c_idx));
                    allCageCoordinates.add(singleCellCage);
                    // isCellAssigned[r_idx][c_idx] = true; // Non strettamente necessario qui se è l'ultimo passaggio
                    System.out.println("Aggiunta gabbia NONE di fallback per cella non coperta: (" + r_idx + "," + c_idx + ")");
                }
            }
        }
        return allCageCoordinates;
    }

    private int countUnassignedCells(boolean[][] isCellAssigned){
        int unassignedCount=0;
        for(boolean[] row:isCellAssigned)
            for(boolean cell:row)
                if(!cell) unassignedCount++;
        return unassignedCount;
    }

    private int countAssignedCells(boolean[][] isCellAssigned){
        int count=0;
        for(boolean[] row:isCellAssigned)
            for(boolean cell:row)
                if(cell) count++;
        return count;
    }

}
