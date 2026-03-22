import matplotlib.pyplot as plt
import pandas as pd

# 1. Wczytanie oryginalnych współrzędnych
# Twoje pliki CSV mają kolumny: X; Y; Koszt (bez nagłówków)
df = pd.read_csv('TSPA.csv', sep=';', header=None, names=['x', 'y', 'cost'])

# 2. Wczytanie kolejności wierzchołków naszej najlepszej trasy
with open('best_route.txt', 'r') as f:
    route = [int(line.strip()) for line in f.readlines()]

# Wyciągamy współrzędne wierzchołków w kolejności występowania w trasie
route_x = [df.iloc[node_id]['x'] for node_id in route]
route_y = [df.iloc[node_id]['y'] for node_id in route]

# 3. Rysowanie wykresu
plt.figure(figsize=(12, 8))

# Najpierw rysujemy WSZYSTKIE 200 punktów jako szare, lekko przezroczyste kropki.
# Wielkość kropki uzależniamy od zysku (im większy zysk, tym większa kropka)
sizes = df['cost'] / df['cost'].max() * 200
plt.scatter(df['x'], df['y'], c='lightgray', s=sizes, label='Ominięte wierzchołki')

# Następnie rysujemy wybrane wierzchołki oraz czerwoną linię trasy
plt.plot(route_x, route_y, c='red', linewidth=2, zorder=1, label='Trasa komiwojażera')
plt.scatter(route_x, route_y, c='blue', s=[sizes[i] for i in route], zorder=2, label='Odwiedzone wierzchołki')

# Zaznaczamy wierzchołek startowy na zielono (gwiazdka)
plt.scatter(route_x[0], route_y[0], c='green', marker='*', s=300, zorder=3, label='Start')

# Estetyka wykresu
plt.title('Najlepsza znaleziona trasa (Ważony 2-Żal)\nProblem komiwojażera z zyskami', fontsize=16)
plt.xlabel('Współrzędna X', fontsize=12)
plt.ylabel('Współrzędna Y', fontsize=12)
plt.legend()
plt.grid(True, linestyle='--', alpha=0.5)

# Zapis do pliku i wyświetlenie
plt.savefig('najlepsza_trasa.png', dpi=300, bbox_inches='tight')
plt.show()