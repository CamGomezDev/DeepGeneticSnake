int scl = 28;
// int scl = 16;
int horsqrs = 36;
int versqrs = 27;
int panelWidth = 240;
int fps = 300;
int currentScore = 0;
float mutRate = 0.05;
String speedText = "20x";
World world;
Population population;
Controller controller;
Generation cGen;
Foods foods;
boolean gamePaused;
boolean renderingAll;
boolean hideKeys = false;

// int bgcol = color(255,229,202);
// int gridcol = color(140);
// int snakecol = color(44,188,178);
// int foodcol = color(212,78,40);
// int panelcol = color(225,198,153);

int bgcol = color(29,30,58);
int gridcol = color(113,112,110);
int snakecol = color(0,204,102);
int foodcol = color(255,78,96);
int panelcol = 175;

// int bgcol = color(255,178,102);
// int gridcol = color(113,112,110);
// int snakecol = color(53,168,73);
// int foodcol = color(95,44,131);
// int panelcol = 175;

// void settings() {
//   size(scl*horsqrs + 1 + panelWidth, scl*versqrs + 1);
// }

void setup() {
  background(bgcol);
  fullScreen();
  pushMatrix();
  translate(50,6);
  gamePaused = false;
  frameRate(fps);
  population = new Population();
  world = new World();
  controller = new Controller();
  foods = new Foods();
  popMatrix();
  world.renderPanel();
}

void draw() {
  pushMatrix();
  translate(50,6);
  if(!gamePaused) {
    frameRate(fps);
    world.render();
    population.update();
    foods.update();
  }
  popMatrix();
  world.renderPanel();
}

void restart() {
  pushMatrix();
  translate(50,6);
  population = new Population();
  world = new World();
  controller = new Controller();
  foods = new Foods();
  popMatrix();
}


class Foods {
  Food[] foods = new Food[population.gens.get(population.cg).ns];

  Foods() {
    for (int i = 0; i < foods.length; ++i) {
      foods[i] = new Food();
    }
  }

  void update() {
    Generation gen = population.gens.get(population.cg);
    for(int i = 0; i < foods.length; ++i) {
      foods[i].update(gen.snakes[i]);
      if(false) {
        foods[i].render();
      } else {
        if(i == population.cshowfI && !gen.showingBestSnake) {
          foods[i].render();
        }
      }
    }
  }

  void restart() {
    for (int i = 0; i < foods.length; ++i) {
      foods[i] = new Food();
    }
  }
}

class Food {
  PVector pos = new PVector(floor(random(horsqrs))*scl, floor(random(versqrs))*scl);
  
  void render() {
    fill(foodcol);
    noStroke();
    rect(pos.x + 1, pos.y + 1, scl - 1, scl - 1);
  }
  
  void update(Snake snake) {
    if(snake.wasInFoodPos()) {
      boolean match = true;
      while(match) {
        match = false;
        pos.x = floor(random(horsqrs))*scl; 
        pos.y = floor(random(versqrs))*scl;
        for(int i = 0; i < snake.pos.length; i++) {
          if(pos.x == snake.pos[i].x && pos.y == snake.pos[i].y) {
            match = true;
          }      
        }
      }
    }
  }
}


void keyPressed() {
  if(key == 'k') {
    gamePaused = !gamePaused;
  }
  if(key == 'l') {
    switch (fps) {
      case 15 :
        fps = 30;
        speedText = "2x";
      break;
      case 30 :
        fps = 60;
        speedText = "4x";
      break;
      case 60 :
        fps = 150;
        speedText = "10x";
      break;	
      case 150 :
        fps = 300;
        speedText = "20x";
      break;	
      default :
      break;	
    }
  }
  if(key == 'j') {
    switch (fps) {
      case 300 :
        fps = 150;
        speedText = "10x";
      break;
      case 150 :
        fps = 60;
        speedText = "4x";
      break;
      case 60 :
        fps = 30;
        speedText = "2x";
      break;	
      case 30 :
        fps = 15;
        speedText = "1x";
      break;	
      default :
      break;	
    }
  }
  if(key == 's' && round(mutRate*100) > 0) {
    mutRate -= 0.01;
  }
  if(key == 'd' && round(mutRate*100) < 100) {
    mutRate += 0.01;
  }
  if(key == 'r') {
    renderingAll = !renderingAll;
  }
  if(key == 'f') {
    hideKeys = !hideKeys;
  }
  if(key == 'q') {
    restart();
  }
}