class Controller {
  boolean isHuman = false;
  boolean pressedKey = false;

  float[] frame(Snake snake) {
    float[] vision = new float[24];
    //look left
    float[] tempValues = lookInDirection(new PVector(-scl, 0), snake);
    vision[0] = tempValues[0];
    vision[1] = tempValues[1];
    vision[2] = tempValues[2];
    //look left/up  
    tempValues = lookInDirection(new PVector(-scl, -scl), snake);
    vision[3] = tempValues[0];
    vision[4] = tempValues[1];
    vision[5] = tempValues[2];
    //look up
    tempValues = lookInDirection(new PVector(0, -scl), snake);
    vision[6] = tempValues[0];
    vision[7] = tempValues[1];
    vision[8] = tempValues[2];
    //look up/right
    tempValues = lookInDirection(new PVector(scl, -scl), snake);
    vision[9] = tempValues[0];
    vision[10] = tempValues[1];
    vision[11] = tempValues[2];
    //look right
    tempValues = lookInDirection(new PVector(scl, 0), snake);
    vision[12] = tempValues[0];
    vision[13] = tempValues[1];
    vision[14] = tempValues[2];
    //look right/down
    tempValues = lookInDirection(new PVector(scl, scl), snake);
    vision[15] = tempValues[0];
    vision[16] = tempValues[1];
    vision[17] = tempValues[2];
    //look down
    tempValues = lookInDirection(new PVector(0, scl), snake);
    vision[18] = tempValues[0];
    vision[19] = tempValues[1];
    vision[20] = tempValues[2];
    //look down/left
    tempValues = lookInDirection(new PVector(-scl, scl), snake);
    vision[21] = tempValues[0];
    vision[22] = tempValues[1];
    vision[23] = tempValues[2];

    return vision;
  }

  float[] frameASD(Snake snake) {
    float[] vision = new float[32];
    if(snake.vel.x == scl) {
      vision[0] = 1;
    } else if(snake.vel.x == -scl) {
      vision[0] = -1;
    } else {
      vision[0] = 0;
    }
    if(snake.vel.y == scl) {
      vision[1] = 1;
    } else if(snake.vel.y == -scl) {
      vision[1] = -1;
    } else {
      vision[1] = 0;
    }

    vision[2] = (snake.pos[0].x/scl)/horsqrs;
    vision[3] = (snake.pos[0].y/scl)/versqrs;

    if(snake.index == -1) {
      vision[4] = (population.showoffFood.pos.x/scl)/horsqrs;
      vision[5] = (population.showoffFood.pos.y/scl)/versqrs;
    } else {
      vision[6] = (foods.foods[snake.index].pos.x/scl)/horsqrs;
      vision[7] = (foods.foods[snake.index].pos.y/scl)/versqrs;
    }
    
    //look left
    float[] tempValues = lookInDirection(new PVector(-scl, 0), snake);
    vision[8] = tempValues[0];
    vision[9] = tempValues[1];
    vision[10] = tempValues[2];
    //look left/up  
    tempValues = lookInDirection(new PVector(-scl, -scl), snake);
    vision[11] = tempValues[0];
    vision[12] = tempValues[1];
    vision[13] = tempValues[2];
    //look up
    tempValues = lookInDirection(new PVector(0, -scl), snake);
    vision[14] = tempValues[0];
    vision[15] = tempValues[1];
    vision[16] = tempValues[2];
    //look up/right
    tempValues = lookInDirection(new PVector(scl, -scl), snake);
    vision[17] = tempValues[0];
    vision[18] = tempValues[1];
    vision[19] = tempValues[2];
    //look right
    tempValues = lookInDirection(new PVector(scl, 0), snake);
    vision[20] = tempValues[0];
    vision[21] = tempValues[1];
    vision[22] = tempValues[2];
    //look right/down
    tempValues = lookInDirection(new PVector(scl, scl), snake);
    vision[23] = tempValues[0];
    vision[24] = tempValues[1];
    vision[25] = tempValues[2];
    //look down
    tempValues = lookInDirection(new PVector(0, scl), snake);
    vision[26] = tempValues[0];
    vision[27] = tempValues[1];
    vision[28] = tempValues[2];
    //look down/left
    tempValues = lookInDirection(new PVector(-scl, scl), snake);
    vision[29] = tempValues[0];
    vision[30] = tempValues[1];
    vision[31] = tempValues[2];

    return vision;
  }


  float[] lookInDirection(PVector direction, Snake cSnake) {

    //set up a temp array to hold the values that are going to be passed to the main vision array
    float[] visionInDirection = new float[3];

    PVector position = new PVector(cSnake.pos[0].x, cSnake.pos[0].y);//the position where we are currently looking for food or tail or wall
    boolean foodIsFound = false;//true if the food has been located in the direction looked
    boolean tailIsFound = false;//true if the tail has been located in the direction looked 
    float distance = 0;
    //move once in the desired direction before starting 
    position.add(direction);
    distance +=1;

    //look in the direction until you reach a wall
    while (!(position.x < 0 || position.y < 0 || position.x >= horsqrs*scl || position.y >= versqrs*scl)) {

      //check for food at the position
      if(cSnake.index == -1) {
        if (!foodIsFound && position.x == population.showoffFood.pos.x && position.y == population.showoffFood.pos.y) {
          visionInDirection[0] = 1;
          foodIsFound = true; // dont check if food is already found
        }
      } else {
        if (!foodIsFound && position.x == foods.foods[cSnake.index].pos.x && position.y == foods.foods[cSnake.index].pos.y) {
          visionInDirection[0] = 1;
          foodIsFound = true; // dont check if food is already found
        }
      }

      //check for tail at the position
      if (!tailIsFound && cSnake.collidedBody(position.x, position.y)) {
        visionInDirection[1] = 1/distance;
        tailIsFound = true; // dont check if tail is already found
      }

      //look further in the direction
      position.add(direction);
      distance +=1;
    }

    //set the distance to the wall
    visionInDirection[2] = 1/distance;

    return visionInDirection;
  }
  
  void play(Snake snake) {
    float[] moves = snake.brain.output(frame(snake));
    // println(frame());
    // println("===============");

    float max = moves[0];
    for(int i = 1; i < moves.length; i++) {
      if(moves[i] > max) {
        max = moves[i];
      }
    }
    int indexOfMax = 0;
    for(int i = 0; i < moves.length; i++) {
      if(moves[i] == max) {
        indexOfMax = i;
      }
    }
    
    //Up
    if (indexOfMax == 1) {
      //This is for the snake to be able to move backwards except if it has length 1 or 2 to suicide to train
      if(snake.pos.length > 2) {
        snake.vel.x = 0;
        snake.vel.y = -scl;
      } else {
        if(snake.vel.y != scl) {
          snake.vel.x = 0;
          snake.vel.y = -scl;
        }
      }
    //Down
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
    //Left
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
    //Right
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
    pressedKey = true;
  }
}
