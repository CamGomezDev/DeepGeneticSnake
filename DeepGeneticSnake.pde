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

// Colores
int bgcol = color(29,30,58);
int gridcol = color(113,112,110);
int snakecol = color(0,204,102);
int foodcol = color(255,78,96);
int panelcol = 175;

// void settings() {
//   size(scl*horsqrs + 1 + panelWidth, scl*versqrs + 1);
// }

void setup() {
  background(bgcol);
  /* Nota: es posible que esta pantalla completa solo se vea bien
     en resoluciones 1364x768 */
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

// Ciclo de juego
void draw() {
  pushMatrix();
  translate(50,6); // dibujar la cuadrícula en el centro
  if(!gamePaused) {
    frameRate(fps);
    world.render(); // Dibujar cuadrícula
    population.update(); // Mover todas las serpientes y actualizar generación
    foods.update(); // Actualizar posición de las comidas
  }
  popMatrix();
  world.renderPanel(); // Dibujar el panel
}

// Reiniciar todo
void restart() {
  pushMatrix();
  translate(50,6);
  population = new Population();
  world = new World();
  controller = new Controller();
  foods = new Foods();
  popMatrix();
}

/* Botones para interactuar, k: pausar, j: desacelerar, l: acelerar, 
   r: renderizar todas las serpientes o no, s: disminuir mutRate,
   d: aumentar mutRate, f: ocultar o no letrero con teclas, q: reiniciar */
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