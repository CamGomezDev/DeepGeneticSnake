// Clase con la comida, nada especial
class Food {
  PVector pos = new PVector(floor(random(horsqrs))*scl, floor(random(versqrs))*scl);
  
  void render() {
    fill(foodcol);
    noStroke();
    rect(pos.x + 1, pos.y + 1, scl - 1, scl - 1);
  }
  
  // Si la serpiente atrapa la comida, que aparezca en otra parte
  void update(Snake snake) {
    if(snake.wasInFoodPos()) {
      boolean match = true;
      while(match) {
        match = false;
        pos.x = floor(random(horsqrs))*scl; 
        pos.y = floor(random(versqrs))*scl;
        // Asegurarse de que la nueva posición no esté en el cuerpo de la serpiente
        for(int i = 0; i < snake.pos.length; i++) {
          if(pos.x == snake.pos[i].x && pos.y == snake.pos[i].y) {
            match = true;
          }      
        }
      }
    }
  }
}