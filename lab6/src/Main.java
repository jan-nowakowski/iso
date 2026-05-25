package lab6.src;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        String filepath = "TSPA.csv"; // Możesz zmienić na TSPB.csv w przyszłości

        try {
            Instance instance = new Instance(filepath);
            Solver solver = new Solver(instance);
            long timeLimitMs = 1500;


            System.out.println("\n=========================================================================");
            System.out.println("🚀 ROZPOCZYNAMY REALIZACJĘ ZADANIA 6: PORÓWNANIE METAHEURYSTYK I HAE");
            System.out.println("=========================================================================");

            int numRuns = 10;
            System.out.println("⏳ Trwa uruchamianie algorytmów (każdy testowany przez " + numRuns + " powtórzeń, limit " + timeLimitMs + "ms)...");

            // Mapa do zapisywania najlepszych znalezionych ścieżek
            Map<String, List<Integer>> bestPaths = new LinkedHashMap<>();

            runExperiment("Bazowe Lokalne Przeszk.", numRuns, () -> solver.solveBaseLocalSearch(), solver, bestPaths);

            runExperiment("MSLS", numRuns, () -> solver.solveMSLS(timeLimitMs), solver, bestPaths);
            runExperiment("ILS", numRuns, () -> solver.solveILS(timeLimitMs), solver, bestPaths);
            runExperiment("LNS", numRuns, () -> solver.solveLNS(timeLimitMs), solver, bestPaths);

            runExperiment("HAE (Op1 + LS)", numRuns, () -> solver.solveHAE(timeLimitMs, 1, true), solver, bestPaths);
            runExperiment("HAE (Op2 + LS)", numRuns, () -> solver.solveHAE(timeLimitMs, 2, true), solver, bestPaths);
            //runExperiment("HAE (Op2 bez LS)", numRuns, () -> solver.solveHAE(timeLimitMs, 2, false), solver, bestPaths);
            //runExperiment("HAE (Op3 + LS)", numRuns, () -> solver.solveHAE(timeLimitMs, 3, true), solver, bestPaths);
            //runExperiment("HAE (Op3 bez LS)", numRuns, () -> solver.solveHAE(timeLimitMs, 3, false), solver, bestPaths);

            // ---> DODAJ TĘ LINIKĘ <---
            runExperiment("Q-ALNS (RL, Zad 7)", numRuns, () -> solver.solveQALNS(timeLimitMs), solver, bestPaths);
            System.out.println("=========================================================================");

            // ZAPIS ŚCIEŻEK DO PLIKU
            try (PrintWriter out = new PrintWriter("lab6/wyniki/najlepsze_sciezki.txt")) {
                for (Map.Entry<String, List<Integer>> entry : bestPaths.entrySet()) {
                    out.print(entry.getKey() + ";");
                    List<Integer> path = entry.getValue();
                    for (int i = 0; i < path.size(); i++) {
                        out.print(path.get(i) + (i == path.size() - 1 ? "" : ","));
                    }
                    out.println();
                }
            }
            System.out.println("✅ Pomyślnie zapisano plik z trasami: lab6/wyniki/najlepsze_sciezki.txt");

        } catch (Exception e) {
            System.err.println("❌ Wystąpił błąd krytyczny: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runExperiment(String name, int runs, java.util.function.Supplier<Solution> algorithm,
                                      Solver solver, Map<String, List<Integer>> bestPathsMap) {
        double avgObj = 0;
        int maxObj = Integer.MIN_VALUE;
        long totalIterations = 0;
        Solution bestSolutionFound = null;

        System.out.print(String.format("%-23s", name) + " [");
        for (int i = 0; i < runs; i++) {
            Solution sol = algorithm.get();
            int obj = sol.getObjectiveValue();
            avgObj += obj;
            totalIterations += solver.getLatestIterations();

            if (obj > maxObj) {
                maxObj = obj;
                bestSolutionFound = new Solution(sol);
            }
            System.out.print("=");
        }
        System.out.print("] ");
        System.out.printf("| Średnia: %7.2f | Max: %5d | Śr. Iteracji: %6.1f\n",
                avgObj / runs, maxObj, (double) totalIterations / runs);

        if (bestSolutionFound != null) {

            bestPathsMap.put(name, bestSolutionFound.getPath());
        }
    }
}