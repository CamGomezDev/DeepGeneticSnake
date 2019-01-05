class Population {
  ArrayList<Generation> gens;
  int cg;
  int cshowfI;
  Snake showoff;
  Food showoffFood;
  long lastAvgFitness = 0;
  int snakesRemaining = 0;

  public Population() {
    gens = new ArrayList<Generation>();
    gens.add(new Generation());
    cg = 0;
  }

  void update() {
    boolean currentShowoff = false;
    boolean oneAlive = false;
    Generation gen = gens.get(cg);

    snakesRemaining = 0;
    
    for (int i = 0; i < gen.snakes.length; ++i) {
      if(gen.snakes[i].alive) {
        
        snakesRemaining += 1;
        oneAlive = true;
        gen.snakes[i].update();
        if(renderingAll) {
          gen.snakes[i].render();
          currentScore = 0;
        } else {
          if(!currentShowoff) {
            currentShowoff = true;
            cshowfI = i;
            gen.snakes[i].render();
            currentScore = gen.snakes[i].relLength - 1;
          }
        }
        controller.play(gen.snakes[i]);
      }
    }
    
    //Different runs
    for(int ii = 0; ii < gen.inRun.length; ii++) {
      if(!oneAlive && !gen.inRun[ii]) { 
        for(int i = 0; i < gen.snakes.length; i++) {
          gen.snakes[i].lifetime = 0;
          gen.snakes[i].relLength = 1;
          gen.snakes[i].alive = true;
          gen.snakes[i].pos = new PVector[1];
          gen.snakes[i].prevHead = new PVector(0, 0);
          gen.snakes[i].pos[0] = new PVector(scl*floor(horsqrs/2), scl*floor(versqrs/2));
          gen.snakes[i].startVel();
        }
        gen.inRun[ii] = true;
        oneAlive = true;
      }
    }

    // This logic is to display the best
    if(!oneAlive && !gens.get(cg).showingBestSnake) {
      chooseBestSnake();
    }
    if(!oneAlive && gens.get(cg).showingBestSnake) {
      playBestSnake();
    }
  }

  void chooseBestSnake() {
    showoff = new Snake(-1, 1);
    showoffFood = new Food();
    double bestFitness = 0;
    lastAvgFitness = 0;
    for (Snake snake : gens.get(cg).snakes) {
      lastAvgFitness += snake.fitness;
      if(snake.fitness > bestFitness) {
        bestFitness = snake.fitness;
        showoff.brain = snake.brain.clone();
      }
    }
    lastAvgFitness = lastAvgFitness/gens.get(cg).ns;
    gens.get(cg).showingBestSnake = true;
  }

  void playBestSnake() {
    showoff.update();
    showoff.render();
    currentScore = showoff.relLength - 1;
    controller.play(showoff);
    // println(controller.frame(showoff));
    if(!showoff.alive) {
      changeGen();
    }
    showoffFood.update(showoff);
    showoffFood.render();
  }

  void changeGen() {
    foods.restart();
    Generation oldGen = gens.get(cg);
    float totFitness = 0;

    for(Snake snake : oldGen.snakes) {
      totFitness = totFitness + snake.fitness;
    }
    // println(totFitness);

    cg = cg + 1;
    gens.add(new Generation());

    Generation newGen = gens.get(cg);
    // The brains are produced
    for(Snake mainSnake : newGen.snakes) {
      // Choose first parent using probability algorithm
      Snake firstParent = new Snake(0, 1);
      float randomi = random(totFitness);
      float fitnessCount = 0;
      for(Snake snake : oldGen.snakes) {
        fitnessCount = fitnessCount + snake.fitness;
        if(randomi < fitnessCount) {
          firstParent = snake;
          break;
        }
      }
      // Choose second parent, also prob. algo.
      Snake secondParent = new Snake(0, 1);
      randomi = random(totFitness);
      fitnessCount = 0;
      for(Snake snake : oldGen.snakes) {
        fitnessCount = fitnessCount + snake.fitness;
        if(randomi < fitnessCount) {
          secondParent = snake;
          break;
        }
      }
      // Set brain
      mainSnake.brain = firstParent.brain.crossover(secondParent.brain);
      mainSnake.brain.mutate(mutRate);
    }

    gens.set(cg, newGen);
    gens.set(cg - 1, null);
  }
}

class Generation {
  boolean showingBestSnake = false;
  int runs = 1;
  boolean[] inRun = new boolean[runs - 1];
  int ns = 2000; //number of snakes
  Snake[] snakes = new Snake[ns];
  int cs; //current snake

  Generation() {
    for (int i = 0; i < inRun.length; i++) {
      inRun[i] = false;
    }
    for (int i = 0; i < snakes.length; ++i) {
      snakes[i] = new Snake(i, runs);
    }
    cs = 0;
  }
}
