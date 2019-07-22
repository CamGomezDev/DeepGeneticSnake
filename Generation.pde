class Generation {
  boolean showingBestSnake = false;
  int ns = 2000; // Número de serpientes
  Snake[] snakes = new Snake[ns];
  int cs; // Serpiente actual (current snake)

  Generation() {
    for (int i = 0; i < snakes.length; ++i) {
      snakes[i] = new Snake(i);
    }
    cs = 0;
  }
}
