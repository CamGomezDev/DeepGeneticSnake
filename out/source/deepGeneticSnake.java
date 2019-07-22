import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class deepGeneticSnake extends PApplet {

int scl = 28;
// int scl = 16;
int horsqrs = 36;
int versqrs = 27;
int panelWidth = 240;
int fps = 300;
int currentScore = 0;
float mutRate = 0.05f;
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

public void setup() {
  background(bgcol);
  /* Nota: es posible que esta pantalla completa solo se vea bien
     en resoluciones 1364x768 */
  
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
public void draw() {
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
public void restart() {
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
public void keyPressed() {
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
    mutRate -= 0.01f;
  }
  if(key == 'd' && round(mutRate*100) < 100) {
    mutRate += 0.01f;
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
class Controller {
  boolean isHuman = false;
  boolean pressedKey = false;

  // Se elige la jugada y dirección de la serpiente en base a los valores generados por su cerebro
  public void play(Snake snake) {
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
  public float[] frame(Snake snake) {
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

  public float[] lookInDirection(PVector direction, Snake cSnake) {

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
// Clase con la comida, nada especial
class Food {
  PVector pos = new PVector(floor(random(horsqrs))*scl, floor(random(versqrs))*scl);
  
  public void render() {
    fill(foodcol);
    noStroke();
    rect(pos.x + 1, pos.y + 1, scl - 1, scl - 1);
  }
  
  // Si la serpiente atrapa la comida, que aparezca en otra parte
  public void update(Snake snake) {
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
// Todas las comidas para todas las serpientes entrenándose en una gen.
class Foods {
  Food[] foods = new Food[population.gens.get(population.cg).ns];

  Foods() {
    for (int i = 0; i < foods.length; ++i) {
      foods[i] = new Food();
    }
  }


  public void update() {
    Generation gen = population.gens.get(population.cg);
    for(int i = 0; i < foods.length; ++i) {
      foods[i].update(gen.snakes[i]); // actualizar la posición de todas las comidas
      if(i == population.cshowfI && !gen.showingBestSnake) {
        foods[i].render(); // pero solo mostrar la comida de la serpiente que se está mostrando
      }
    }
  }

  public void restart() {
    for (int i = 0; i < foods.length; ++i) {
      foods[i] = new Food();
    }
  }
}
class Generation {
  boolean showingBestSnake = false;
  int ns = 2000; // Número de serpientes
  Snake[] snakes = new Snake[ns];
  int cs; // Serpiente actual (current snake)

  Generation() {
    for (int i = 0; i < snakes.length; ++i) {
      snakes[i] = new Snake(i);
    }
    cs = 0;
  }
}
class Matrix {
  
  //local variables
  int rows;
  int cols;
  float[][] matrix;
  
  //---------------------------------------------------------------------------------------------------------------------------------------------------------  
  //constructor
  Matrix(int r, int c) {
    rows = r;
    cols = c;
    matrix = new float[rows][cols];
  }
  
  //---------------------------------------------------------------------------------------------------------------------------------------------------------  
  //constructor from 2D array
  Matrix(float[][] m) {
    matrix = m;
    cols = m.length;
    rows = m[0].length;
  }
  
  //---------------------------------------------------------------------------------------------------------------------------------------------------------  
  //print matrix
  public void output() {
    for (int i =0; i<rows; i++) {
      for (int j = 0; j<cols; j++) {
        print(matrix[i][j] + "  ");
      }
      println(" ");
    }
    println();
  }
  //---------------------------------------------------------------------------------------------------------------------------------------------------------  
  
  //multiply by scalar
  public void multiply(float n ) {

    for (int i =0; i<rows; i++) {
      for (int j = 0; j<cols; j++) {
        matrix[i][j] *= n;
      }
    }
  }

//---------------------------------------------------------------------------------------------------------------------------------------------------------  
  //return a matrix which is this matrix dot product parameter matrix 
  public Matrix dot(Matrix n) {
    Matrix result = new Matrix(rows, n.cols);
   
    if (cols == n.rows) {
      //for each spot in the new matrix
      for (int i =0; i<rows; i++) {
        for (int j = 0; j<n.cols; j++) {
          float sum = 0;
          for (int k = 0; k<cols; k++) {
            sum+= matrix[i][k]*n.matrix[k][j];
          }
          result.matrix[i][j] = sum;
        }
      }
    }

    return result;
  }
//---------------------------------------------------------------------------------------------------------------------------------------------------------  
  //set the matrix to random ints between -1 and 1
  public void randomize() {
    for (int i =0; i<rows; i++) {
      for (int j = 0; j<cols; j++) {
        matrix[i][j] = random(-1, 1);
      }
    }
  }

//---------------------------------------------------------------------------------------------------------------------------------------------------------  
  //add a scalar to the matrix
  public void Add(float n ) {
    for (int i =0; i<rows; i++) {
      for (int j = 0; j<cols; j++) {
        matrix[i][j] += n;
      }
    }
  }
//---------------------------------------------------------------------------------------------------------------------------------------------------------  
  ///return a matrix which is this matrix + parameter matrix
  public Matrix add(Matrix n ) {
    Matrix newMatrix = new Matrix(rows, cols);
    if (cols == n.cols && rows == n.rows) {
      for (int i =0; i<rows; i++) {
        for (int j = 0; j<cols; j++) {
          newMatrix.matrix[i][j] = matrix[i][j] + n.matrix[i][j];
        }
      }
    }
    return newMatrix;
  }
//---------------------------------------------------------------------------------------------------------------------------------------------------------  
  //return a matrix which is this matrix - parameter matrix
  public Matrix subtract(Matrix n ) {
    Matrix newMatrix = new Matrix(cols, rows);
    if (cols == n.cols && rows == n.rows) {
      for (int i =0; i<rows; i++) {
        for (int j = 0; j<cols; j++) {
          newMatrix.matrix[i][j] = matrix[i][j] - n.matrix[i][j];
        }
      }
    }
    return newMatrix;
  }
//---------------------------------------------------------------------------------------------------------------------------------------------------------  
  //return a matrix which is this matrix * parameter matrix (element wise multiplication)
  public Matrix multiply(Matrix n ) {
    Matrix newMatrix = new Matrix(rows, cols);
    if (cols == n.cols && rows == n.rows) {
      for (int i =0; i<rows; i++) {
        for (int j = 0; j<cols; j++) {
          newMatrix.matrix[i][j] = matrix[i][j] * n.matrix[i][j];
        }
      }
    }
    return newMatrix;
  }
//---------------------------------------------------------------------------------------------------------------------------------------------------------  
  //return a matrix which is the transpose of this matrix
  public Matrix transpose() {
    Matrix n = new Matrix(cols, rows);
    for (int i =0; i<rows; i++) {
      for (int j = 0; j<cols; j++) {
        n.matrix[j][i] = matrix[i][j];
      }
    }
    return n;
  }
//---------------------------------------------------------------------------------------------------------------------------------------------------------  
  //Creates a single column array from the parameter array
  public Matrix singleColumnMatrixFromArray(float[] arr) {
    Matrix n = new Matrix(arr.length, 1);
    for (int i = 0; i< arr.length; i++) {
      n.matrix[i][0] = arr[i];
    }
    return n;
  }
  //---------------------------------------------------------------------------------------------------------------------------------------------------------  
  //sets this matrix from an array
  public void fromArray(float[] arr) {
    for (int i = 0; i< rows; i++) {
      for (int j = 0; j< cols; j++) {
        matrix[i][j] =  arr[j+i*cols];
      }
    }
  }
//---------------------------------------------------------------------------------------------------------------------------------------------------------    
  //returns an array which represents this matrix
  public float[] toArray() {
    float[] arr = new float[rows*cols];
    for (int i = 0; i< rows; i++) {
      for (int j = 0; j< cols; j++) {
        arr[j+i*cols] = matrix[i][j];
      }
    }
    return arr;
  }

//---------------------------------------------------------------------------------------------------------------------------------------------------------  
  //for ix1 matrixes adds one to the bottom
  public Matrix addBias() {
    Matrix n = new Matrix(rows+1, 1);
    for (int i = 0; i<rows; i++) {
      n.matrix[i][0] = matrix[i][0];
    }
    n.matrix[rows][0] = 1;
    return n;
  }
//---------------------------------------------------------------------------------------------------------------------------------------------------------  
  //applies the activation function(sigmoid) to each element of the matrix
  public Matrix activate() {
    Matrix n = new Matrix(rows, cols);
    for (int i =0; i<rows; i++) {
      for (int j = 0; j<cols; j++) {
        n.matrix[i][j] = sigmoid(matrix[i][j]);
      }
    }
    return n;
  }
  
//---------------------------------------------------------------------------------------------------------------------------------------------------------    
  //sigmoid activation function
  public float sigmoid(float x) {
    float y = 1 / (1 + pow((float)Math.E, -x));
    return y;
  }
  //returns the matrix that is the derived sigmoid function of the current matrix
  public Matrix sigmoidDerived() {
    Matrix n = new Matrix(rows, cols);
    for (int i =0; i<rows; i++) {
      for (int j = 0; j<cols; j++) {
        n.matrix[i][j] = (matrix[i][j] * (1- matrix[i][j]));
      }
    }
    return n;
  }

//---------------------------------------------------------------------------------------------------------------------------------------------------------  
  //returns the matrix which is this matrix with the bottom layer removed
  public Matrix removeBottomLayer() {
    Matrix n = new Matrix(rows-1, cols);      
    for (int i =0; i<n.rows; i++) {
      for (int j = 0; j<cols; j++) {
        n.matrix[i][j] = matrix[i][j];
      }
    }
    return n;
  }
//---------------------------------------------------------------------------------------------------------------------------------------------------------  
  //Mutation function for genetic algorithm 
  
  public void mutate(float mutationRate) {
    
    //for each element in the matrix
    for (int i =0; i<rows; i++) {
      for (int j = 0; j<cols; j++) {
        float rand = random(1);
        if (rand<mutationRate) {//if chosen to be mutated
          matrix[i][j] += randomGaussian()/5;//add a random value to it(can be negative)
          
          //set the boundaries to 1 and -1
          if (matrix[i][j]>1) {
            matrix[i][j] = 1;
          }
          if (matrix[i][j] <-1) {
            matrix[i][j] = -1;
          }
        }
      }
    }
  }
//---------------------------------------------------------------------------------------------------------------------------------------------------------  
  //returns a matrix which has a random number of values from this matrix and the rest from the parameter matrix
  public Matrix crossover(Matrix partner) {
    Matrix child = new Matrix(rows, cols);
    
    //pick a random point in the matrix
    int randC = floor(random(cols));
    int randR = floor(random(rows));
    for (int i =0; i<rows; i++) {
      for (int j = 0; j<cols; j++) {

        if ((i< randR)|| (i==randR && j<=randC)) { //if before the random point then copy from this matric
          child.matrix[i][j] = matrix[i][j];
        } else { //if after the random point then copy from the parameter array
          child.matrix[i][j] = partner.matrix[i][j];
        }
      }
    }
    return child;
  }
//---------------------------------------------------------------------------------------------------------------------------------------------------------  
  //return a copy of this matrix
  public Matrix clone() {
    Matrix clone = new  Matrix(rows, cols);
    for (int i =0; i<rows; i++) {
      for (int j = 0; j<cols; j++) {
        clone.matrix[i][j] = matrix[i][j];
      }
    }
    return clone;
  }
}
class NeuralNet {

  int iNodes;//No. of input nodes
  int hNodes;//No. of hidden nodes
  int oNodes;//No. of output nodes

  Matrix whi;//matrix containing weights between the input nodes and the hidden nodes
  Matrix whh;//matrix containing weights between the hidden nodes and the second layer hidden nodes
  Matrix woh;//matrix containing weights between the second hidden layer nodes and the output nodes
//---------------------------------------------------------------------------------------------------------------------------------------------------------  

  //constructor
  NeuralNet(int inputs, int hiddenNo, int outputNo) {

    //set dimensions from parameters
    iNodes = inputs;
    oNodes = outputNo;
    hNodes = hiddenNo;


    //create first layer weights 
    //included bias weight
    whi = new Matrix(hNodes, iNodes +1);

    //create second layer weights
    //include bias weight
    whh = new Matrix(hNodes, hNodes +1);

    //create second layer weights
    //include bias weight
    woh = new Matrix(oNodes, hNodes +1);  

    //set the matricies to random values
    whi.randomize();
    whh.randomize();
    woh.randomize();
  }
//---------------------------------------------------------------------------------------------------------------------------------------------------------  

  //mutation function for genetic algorithm
  public void mutate(float mr) {
    //mutates each weight matrix
    whi.mutate(mr);
    whh.mutate(mr);
    woh.mutate(mr);
  }

//---------------------------------------------------------------------------------------------------------------------------------------------------------  
  //calculate the output values by feeding forward through the neural network
  public float[] output(float[] inputsArr) {

    //convert array to matrix
    //Note woh has nothing to do with it its just a function in the Matrix class
    Matrix inputs = woh.singleColumnMatrixFromArray(inputsArr);

    //add bias 
    Matrix inputsBias = inputs.addBias();


    //-----------------------calculate the guessed output

    //apply layer one weights to the inputs
    Matrix hiddenInputs = whi.dot(inputsBias);

    //pass through activation function(sigmoid)
    Matrix hiddenOutputs = hiddenInputs.activate();

    //add bias
    Matrix hiddenOutputsBias = hiddenOutputs.addBias();

    //apply layer two weights
    Matrix hiddenInputs2 = whh.dot(hiddenOutputsBias);
    Matrix hiddenOutputs2 = hiddenInputs2.activate();
    Matrix hiddenOutputsBias2 = hiddenOutputs2.addBias();

    //apply level three weights
    Matrix outputInputs = woh.dot(hiddenOutputsBias2);
    //pass through activation function(sigmoid)
    Matrix outputs = outputInputs.activate();

    //convert to an array and return
    return outputs.toArray();
  }
//---------------------------------------------------------------------------------------------------------------------------------------------------------  
  //crossover function for genetic algorithm
  public NeuralNet crossover(NeuralNet partner) {

    //creates a new child with layer matrices from both parents
    NeuralNet child = new NeuralNet(iNodes, hNodes, oNodes);
    child.whi = whi.crossover(partner.whi);
    child.whh = whh.crossover(partner.whh);
    child.woh = woh.crossover(partner.woh);
    return child;
  }
//---------------------------------------------------------------------------------------------------------------------------------------------------------  
  //return a neural net which is a clone of this Neural net
  public NeuralNet clone() {
    NeuralNet clone  = new NeuralNet(iNodes, hNodes, oNodes); 
    clone.whi = whi.clone();
    clone.whh = whh.clone();
    clone.woh = woh.clone();

    return clone;
  }
//---------------------------------------------------------------------------------------------------------------------------------------------------------  
  //converts the weights matrices to a single table 
  //used for storing the snakes brain in a file
  public Table NetToTable() {

    //create table
    Table t = new Table();


    //convert the matricies to an array 
    float[] whiArr = whi.toArray();
    float[] whhArr = whh.toArray();
    float[] wohArr = woh.toArray();

    //set the amount of columns in the table
    for (int i = 0; i< max(whiArr.length, whhArr.length, wohArr.length); i++) {
      t.addColumn();
    }

    //set the first row as whi
    TableRow tr = t.addRow();

    for (int i = 0; i< whiArr.length; i++) {
      tr.setFloat(i, whiArr[i]);
    }


    //set the second row as whh
    tr = t.addRow();

    for (int i = 0; i< whhArr.length; i++) {
      tr.setFloat(i, whhArr[i]);
    }

    //set the third row as woh
    tr = t.addRow();

    for (int i = 0; i< wohArr.length; i++) {
      tr.setFloat(i, wohArr[i]);
    }

    //return table
    return t;
  }

//---------------------------------------------------------------------------------------------------------------------------------------------------------  
  //takes in table as parameter and overwrites the matrices data for this neural network
  //used to load snakes from file
  public void TableToNet(Table t) {

    //create arrays to tempurarily store the data for each matrix
    float[] whiArr = new float[whi.rows * whi.cols];
    float[] whhArr = new float[whh.rows * whh.cols];
    float[] wohArr = new float[woh.rows * woh.cols];

    //set the whi array as the first row of the table
    TableRow tr = t.getRow(0);

    for (int i = 0; i< whiArr.length; i++) {
      whiArr[i] = tr.getFloat(i);
    }


    //set the whh array as the second row of the table
    tr = t.getRow(1);

    for (int i = 0; i< whhArr.length; i++) {
      whhArr[i] = tr.getFloat(i);
    }

    //set the woh array as the third row of the table

    tr = t.getRow(2);

    for (int i = 0; i< wohArr.length; i++) {
      wohArr[i] = tr.getFloat(i);
    }


    //convert the arrays to matrices and set them as the layer matrices 
    whi.fromArray(whiArr);
    whh.fromArray(whhArr);
    woh.fromArray(wohArr);
  }
}
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
  public void update() {
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
  public void chooseBestSnakeAndGetAvgFitness() {
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
  public void playBestSnake() {
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
  public void changeGen() {
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
  public void startVel() {
    float rel = random(4); //randomVel = rel
    vel = new PVector(0, 0);
    if(rel<1){vel.x=-1;vel.y=0;}else if(rel>=1&&rel<2){vel.x=1;vel.y=0;}else if(rel>=2&&rel<3){vel.x=0;vel.y=-1;}else{vel.x=0;vel.y=1;}
    vel.mult(scl);
  }

  public void update() {
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

  public void render() {
    for(int i = 0; i < pos.length; i++) {
      square(pos[i].x, pos[i].y); // dibujar todos los cuadrados
    }
  }

  // Mover la serpiente
  public void move() {
    PVector previous = prevHead.get();
    PVector previousCopy = prevHead.get(); 
    for(int i = 1; i < pos.length; i++) {
      previous = pos[i];
      pos[i] = previousCopy;
      previousCopy = previous;
    }
  }

  // Comprobar si la serpiente estuvo en la posición de la comida
  public boolean wasInFoodPos() {
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
  public void plusOne() {
    sinceFood = 0;
    if(pos.length == 1) {
      pos = (PVector[])append(pos, new PVector(prevHead.x, prevHead.y));
    } else {
      pos = (PVector[])append(pos, new PVector(pos[pos.length - 1].x, pos[pos.length - 1].y));
    }
  }
  
  // Morir y calcular el fitness
  public void died() {
    alive = false;
    justDied = true;
    if(pos.length < 10) { // pos.length es el tamaño de la serpiente
      fitness = lifetime*lifetime*floor(pow(2, pos.length));
    } else {
      fitness = lifetime*lifetime*floor(pow(2, 10))*(pos.length - 9);
    }

    pos[0].sub(vel);
  }
  
  public boolean justDied() {
    if(justDied) {
      justDied = false;
      return true;
    }
    return false;
  }

  // Comprobar si chocó con el cuerpo
  public boolean collidedBody(float x, float y) {
    for(int i = 2; i < pos.length; i++) {
      if(x == pos[i].x && y == pos[i].y) {
        return true;
      }
    }
    return false;
  }

  // Dibujar un cuadrado en las coords. x e y 
  public void square(float x, float y) {
    noStroke();
    fill(snakecol);
    rect(x + 1, y + 1, scl - 1, scl - 1);
  }
}
class World {
  World() {
    render();
  }
  
  // Dibujar cuadrícula normal
  public void render() {
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

  // Comprobar si las coords. x e y están dentro de los límites
  public boolean isInsideBoundaries(float x, float y) {
    if(x >= scl*horsqrs || x < 0 || y >= scl*versqrs || y < 0) {
      return false;
    }
    return true;
  }
  
  // Mostrar el panel de la derecha
  public void renderPanel() {
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
      text("Training", 20, 140);
    }
    text("Score: " + currentScore, 20, 180);
    text("Speed: " + speedText, 20, 220);
    text("Mut. rate: " + round(mutRate*100) + "%", 20, 260);
    if(!hideKeys) {
      text("J-K-L: vary speed", 20, 340);
      text("S-D: vary mut. rate", 20, 370);
      text("R: render all snakes", 20, 400);
    }

    popMatrix();
  } 
}
  public void settings() {  fullScreen(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "deepGeneticSnake" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
