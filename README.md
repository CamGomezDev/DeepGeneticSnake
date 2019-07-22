# I.A. de Snake con Algoritmo Genético y Redes Neuronales

Este repositorio acompaña este video: https://www.youtube.com/watch?v=k2JqH5j2VYo. 

El framework de Java usado es Processing y el archivo principal es `DeepGeneticSnake.pde`. He comentado el repositorio tan bien como he podido.

![alt text](https://github.com/dokasov/deepGeneticSnake/blob/master/img/git.png)

Para ejecutar es necesario tener Processing instalado. Después de esto, solo se debe descargar o clonar el repositorio, abrirlo desde Processing y correrlo. Ahí aparecen las teclas para controlar la simulación. Las clases `NeuralNet.pde` y `Matrix.pde` fueron sacadas del código que acompaña este video de Code Bullet https://www.youtube.com/watch?v=3bhP7zulFfY, y por eso tienen comentarios en inglés.

La I.A. consiste en un algoritmo genético que, en cada generación, crea 2000 serpientes y después elige las mejores y combina sus cerebros para la siguiente generación. Mejores, en este caso, se definen como las que hayan sacado un fitness más alto, en donde la función fitness está definida en `Snake.pde` en la función `died()`. Los cerebros son las redes neuronales, y combinarlos significa crear uno nuevo con unos nodos copiados de uno y otros de otro, mutando unos de ellos para generar variedad.

Si algo no se entiende me lo pueden hacer saber en los Issues.
