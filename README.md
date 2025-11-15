================================================================================
                    Punto Implementado: SUPERFICIE DE BÉZIER 3D 
                Universidad EAFIT - Computación Gráfica ST0275
                    Examen Final Práctico - Noviembre 2025
================================================================================
ESTUDIANTE: [Samuel Valencia Loaiza]
================================================================================
ARCHIVO superficie.txt:
================================================================================
En este archivo se deben guardar las coodernadas con las que se quiera formar la superficie, en el repositorio se encuentran las proporcionadas por el profesor
================================================================================
MÉTODO IMPLEMENTADO (SEGÚN EL EXAMEN):
================================================================================

Este programa implementa EXACTAMENTE el método de Superficies de Bézier
especificado en la presentación 10aCurvesAndSurfaces.pdf

1. COEFICIENTES BINOMIALES:
   Implementados en el método binomialCoeff(n, k)
   
   Fórmula: C(n,k) = n! / (k! × (n-k)!)
   
   Ejemplo para n=2, k=1: C(2,1) = 2!/(1!×1!) = 2

2. POLINOMIOS DE BERNSTEIN:
   Implementados en el método bernstein(n, k, t)
   
   Fórmula: BEZ[k,n](u) = C(n,k) × u^k × (1-u)^(n-k)
   
   Estos son los "blending functions" que mezclan los puntos de control.

3. SUPERFICIE DE BÉZIER PARAMÉTRICA:
   Implementada en el método bezierSurface(u, v)
   
   Fórmula tomada de la presentación:
   P(u,v) = Σ(j=0 hasta m) Σ(k=0 hasta n) p[j,k] × BEZ[j,m](u) × BEZ[k,n](v)
   
   Donde:
   - p[j,k] son los 9 puntos de control (matriz 3×3)
   - 0 ≤ u ≤ 1 (parámetro en dirección horizontal)
   - 0 ≤ v ≤ 1 (parámetro en dirección vertical)
   - n = 2, m = 2 (grado de la superficie)

4. GENERACIÓN DE LA MALLA:
   Implementada en el método generateSurface()
   
   - Se calculan 16×16 segmentos (como especifica el archivo)
   - Para cada valor de u = i/16 y v = j/16 se calcula P(u,v)
   - Esto genera una malla de 17×17 puntos (0 hasta 16 en cada dirección)

5. PROYECCIÓN Y VISUALIZACIÓN:
   - Rotación 3D usando matrices de transformación
   - Proyección perspectiva con distancia focal = 600
   - La cámara gira en coordenadas esféricas alrededor del centro (0,0,-1000)

================================================================================

ARCHIVOS INCLUIDOS EN EL ZIP:
================================================================================
- BezierSurface.java    : Clase principal con toda la implementación
- superficie.txt        : Archivo de datos de entrada (según especificación)
- README.TXT            : Este archivo de documentación
- video.mp4            : Video demostrativo (máximo 3 minutos)

================================================================================
CÓMO COMPILAR EL PROGRAMA:
================================================================================

Abrir terminal o símbolo del sistema en la carpeta del proyecto y ejecutar:

    javac BezierSurface.java

Esto generará el archivo BezierSurface.class

================================================================================
CÓMO EJECUTAR EL PROGRAMA:
================================================================================

En la misma terminal, ejecutar:

    java BezierSurface superficie.txt

El programa abrirá una ventana mostrando la superficie de Bézier en 3D.

================================================================================
CONTROLES:
================================================================================
Flecha IZQUIERDA  : Rotar cámara hacia la izquierda (theta -)
Flecha DERECHA    : Rotar cámara hacia la derecha (theta +)
Flecha ARRIBA     : Rotar cámara hacia arriba (phi -)
Flecha ABAJO      : Rotar cámara hacia abajo (phi +)
================================================================================
