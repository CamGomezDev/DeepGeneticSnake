# AI  for Snake with a Genetic Algorithm and Neural Networks

This repository accompanies this video: https://www.youtube.com/watch?v=k2JqH5j2VYo. 

The Java framework used is Processing and the main file is `DeepGeneticSnake.pde`. I've made comments to the code (**in spanish**) as well as I could.

![alt text](https://github.com/dokasov/deepGeneticSnake/blob/master/img/git.png)

To execute it's necessary to have Processing installed. After this, the repository must be just downloaded or cloned, opened from Processing and then run. The keys to control the simulation then show up. The classes `NeuralNet.pde` and `Matrix.pde` were gotten from the code that accompanies this Code Bullet video https://www.youtube.com/watch?v=3bhP7zulFfY.

The AI consists in a genetic algorithm that, in each generation, create 2000 snakes and then chooses the best ones and combines its brain for the next generation. The best ones, in this case, are defined as the ones that got a highest fitness, where the fitness function is defined in `Snake.pde` in the function `died()`. The brains are the neural networks, and to combine them means to create a new one with some nodes copied from one some others from other, mutating some of them to generate variety.

If something is not understood you can let me know in the Issues.
