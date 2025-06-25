package gui;

import auth.service.LoginController;
import auth.service.NewLoginUI;

/**
 *
 * @author Jomax
 */
public class AutomatedPayrollSystem {

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
    java.awt.EventQueue.invokeLater(new Runnable() {
        public void run() {
            new NewLoginUI().setVisible(true);
        }
    });
}
}