public class Snake {
  NeuralNet brain = new NeuralNet(24, 24, 4);
  int index;
  int lifetime = 0;
  int incBy = 0;
  int relLength = 1;
  float fitness = 0;
  float[] fitnesses;
  float fitnessExtra = 1;
  int sinceFood = 0;
  float probs = 0;
  boolean alive = true;
  boolean justAte = false;
  boolean justDied = false;
  boolean isShowoff = false;

  PVector[] pos = new PVector[1];
  PVector prevHead = new PVector(0, 0);
  PVector vel;

  public Snake (int indexi, int runs) {
    fitnesses = new float[runs];
    for(int i = 0; i < runs; i++) {
      fitnesses[i] = 0;
    }
    if(indexi == -1) {
      isShowoff = true;
    }
    index = indexi;
    startVel();
    
    pos[0] = new PVector(scl*floor(horsqrs/2), scl*floor(versqrs/2));
    square(pos[0].x, pos[0].y);
  }

  void startVel() {
    float rel = random(4); //randomVel = rel
    vel = new PVector(0, 0);
    if(rel<1){vel.x=-1;vel.y=0;}else if(rel>=1&&rel<2){vel.x=1;vel.y=0;}else if(rel>=2&&rel<3){vel.x=0;vel.y=-1;}else{vel.x=0;vel.y=1;}
    vel.mult(scl);
  }

  void update() {
    lifetime = lifetime + 1;
    prevHead = pos[0].get();
    pos[0].add(vel);
    if(wasInFoodPos()) {
      relLength += 1;
      if(pos.length < 10) {
        incBy += 1; //increase by when is short
      } else {
        incBy += 1; //increase by when is long
      }
    }
    if(incBy > 0) {
      plusOne();
      incBy -= 1;
    }
    if(!world.isInsideBoundaries(pos[0].x, pos[0].y)) {
      died();
    } else {
      move();
      if(collidedBody(pos[0].x, pos[0].y)) {
        died();
      }
    }
    sinceFood = sinceFood + 1;
    if(pos.length < 3 && sinceFood > 120) {
      died();
    } else if(pos.length >= 3 && sinceFood > 300) {
      died();
    }
  }

  void render() {
    for(int i = 0; i < pos.length; i++) {
      square(pos[i].x, pos[i].y);
    }
  }

  void move() {
    PVector previous = prevHead.get();
    PVector previousCopy = prevHead.get(); 
    for(int i = 1; i < pos.length; i++) {
      previous = pos[i];
      pos[i] = previousCopy;
      previousCopy = previous;
    }
  }

  boolean wasInFoodPos() {
    if(!isShowoff) {
      if(pos[0].x == foods.foods[index].pos.x && pos[0].y == foods.foods[index].pos.y) {
        return true;
      }
    } else {
      if(pos[0].x == population.showoffFood.pos.x && pos[0].y == population.showoffFood.pos.y) {
        return true;
      }
    }
    return false;
  }

  void plusOne() {
    if(sinceFood != 0) {
      fitnessExtra += sinceFood;
    }
    sinceFood = 0;
    if(pos.length == 1) {
      pos = (PVector[])append(pos, new PVector(prevHead.x, prevHead.y));
    } else {
      pos = (PVector[])append(pos, new PVector(pos[pos.length - 1].x, pos[pos.length - 1].y));
    }
  }
  
  void died() {
    alive = false;
    justDied = true;
    for(int i = 0; i < fitnesses.length; i++) {
      if(fitnesses[i] == 0) {
        if(relLength < 10) {
          fitnesses[i] = lifetime*lifetime*floor(pow(2, relLength));
        } else {
          fitnesses[i] = lifetime*lifetime*floor(pow(2, 10))*(relLength - 9);
        }
        break;
      }
    }
    for (float fit : fitnesses) {
      fitness += fit;
    }
    fitness = fitness/fitnesses.length;

    pos[0].sub(vel);
  }
  
  boolean justDied() {
    if(justDied) {
      justDied = false;
      return true;
    }
    return false;
  }
  
  boolean collidedBody(float x, float y) {
    for(int i = 2; i < pos.length; i++) {
      if(x == pos[i].x && y == pos[i].y) {
        return true;
      }
    }
    return false;
  }
  
  void square(float x, float y) {
    noStroke();
    fill(snakecol);
    rect(x + 1, y + 1, scl - 1, scl - 1);
  }
}
