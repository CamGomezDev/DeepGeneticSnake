class World {
  World() {
    render();
  }
  
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

  boolean isInsideBoundaries(float x, float y) {
    if(x >= scl*horsqrs || x < 0 || y >= scl*versqrs || y < 0) {
      return false;
    }
    return true;
  }
  
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
      boolean runInArray = false;
      for (int i = population.gens.get(population.cg).inRun.length - 1; i > -1; i--) {
        if(population.gens.get(population.cg).inRun[i]) {
          text("Generation run " + (i + 2), 20, 140);
          runInArray = true;
          break;
        }
      }
      if(!runInArray) {
        text("Generation run 1", 20, 140);
      }
    }
    text("Score: " + currentScore, 20, 180);
    text("Speed: " + speedText, 20, 220);
    text("Mut. rate: " + round(mutRate*100) + "%", 20, 260);
    if(!hideKeys) {
      text("J-K-L: vary speed", 20, 340);
      text("S-D: vary mut. rate", 20, 370);
      text("R: render all snakes", 20, 400);
    }
    
    // //Button 1
    // stroke(0);
    // if(controller.isHuman) {
    //   fill(110);
    // } else {
    //   fill(200);
    // }
    // textSize(20);
    // rect(20, 180, 70, 30);
    // fill(0);
    // textAlign(CENTER, CENTER);
    // text("Smart", 20 + 70/2, 180 + 30/2 - 3);
    
    // //Button 2
    // stroke(0);
    // if(controller.isHuman) {
    //   fill(200);
    // } else {
    //   fill(110);
    // }
    // textSize(20);
    // rect(20, 220, 70, 30);
    // fill(0);
    // textAlign(CENTER, CENTER);
    // text("You", 20 + 70/2, 220 + 30/2 - 3);   
    
    popMatrix();
  } 
}

void mouseClicked() {
  float panelX = width - panelWidth;
  if(mouseX > panelX + 20 && mouseX < panelX + 20 + 70 && mouseY > 180 && mouseY < 180 + 30) {
    controller.isHuman = false;
    world.renderPanel();
  }
  else if(mouseX > panelX + 20 && mouseX < panelX + 20 + 70 && mouseY > 220 && mouseY < 220 + 30) {
    controller.isHuman = true;
    world.renderPanel();
  }
}