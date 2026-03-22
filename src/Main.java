package lab1.src;
import java.io.IOException;
import java.util.DoubleSummaryStatistics;

public class Main {
    public static void main(String[] args) {
        // Upewnij się, że nazwa pliku zgadza się z tą na Twoim dysku (TSPA lub TSPB)
        String filepath = "TSPA.csv";

        try {
            Instance instance = new Instance(filepath);
            int n = instance.getSize();
            System.out.println("✅ Wczytano wierzchołków: " + n);
            System.out.println("Rozpoczynam eksperymenty (po 200 startów dla każdego algorytmu)...\n");

            // Twój zaktualizowany Solver!
            Solver solver = new Solver(instance);

            // Tablice do zapisywania wyników funkcji celu (Zysk - Dystans)
            double[] randomResults = new double[n];
            double[] nnWithoutProfitResults = new double[n];
            double[] nnWithProfitResults = new double[n];
            double[] gcWithoutProfitResults = new double[n];
            double[] gcWithProfitResults = new double[n];
            double[] regretWithProfitResults = new double[n];
            double[] weightedRegretWithProfitResults = new double[n];

            // Główna pętla eksperymentu - każdy wierzchołek jest punktem startowym dokładnie raz
            Solution bestWeightedRegret = null;
            for (int i = 0; i < n; i++) {
                bestWeightedRegret = null;
                double maxWeightedRegretScore = -Double.MAX_VALUE;
                // 0. Algorytm Losowy (bez Fazy II)
                Solution randomSolution = solver.randomSolution();
                randomResults[i] = randomSolution.getObjectiveValue();

                // 1. Najbliższy Sąsiad - FAZA I (bez zysku) + FAZA II
                Solution nnWithoutProfit = solver.nearestNeighbor(i, false);
                nnWithoutProfitResults[i] = nnWithoutProfit.getObjectiveValue();

                // 2. Najbliższy Sąsiad - FAZA I (z zyskiem) + FAZA II
                Solution nnWithProfit = solver.nearestNeighbor(i, true);
                nnWithProfitResults[i] = nnWithProfit.getObjectiveValue();

                // 3. Zachłanny Cykl - FAZA I (bez zysku) + FAZA II
                Solution gcWithoutProfit = solver.greedyCycle(i, false);
                gcWithoutProfitResults[i] = gcWithoutProfit.getObjectiveValue();

                // 4. Zachłanny Cykl - FAZA I (z zyskiem) + FAZA II
                Solution gcWithProfit = solver.greedyCycle(i, true);
                gcWithProfitResults[i] = gcWithProfit.getObjectiveValue();

                // 5. Zwykły 2-Żal z zyskiem (bez Fazy II)
                Solution regretWithProfit = solver.regretCycle(i, true, false);
                regretWithProfitResults[i] = regretWithProfit.getObjectiveValue();

                // 6. Ważony 2-Żal z zyskiem (bez Fazy II)
                Solution weightedRegretWithProfit = solver.regretCycle(i, true, true);
                weightedRegretWithProfitResults[i] = weightedRegretWithProfit.getObjectiveValue();

                if (weightedRegretWithProfit.getObjectiveValue() > maxWeightedRegretScore) {
                    maxWeightedRegretScore = weightedRegretWithProfit.getObjectiveValue();
                    // Robimy kopię najlepszego rozwiązania
                    bestWeightedRegret = new Solution(weightedRegretWithProfit);
                }

                // Pasek postępu, żeby wiedzieć, że program "żyje" i liczy
                if ((i + 1) % 50 == 0) {
                    System.out.println("Przeliczono " + (i + 1) + " / " + n + " iteracji...");
                }
            }
            saveRouteToFile(bestWeightedRegret, "best_route.txt");
            // --- WYPISYWANIE STATYSTYK ---
            System.out.println("\n=================== WYNIKI EKSPERYMENTÓW ===================");
            printStats("Algorytm Losowy", randomResults);
            printStats("Najbliższy Sąsiad (Faza I ignoruje zysk)", nnWithoutProfitResults);
            printStats("Najbliższy Sąsiad (Faza I uwzględnia zysk)", nnWithProfitResults);
            printStats("Zachłanny Cykl (Faza I ignoruje zysk)", gcWithoutProfitResults);
            printStats("Zachłanny Cykl (Faza I uwzględnia zysk)", gcWithProfitResults);
            printStats("Algorytm 2-Żal (uwzględnia zysk)", regretWithProfitResults);
            printStats("Ważony Algorytm 2-Żal (uwzględnia zysk)", weightedRegretWithProfitResults);
            System.out.println("============================================================");

        } catch (IOException e) {
            System.err.println("❌ Błąd! Nie udało się wczytać pliku: " + e.getMessage());
        }
    }

    /**
     * Metoda pomocnicza do ładnego wypisywania statystyk (Max, Min, Średnia)
     */
    private static void printStats(String algorithmName, double[] results) {
        DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        for (double result : results) {
            stats.accept(result);
        }

        System.out.println("👉 " + algorithmName + ":");
        // MAKSYMALIZUJEMY, więc największy wynik to najlepsza trasa!
        System.out.printf("   Najlepszy wynik (MAX) : %.2f\n", stats.getMax());
        System.out.printf("   Najgorszy wynik (MIN) : %.2f\n", stats.getMin());
        System.out.printf("   Średni wynik (AVG)    : %.2f\n\n", stats.getAverage());
    }
    // Metoda zapisująca ID wierzchołków do pliku
    private static void saveRouteToFile(Solution solution, String filename) {
        try (java.io.PrintWriter out = new java.io.PrintWriter(filename)) {
            for (int nodeId : solution.getPath()) {
                out.println(nodeId);
            }
            // Zamykamy cykl (dodajemy na koniec znowu pierwszy element)
            if (!solution.getPath().isEmpty()) {
                out.println(solution.getPath().get(0));
            }
            System.out.println("💾 Zapisano najlepszą trasę do pliku: " + filename);
        } catch (java.io.FileNotFoundException e) {
            System.err.println("Błąd zapisu pliku: " + e.getMessage());
        }
    }
}