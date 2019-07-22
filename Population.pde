/* Desde esta clase se controlan todas las serpientes en la población, y se
   ejecuta el algoritmo genético. */
class Population {
  ArrayList<Generation> gens;
  int cg;
  int cshowfI;
  Snake showoff; // showoff es la mejor serpiente del grupo siendo mostrada
  Food showoffFood;
  long lastAvgFitness = 0;
  int snakesRemaining = 0;

  public Population() {
    gens = new ArrayList<Generation>();
    gens.add(new Generation());
    cg = 0;
  }

  // Desde aquí se mueven todas las serpientes
  void update() {
    boolean showingOneSelected = false;
    boolean oneAlive = false;
    Generation gen = gens.get(cg); // obtener número ede generación actual o current gen (cg)

    snakesRemaining = 0; // serpientes restantes
    
    // Ciclo de todas las serpientes vivas
    for (int i = 0; i < gen.snakes.length; ++i) {
      if(gen.snakes[i].alive) { // Si la serpiente aún está viva
        
        snakesRemaining += 1;
        oneAlive = true;
        gen.snakes[i].update(); // Actualizar la posición de la serpiente
        if(renderingAll) { // Y si está el modo mostrar todas las serpientes
          gen.snakes[i].render(); // Renderizar cada una
          currentScore = 0;
        } else { // Pero si solo se está mostrando una
          if(!showingOneSelected) { // Mostrar la primera que encuentre viva
            showingOneSelected = true;
            cshowfI = i;
            gen.snakes[i].render();
            currentScore = gen.snakes[i].pos.length - 1;
          }
        }
        controller.play(gen.snakes[i]); // Aquí se usa la I.A. para elegir el siguiente movimiento de cada serpiente
      }
    }

    // Lógica usada para mostrar la mejor serpiente
    if(!oneAlive && !gens.get(cg).showingBestSnake) { // Si no queda ninguna serpiente viva y actualmente no se ha elegido la mejor serpiente...
      chooseBestSnakeAndGetAvgFitness(); // ...elegir la mejor y calcular el fitness promedio (avg.) de la gen.
    }
    if(!oneAlive && gens.get(cg).showingBestSnake) { // Si ya se eligió la mejor...
      playBestSnake(); // ...jugar normal con la mejor. Cuando la serpiente muere se ejecuta el algo. gen.
    }
  }

  // Aquí se elige la mejor serpiente y se actualiza el fitness promedio de la gen.
  void chooseBestSnakeAndGetAvgFitness() {
    showoff = new Snake(-1);
    showoffFood = new Food();
    double bestFitness = 0;
    lastAvgFitness = 0;
    // La mejor serpiente sale del fitness
    for (Snake snake : gens.get(cg).snakes) {
      lastAvgFitness += snake.fitness;
      if(snake.fitness > bestFitness) {
        bestFitness = snake.fitness;
        showoff.brain = snake.brain.clone();
      }
    }
    lastAvgFitness = lastAvgFitness/gens.get(cg).ns; //ns es number of snakes
    gens.get(cg).showingBestSnake = true;
  }

  // Se juega con la mejor serpiente normal
  void playBestSnake() {
    showoff.update();
    showoff.render();
    currentScore = showoff.pos.length - 1;
    controller.play(showoff);
    if(!showoff.alive) { // Y cuando se muere...
      changeGen(); // ...se ejecuta el cambio de generación (esta línea es importante porque aquí se activa el algo. gen.)
    }
    showoffFood.update(showoff);
    showoffFood.render();
  }

  // Este es el cambio de generación. Aquí se ejecuta el algoritmo genético
  void changeGen() {
    foods.restart();
    Generation oldGen = gens.get(cg);
    float totFitness = 0;

    for(Snake snake : oldGen.snakes) {
      totFitness = totFitness + snake.fitness; // Se obtiene la suma de los fitness de las serpientes de la última gen.
    }
    // println(totFitness);

    cg = cg + 1; // Aumentar el número de la gen.
    gens.add(new Generation()); // Añadir la nueva gen. al arreglo

    Generation newGen = gens.get(cg);
    // Ahora se producen las serpientes de la nueva gen.
    for(Snake childSnake : newGen.snakes) { // Para cada una de las serpientes...
      /* ...se eligen los padres usando las serpientes de la última gen. Para esto
         se eligen al azar los padres, con cada padre teniendo una probabilidad de ser
         elegido proporcional a su fitness relativo a la suma total */

      // Se elige el primer padre
      Snake firstParent = new Snake(0);
      float randomi = random(totFitness);
      float fitnessCount = 0;
      // Algoritmo de probabilidad
      for(Snake snake : oldGen.snakes) {
        fitnessCount = fitnessCount + snake.fitness;
        if(randomi < fitnessCount) {
          firstParent = snake;
          break;
        }
      }
      // Se elige el segundo padre
      Snake secondParent = new Snake(0);
      randomi = random(totFitness);
      fitnessCount = 0;
      // Algoritmo de probabilidad
      for(Snake snake : oldGen.snakes) {
        fitnessCount = fitnessCount + snake.fitness;
        if(randomi < fitnessCount) {
          secondParent = snake;
          break;
        }
      }
      
      childSnake.brain = firstParent.brain.crossover(secondParent.brain); // Combinar los cerebros de los dos padres
      childSnake.brain.mutate(mutRate); // Y mutarlo un poco, para añadir variación
    }

    gens.set(cg, newGen); // set la nueva generación, ya esta estando creada
    gens.set(cg - 1, null); // Remover la generación anterior, para liberar memoria (esta línea se podría eliminar)
  }
}
