package threadrelay;

public class Corridori implements Runnable {

    private boolean nuovoThreadAvviato = false;
    public static int contatoreThread = 0;
    private boolean sospeso = false;
    private boolean fermato = false;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel lblPercentuale;
    private Staffetta staffetta;

    public Corridori(javax.swing.JProgressBar progressBar, javax.swing.JLabel lblPercentuale, Staffetta staffetta) {
        this.progressBar = progressBar;
        this.lblPercentuale = lblPercentuale;
        this.staffetta = staffetta;
    }

    @Override
    public void run() {
        for (int i = 0; i <= 100; i++) {
            if (fermato) {
                return;
            }

            int p = i;
            javax.swing.SwingUtilities.invokeLater(() -> {
                progressBar.setValue(p);
                progressBar.repaint();
                lblPercentuale.setText(p == 100 ? "Fine" : p + "%");
            });

            if (i == 90 && !nuovoThreadAvviato) {
                nuovoThreadAvviato = true;
                if (contatoreThread < 3) {
                    contatoreThread++;
                    staffetta.avviaRunner(contatoreThread);
                }
            }

            synchronized (this) {
                while (sospeso) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }

            try {
                Thread.sleep(staffetta.getVelocita());
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    public synchronized void setSospeso(boolean sospeso) {
        this.sospeso = sospeso;
        if (!sospeso) {
            notifyAll();
        }
    }

    public void setFermato(boolean fermato) {
        this.fermato = fermato;
    }
}
