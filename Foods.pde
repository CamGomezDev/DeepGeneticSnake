// Todas las comidas para todas las serpientes entrenándose en una gen.
class Foods {
  Food[] foods = new Food[population.gens.get(population.cg).ns];

  Foods() {
    for (int i = 0; i < foods.length; ++i) {
      foods[i] = new Food();
    }
  }


  void update() {
    Generation gen = population.gens.get(population.cg);
    for(int i = 0; i < foods.length; ++i) {
      foods[i].update(gen.snakes[i]); // actualizar la posición de todas las comidas
      if(i == population.cshowfI && !gen.showingBestSnake) {
        foods[i].render(); // pero solo mostrar la comida de la serpiente que se está mostrando
      }
    }
  }

  void restart() {
    for (int i = 0; i < foods.length; ++i) {
      foods[i] = new Food();
    }
  }
}