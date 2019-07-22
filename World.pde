class World {
  World() {
    render();
  }
  
  // Dibujar cuadrícula normal
  void render() {
    fill(bgcol);
    noStroke();
    rect(0,0,width - panelWidth - 1, height);
    for(int i = 0; i < horsqrs + 1; i++) {
      stroke(gridcol);
      line(scl*i, 0, scl*i, versqrs*scl); 
    }
    for(int i = 0; i < versqrs + 1; i++) {
      stroke(gridcol);
      line(0, scl*i, horsqrs*scl, scl*i); 
    }
  }

  // Comprobar si las coords. x e y están dentro de los límites
  boolean isInsideBoundaries(float x, float y) {
    if(x >= scl*horsqrs || x < 0 || y >= scl*versqrs || y < 0) {
      return false;
    }
    return true;
  }
  
  // Mostrar el panel de la derecha
  void renderPanel() {
    pushMatrix();
    translate(width - panelWidth, 0);
    stroke(175);
    fill(panelcol);
    rect(0, 0, panelWidth, height);
    textSize(20);
    textAlign(LEFT, CENTER);
    fill(30);
    text("Generation: " + (population.cg + 1), 20, 20);
    text("Avg. fit.: " + population.lastAvgFitness, 20, 60);
    text("Snakes in gen.: " + population.snakesRemaining, 20, 100);
    if(population.gens.get(population.cg).showingBestSnake) {
      text("Showing best snake", 20, 140);
    } else {
      text("Training", 20, 140);
    }
    text("Score: " + currentScore, 20, 180);
    text("Speed: " + speedText, 20, 220);
    text("Mut. rate: " + round(mutRate*100) + "%", 20, 260);
    if(!hideKeys) {
      text("J-K-L: vary speed", 20, 340);
      text("S-D: vary mut. rate", 20, 370);
      text("R: render all snakes", 20, 400);
    }

    popMatrix();
  } 
}
