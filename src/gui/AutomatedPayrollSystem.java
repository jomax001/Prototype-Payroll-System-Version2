package gui;

import auth.service.LoginUI;

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
            new LoginUI().setVisible(true);
        }
    });
}
}