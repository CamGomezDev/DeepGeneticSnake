public class Snake {
  NeuralNet brain = new NeuralNet(24, 24, 4); // El cerebro es la red neuronal
  int index;
  int lifetime = 0;
  int incBy = 0;
  float fitness = 0;
  int sinceFood = 0;
  float probs = 0;
  boolean alive = true;
  boolean justAte = false;
  boolean justDied = false;
  boolean isShowoff = false;

  PVector[] pos = new PVector[1];
  PVector prevHead = new PVector(0, 0);
  PVector vel;

  public Snake (int indexi) {
    if(indexi == -1) {
      isShowoff = true; // simple identificador, en caso de que esta sea la serpiente para mostrar
    }
    index = indexi;
    startVel();
    
    pos[0] = new PVector(scl*floor(horsqrs/2), scl*floor(versqrs/2));
    square(pos[0].x, pos[0].y);
  }

  // Iniciar una velocidad al azar, no siempre en la misma dirección
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
    if(wasInFoodPos()) { // Si acaba de comer
      plusOne(); // Aumentar tamaño
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
    if(pos.length < 3 && sinceFood > 120) { // Morir si ha pasado 120 frames sin comer y su tamaño es menor a 3
      died();
    } else if(pos.length >= 3 && sinceFood > 300) { // Morirs si ha pasado 300 frames sin comer y su tamaño es mayor o igual a 3
      died();
    }
  }

  void render() {
    for(int i = 0; i < pos.length; i++) {
      square(pos[i].x, pos[i].y); // dibujar todos los cuadrados
    }
  }

  // Mover la serpiente
  void move() {
    PVector previous = prevHead.get();
    PVector previousCopy = prevHead.get(); 
    for(int i = 1; i < pos.length; i++) {
      previous = pos[i];
      pos[i] = previousCopy;
      previousCopy = previous;
    }
  }

  // Comprobar si la serpiente estuvo en la posición de la comida
  boolean wasInFoodPos() {
    // Recordar que showoff es la mejor serpiente del grupo siendo mostrada
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

  // Aumentar el tamaño de la serpiente
  void plusOne() {
    sinceFood = 0;
    if(pos.length == 1) {
      pos = (PVector[])append(pos, new PVector(prevHead.x, prevHead.y));
    } else {
      pos = (PVector[])append(pos, new PVector(pos[pos.length - 1].x, pos[pos.length - 1].y));
    }
  }
  
  // Morir y calcular el fitness
  void died() {
    alive = false;
    justDied = true;
    if(pos.length < 10) { // pos.length es el tamaño de la serpiente
      fitness = lifetime*lifetime*floor(pow(2, pos.length)); // función fitness 1
    } else {
      fitness = lifetime*lifetime*floor(pow(2, 10))*(pos.length - 9); // función fitness 2
    }

    pos[0].sub(vel);
  }
  
  boolean justDied() {
    if(justDied) {
      justDied = false;
      return true;
    }
    return false;
  }

  // Comprobar si chocó con el cuerpo
  boolean collidedBody(float x, float y) {
    for(int i = 2; i < pos.length; i++) {
      if(x == pos[i].x && y == pos[i].y) {
        return true;
      }
    }
    return false;
  }

  // Dibujar un cuadrado en las coords. x e y 
  void square(float x, float y) {
    noStroke();
    fill(snakecol);
    rect(x + 1, y + 1, scl - 1, scl - 1);
  }
}
