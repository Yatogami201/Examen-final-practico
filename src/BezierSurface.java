import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class BezierSurface extends JPanel implements KeyListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final double FOCAL_DISTANCE = 600.0;

    // Ángulos de rotación de la cámara
    private double theta = 0.0;  // Rotación horizontal
    private double phi = Math.PI / 2;  // Rotación vertical
    private double radius = 1500.0;  // Radio de la esfera virtual

    // Datos del archivo
    private int n, m;  // n y m para los puntos de control (0 a n, 0 a m)
    private Point3D[][] controlPoints;
    private int uSegments, vSegments;
    private Point3D center;

    // Puntos de la superficie generada
    private Point3D[][] surfacePoints;

    public BezierSurface(String filename) {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        loadFile(filename);
        generateSurface();
    }

    // Cargar archivo de entrada
    private void loadFile(String filename) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));

            // Primera línea: n m
            String[] line = reader.readLine().trim().split("\\s+");
            n = Integer.parseInt(line[0]);
            m = Integer.parseInt(line[1]);

            // Matriz de puntos de control (n+1) x (m+1)
            controlPoints = new Point3D[n + 1][m + 1];
            for (int i = 0; i <= n; i++) {
                for (int j = 0; j <= m; j++) {
                    line = reader.readLine().trim().split("\\s+");
                    double x = Double.parseDouble(line[0]);
                    double y = Double.parseDouble(line[1]);
                    double z = Double.parseDouble(line[2]);
                    controlPoints[i][j] = new Point3D(x, y, z);
                }
            }

            // Número de segmentos
            line = reader.readLine().trim().split("\\s+");
            uSegments = Integer.parseInt(line[0]);
            vSegments = Integer.parseInt(line[1]);

            // Centro de rotación
            line = reader.readLine().trim().split("\\s+");
            double cx = Double.parseDouble(line[0]);
            double cy = Double.parseDouble(line[1]);
            double cz = Double.parseDouble(line[2]);
            center = new Point3D(cx, cy, cz);

            reader.close();

            System.out.println("Archivo cargado exitosamente:");
            System.out.println("Puntos de control: " + (n+1) + "x" + (m+1));
            System.out.println("Segmentos: " + uSegments + "x" + vSegments);
            System.out.println("Centro: " + center);

        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
            System.exit(1);
        }
    }

    // Calcular coeficiente binomial C(n, k)
    private double binomialCoeff(int n, int k) {
        if (k < 0 || k > n) return 0;
        if (k == 0 || k == n) return 1;

        double result = 1;
        for (int i = 1; i <= k; i++) {
            result *= (double)(n - i + 1) / i;
        }
        return result;
    }

    // Función de mezcla de Bernstein
    private double bernstein(int n, int k, double t) {
        return binomialCoeff(n, k) * Math.pow(t, k) * Math.pow(1 - t, n - k);
    }

    // Calcular punto en la superficie de Bézier usando la fórmula:
    // P(u,v) = Σ(j=0 to m) Σ(k=0 to n) p[j,k] * BEZ[j,m](u) * BEZ[k,n](v)
    private Point3D bezierSurface(double u, double v) {
        double x = 0, y = 0, z = 0;

        for (int j = 0; j <= m; j++) {
            for (int k = 0; k <= n; k++) {
                double blend = bernstein(m, j, u) * bernstein(n, k, v);
                x += controlPoints[j][k].x * blend;
                y += controlPoints[j][k].y * blend;
                z += controlPoints[j][k].z * blend;
            }
        }

        return new Point3D(x, y, z);
    }

    // Generar todos los puntos de la superficie
    private void generateSurface() {
        surfacePoints = new Point3D[uSegments + 1][vSegments + 1];

        for (int i = 0; i <= uSegments; i++) {
            double u = (double)i / uSegments;
            for (int j = 0; j <= vSegments; j++) {
                double v = (double)j / vSegments;
                surfacePoints[i][j] = bezierSurface(u, v);
            }
        }
    }

    // Transformar punto 3D según la vista de la cámara
    private Point3D transformPoint(Point3D point) {
        // Trasladar al origen (centrar en el centro de rotación)
        double x = point.x - center.x;
        double y = point.y - center.y;
        double z = point.z - center.z;

        // Rotación alrededor del eje Y (theta)
        double x1 = x * Math.cos(theta) - z * Math.sin(theta);
        double z1 = x * Math.sin(theta) + z * Math.cos(theta);
        double y1 = y;

        // Rotación alrededor del eje X (phi - π/2 para ajustar orientación)
        double phiAdjusted = phi - Math.PI / 2;
        double y2 = y1 * Math.cos(phiAdjusted) - z1 * Math.sin(phiAdjusted);
        double z2 = y1 * Math.sin(phiAdjusted) + z1 * Math.cos(phiAdjusted);
        double x2 = x1;

        // Trasladar hacia atrás (alejar del observador)
        z2 -= radius;

        return new Point3D(x2, y2, z2);
    }

    // Proyección perspectiva
    private Point2D project(Point3D point3d) {
        if (point3d.z >= -50) return null;  // Punto muy cerca o detrás

        double x2d = (FOCAL_DISTANCE * point3d.x) / (-point3d.z);
        double y2d = (FOCAL_DISTANCE * point3d.y) / (-point3d.z);

        return new Point2D(x2d, y2d);
    }

    // Convertir coordenadas 2D a coordenadas de pantalla
    private Point toScreen(Point2D p) {
        if (p == null) return null;
        int x = (int)(WIDTH / 2 + p.x);
        int y = (int)(HEIGHT / 2 - p.y);
        return new Point(x, y);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Transformar todos los puntos
        Point3D[][] transformed = new Point3D[uSegments + 1][vSegments + 1];
        for (int i = 0; i <= uSegments; i++) {
            for (int j = 0; j <= vSegments; j++) {
                transformed[i][j] = transformPoint(surfacePoints[i][j]);
            }
        }

        // Dibujar malla de la superficie en verde
        g2d.setColor(Color.GREEN);
        g2d.setStroke(new BasicStroke(1.5f));

        // Líneas en dirección u
        for (int j = 0; j <= vSegments; j++) {
            Point lastPoint = null;
            for (int i = 0; i <= uSegments; i++) {
                Point2D p2d = project(transformed[i][j]);
                Point screenPoint = toScreen(p2d);

                if (screenPoint != null) {
                    if (lastPoint != null) {
                        g2d.drawLine(lastPoint.x, lastPoint.y,
                                screenPoint.x, screenPoint.y);
                    }
                    lastPoint = screenPoint;
                }
            }
        }

        // Líneas en dirección v
        for (int i = 0; i <= uSegments; i++) {
            Point lastPoint = null;
            for (int j = 0; j <= vSegments; j++) {
                Point2D p2d = project(transformed[i][j]);
                Point screenPoint = toScreen(p2d);

                if (screenPoint != null) {
                    if (lastPoint != null) {
                        g2d.drawLine(lastPoint.x, lastPoint.y,
                                screenPoint.x, screenPoint.y);
                    }
                    lastPoint = screenPoint;
                }
            }
        }

        // Dibujar puntos de control en rojo
        g2d.setColor(Color.RED);
        for (int i = 0; i <= n; i++) {
            for (int j = 0; j <= m; j++) {
                Point3D transformed3d = transformPoint(controlPoints[i][j]);
                Point2D p2d = project(transformed3d);
                Point screenPoint = toScreen(p2d);

                if (screenPoint != null) {
                    g2d.fillOval(screenPoint.x - 5, screenPoint.y - 5, 10, 10);
                    g2d.setColor(Color.WHITE);
                    g2d.drawOval(screenPoint.x - 5, screenPoint.y - 5, 10, 10);
                    g2d.setColor(Color.RED);
                }
            }
        }

        // Información de debug
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2d.drawString(String.format("Theta: %.2f", theta), 10, 20);
        g2d.drawString(String.format("Phi: %.2f", phi), 10, 35);
        g2d.drawString("Flechas: Rotar cámara", 10, 55);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        double step = 0.15;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                theta -= step;
                break;
            case KeyEvent.VK_RIGHT:
                theta += step;
                break;
            case KeyEvent.VK_UP:
                phi = Math.max(0.1, phi - step);
                break;
            case KeyEvent.VK_DOWN:
                phi = Math.min(Math.PI - 0.1, phi + step);
                break;
        }

        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java BezierSurface <archivo.txt>");
            System.out.println("Usando archivo por defecto: superficie.txt");
            args = new String[]{"superficie.txt"};
        }

        JFrame frame = new JFrame("Superficie de Bézier 3D");
        BezierSurface panel = new BezierSurface(args[0]);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        panel.requestFocusInWindow();
    }
}

// Clase para representar puntos 3D
class Point3D {
    double x, y, z;

    Point3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f, %.2f)", x, y, z);
    }
}

// Clase para representar puntos 2D
class Point2D {
    double x, y;

    Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
}