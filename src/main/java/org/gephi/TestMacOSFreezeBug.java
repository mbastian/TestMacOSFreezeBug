package org.gephi;

import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import java.awt.BorderLayout;
import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class TestMacOSFreezeBug extends JFrame {

    private final JPanel panel;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "GL Init"));

    public TestMacOSFreezeBug() {
        super("JOGL Window");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);

        panel = new JPanel();
        panel.setLayout(new java.awt.BorderLayout());
        getContentPane().add(panel);

        setLocationRelativeTo(null);
        setVisible(true);
        toggle();
    }

    private void toggle() {
        Drawable drawable = new Drawable();
        Component component = drawable.getComponent();
        executor.execute(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            try {
                SwingUtilities.invokeAndWait(() -> {
                    panel.add(component, BorderLayout.CENTER);
                    panel.validate();
                });
            } catch (InterruptedException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static class Drawable implements GLEventListener {

        NewtCanvasAWT glCanvas;
        GLWindow glWindow;

        protected static final com.jogamp.opengl.glu.GLU GLU = new GLU();

        public synchronized Component getComponent() {
            if (this.glCanvas == null) {
                glWindow = GLWindow.create(new GLCapabilities(GLProfile.get(GLProfile.GL2)));

                glCanvas = new NewtCanvasAWT(glWindow);

                FPSAnimator animator = new FPSAnimator(10);
                animator.add(glWindow);
                animator.start();

                glWindow.addGLEventListener(this);
            }
            return glCanvas;
        }

        @Override
        public void init(GLAutoDrawable drawable) {
        }

        @Override
        public void dispose(GLAutoDrawable glAutoDrawable) {

        }

        @Override
        public void display(GLAutoDrawable drawable) {
            GL2 gl = drawable.getGL().getGL2();

            gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glLoadIdentity();
            gl.glTranslatef(0f, 0f, -10.0f);
            gl.glColor3f(0f, 1f, 0f);

            gl.glBegin(GL2.GL_TRIANGLES);
            gl.glVertex3f(0.5f, 0.7f, 0.0f);
            gl.glVertex3f(-0.2f, -0.50f, 0.0f);
            gl.glVertex3f(0.5f, -0.5f, 0.0f);
            gl.glEnd();
        }

        @Override
        public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
            GL2 gl = drawable.getGL().getGL2();

            gl.glViewport(0, 0, width, height);
            gl.glMatrixMode(GL2.GL_PROJECTION);

            gl.glLoadIdentity();
            GLU.gluPerspective(45.0f, (float) height / (float) width, 1.0, 20.0);
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glLoadIdentity();
        }
    }

    public static void main(String[] args) {
        new TestMacOSFreezeBug();
    }
}
