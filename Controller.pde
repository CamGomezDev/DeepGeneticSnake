class Controller {
  boolean isHuman = false;
  boolean pressedKey = false;

  // Se elige la jugada y dirección de la serpiente en base a los valores generados por su cerebro
  void play(Snake snake) {
    /* Al pasar el frame (la observación) por el cerebro se devuelven
       los valores correspondientes a las direcciones de movimiento */
    float[] moves = snake.brain.output(frame(snake));

    // Se eligen el valor max. y su correspondiente índice
    float max = moves[0];
    int indexOfMax = 0;
    for(int i = 1; i < moves.length; i++) {
      if(moves[i] > max) {
        max = moves[i];
        indexOfMax = i;
      }
    }
    
    // Se usa el índice para elegir la nueva dirección de la velocidad de la serpiente

    // Hacia arriba
    if (indexOfMax == 1) {
      /* El doble if es para que la serpiente se pueda devolver y morir chocando
         su propio cuerpo si tiene un tamaño menor a 2, así aprende a evitarlo. */
      if(snake.pos.length > 2) {
        snake.vel.x = 0;
        snake.vel.y = -scl;
      } else {
        if(snake.vel.y != scl) { // Si no se está moviendo hacia abajo
          snake.vel.x = 0;
          snake.vel.y = -scl; // ir hacia arriba
        }
      }
    // Hacia abajo
    } else if (indexOfMax == 3) {
      if(snake.pos.length > 2) {
        snake.vel.x = 0;
        snake.vel.y = scl;
      } else {
        if(snake.vel.y != -scl) {
          snake.vel.x = 0;
          snake.vel.y = scl;
        }
      }
    // Hacia la izquierda
    } else if (indexOfMax == 2) {
      if(snake.pos.length > 2) {
        snake.vel.x = -scl;
        snake.vel.y = 0;
      } else {
        if(snake.vel.x != scl) {
          snake.vel.x = -scl;
          snake.vel.y = 0;
        }
      }
    // Hacia la derecha
    } else if (indexOfMax == 0) {
      if(snake.pos.length > 2) {
        snake.vel.x = scl;
        snake.vel.y = 0;
      } else {
        if(snake.vel.x != -scl) {
          snake.vel.x = scl;
          snake.vel.y = 0;
        }
      }
    }
  }

  // Aquí se calcula la distancia en todas las ocho direcciones a la serpiente
  float[] frame(Snake snake) {
    float[] vision = new float[24];
    //Izquierda
    float[] tempValues = lookInDirection(new PVector(-scl, 0), snake);
    vision[0] = tempValues[0];
    vision[1] = tempValues[1];
    vision[2] = tempValues[2];
    //Izquierda/arriba
    tempValues = lookInDirection(new PVector(-scl, -scl), snake);
    vision[3] = tempValues[0];
    vision[4] = tempValues[1];
    vision[5] = tempValues[2];
    //Arriba
    tempValues = lookInDirection(new PVector(0, -scl), snake);
    vision[6] = tempValues[0];
    vision[7] = tempValues[1];
    vision[8] = tempValues[2];
    //Arriba/derecha
    tempValues = lookInDirection(new PVector(scl, -scl), snake);
    vision[9] = tempValues[0];
    vision[10] = tempValues[1];
    vision[11] = tempValues[2];
    //Derecha
    tempValues = lookInDirection(new PVector(scl, 0), snake);
    vision[12] = tempValues[0];
    vision[13] = tempValues[1];
    vision[14] = tempValues[2];
    //Derecha/abajo
    tempValues = lookInDirection(new PVector(scl, scl), snake);
    vision[15] = tempValues[0];
    vision[16] = tempValues[1];
    vision[17] = tempValues[2];
    //Abajo
    tempValues = lookInDirection(new PVector(0, scl), snake);
    vision[18] = tempValues[0];
    vision[19] = tempValues[1];
    vision[20] = tempValues[2];
    //Abajo/izquierda
    tempValues = lookInDirection(new PVector(-scl, scl), snake);
    vision[21] = tempValues[0];
    vision[22] = tempValues[1];
    vision[23] = tempValues[2];

    return vision;
  }

  float[] lookInDirection(PVector direction, Snake cSnake) {

    // Crear un arreglo temporal para tener los valores que van a ser pasados al arreglo de visión principal
    float[] visionInDirection = new float[3];

    PVector position = new PVector(cSnake.pos[0].x, cSnake.pos[0].y);//La posición desde la que estamos buscando comida, cola o pared
    boolean foodIsFound = false;//verdad si la comida ha sido encontrada en la dirección mirada
    boolean tailIsFound = false;//verdad si la cola ha sido encontrada en la dirección mirada
    float distance = 0;
    // Moverse una vez en la dirección deseada antes de empezar
    position.add(direction);
    distance +=1;

    // Mirar en la dirección hasta llegar a una pared
    while (!(position.x < 0 || position.y < 0 || position.x >= horsqrs*scl || position.y >= versqrs*scl)) {

      // Buscar comida en la posición
      if(cSnake.index == -1) {
        if (!foodIsFound && position.x == population.showoffFood.pos.x && position.y == population.showoffFood.pos.y) {
          visionInDirection[0] = 1;
          foodIsFound = true; // Si ya encontró la comida no revisar
        }
      } else {
        if (!foodIsFound && position.x == foods.foods[cSnake.index].pos.x && position.y == foods.foods[cSnake.index].pos.y) {
          visionInDirection[0] = 1;
          foodIsFound = true; // Si ya encontró la comida no revisar
        }
      }

      // Buscar la cola en la posición
      if (!tailIsFound && cSnake.collidedBody(position.x, position.y)) {
        visionInDirection[1] = 1/distance;
        tailIsFound = true; // Si ya encontró la cola no revisar
      }

      // Seguir buscando en la dirección
      position.add(direction);
      distance +=1;
    }

    // Establecer la distancia a la pared
    visionInDirection[2] = 1/distance;

    return visionInDirection;
  }
}
