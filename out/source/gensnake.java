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

public class gensnake extends PApplet {

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

public void setup() {
  background(bgcol);
  
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

public void draw() {
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

public void restart() {
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

  public void update() {
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

  public void restart() {
    for (int i = 0; i < foods.length; ++i) {
      foods[i] = new Food();
    }
  }
}

class Food {
  PVector pos = new PVector(floor(random(horsqrs))*scl, floor(random(versqrs))*scl);
  
  public void render() {
    fill(foodcol);
    noStroke();
    rect(pos.x + 1, pos.y + 1, scl - 1, scl - 1);
  }
  
  public void update(Snake snake) {
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

  public float[] frame(Snake snake) {
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

  public float[] frameASD(Snake snake) {
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


  public float[] lookInDirection(PVector direction, Snake cSnake) {

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
  
  public void play(Snake snake) {
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

  public void update() {
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

  public void chooseBestSnake() {
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

  public void playBestSnake() {
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

  public void changeGen() {
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

  public void render() {
    for(int i = 0; i < pos.length; i++) {
      square(pos[i].x, pos[i].y);
    }
  }

  public void move() {
    PVector previous = prevHead.get();
    PVector previousCopy = prevHead.get(); 
    for(int i = 1; i < pos.length; i++) {
      previous = pos[i];
      pos[i] = previousCopy;
      previousCopy = previous;
    }
  }

  public boolean wasInFoodPos() {
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

  public void plusOne() {
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
  
  public void died() {
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
  
  public boolean justDied() {
    if(justDied) {
      justDied = false;
      return true;
    }
    return false;
  }
  
  public boolean collidedBody(float x, float y) {
    for(int i = 2; i < pos.length; i++) {
      if(x == pos[i].x && y == pos[i].y) {
        return true;
      }
    }
    return false;
  }
  
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

  public boolean isInsideBoundaries(float x, float y) {
    if(x >= scl*horsqrs || x < 0 || y >= scl*versqrs || y < 0) {
      return false;
    }
    return true;
  }
  
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
      boolean runInArray = false;
      for (int i = population.gens.get(population.cg).inRun.length - 1; i > -1; i--) {
        if(population.gens.get(population.cg).inRun[i]) {
          text("Generation run " + (i + 2), 20, 140);
          runInArray = true;
          break;
        }
      }
      if(!runInArray) {
        text("Generation run 1", 20, 140);
      }
    }
    text("Score: " + currentScore, 20, 180);
    text("Speed: " + speedText, 20, 220);
    text("Mut. rate: " + round(mutRate*100) + "%", 20, 260);
    if(!hideKeys) {
      text("J-K-L: vary speed", 20, 340);
      text("S-D: vary mut. rate", 20, 370);
      text("R: render all snakes", 20, 400);
    }
    
    // //Button 1
    // stroke(0);
    // if(controller.isHuman) {
    //   fill(110);
    // } else {
    //   fill(200);
    // }
    // textSize(20);
    // rect(20, 180, 70, 30);
    // fill(0);
    // textAlign(CENTER, CENTER);
    // text("Smart", 20 + 70/2, 180 + 30/2 - 3);
    
    // //Button 2
    // stroke(0);
    // if(controller.isHuman) {
    //   fill(200);
    // } else {
    //   fill(110);
    // }
    // textSize(20);
    // rect(20, 220, 70, 30);
    // fill(0);
    // textAlign(CENTER, CENTER);
    // text("You", 20 + 70/2, 220 + 30/2 - 3);   
    
    popMatrix();
  } 
}

public void mouseClicked() {
  float panelX = width - panelWidth;
  if(mouseX > panelX + 20 && mouseX < panelX + 20 + 70 && mouseY > 180 && mouseY < 180 + 30) {
    controller.isHuman = false;
    world.renderPanel();
  }
  else if(mouseX > panelX + 20 && mouseX < panelX + 20 + 70 && mouseY > 220 && mouseY < 220 + 30) {
    controller.isHuman = true;
    world.renderPanel();
  }
}
  public void settings() {  fullScreen(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "gensnake" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
