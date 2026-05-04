package threadrelay;

import java.awt.Container;
import java.awt.Image;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

/**
 * Contiene tutto il necessario per il Pattern Observer della staffetta:
 *  - Observer           : interfaccia per chi riceve le notifiche
 *  - Subject            : classe base che gestisce lista Observer e notifiche
 *  - AscoltatoreCorsore : Observer concreto che aggiorna GUI e icona corridore
 */
public class GestoreNotifiche {

    /** Chi vuole ricevere aggiornamenti implementa questa interfaccia. */
    public interface Observer {
        void update(int indiceRunner, int percentuale);
    }

    /** Gestisce la lista degli Observer e le notifiche. Estendila per diventare un Subject. */
    public static abstract class Subject {

        private final List<Observer> observers = new ArrayList<>();

        public synchronized void addObserver(Observer o) {
            if (!observers.contains(o)) observers.add(o);
        }

        public synchronized void removeObserver(Observer o) {
            observers.remove(o);
        }

        protected synchronized void notifyObservers(int indiceRunner, int percentuale) {
            new ArrayList<>(observers).forEach(o -> o.update(indiceRunner, percentuale));
        }
    }

    /**
     * Observer concreto: aggiorna JProgressBar, JLabel percentuale
     * e sposta un'icona corridore sopra ogni barra.
     *
     * Strategia: ogni JLabel icona viene aggiunta al PARENT della JProgressBar
     * (cioè jPanel4) con coordinate assolute calcolate in base alla posizione
     * e alla larghezza della barra. Non serve JLayeredPane.
     */
    public static class AscoltatoreCorsore implements Observer {

        private final JProgressBar[] bars;
        private final JLabel[]       lblPercentuale;
        private final JLabel[]       icone;
        private final int            iconW;
        private final int            iconH = 36;

        public AscoltatoreCorsore(JProgressBar[] bars, JLabel[] lblPercentuale,
                                  String iconaPath, int altezza) {
            this.bars           = bars;
            this.lblPercentuale = lblPercentuale;
            this.icone          = new JLabel[4];

            ImageIcon icona = caricaIcona(iconaPath, altezza);
            this.iconW = (icona != null) ? icona.getIconWidth() : 0;

            for (int i = 0; i < 4; i++) {
                icone[i] = new JLabel(icona);
                icone[i].setSize(iconW, altezza);
                icone[i].setVisible(false);
                // Aggiunto al parent della barra (jPanel4) dopo che la GUI è visibile
            }

            // Aggiungiamo le icone al parent non appena la barra è visualizzata
            SwingUtilities.invokeLater(this::aggiungiIconeAlParent);
        }

        /** Aggiunge ogni JLabel icona al parent della rispettiva JProgressBar. */
        private void aggiungiIconeAlParent() {
            for (int i = 0; i < 4; i++) {
                Container parent = bars[i].getParent();
                if (parent != null) {
                    parent.add(icone[i]);
                    parent.setComponentZOrder(icone[i], 0); // sopra tutto
                }
            }
        }

        /** Carica e scala l'immagine dal classpath. */
        private ImageIcon caricaIcona(String path, int altezza) {
            java.net.URL url = getClass().getResource(path);
            if (url == null) {
                System.err.println("ATTENZIONE: immagine non trovata -> " + path);
                return null;
            }
            ImageIcon raw = new ImageIcon(url);
            int w = (int) (raw.getIconWidth() * ((double) altezza / raw.getIconHeight()));
            Image scaled = raw.getImage().getScaledInstance(w, altezza, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        }

        @Override
        public void update(int idx, int perc) {
            SwingUtilities.invokeLater(() -> {
                bars[idx].setValue(perc);
                lblPercentuale[idx].setText(perc == 100 ? "Fine" : perc + "%");

                if (iconW == 0) return;

                // Calcola la posizione dell'icona in coordinate del parent (jPanel4)
                Point barLoc = bars[idx].getLocation();
                int barW     = bars[idx].getWidth();
                int barH     = bars[idx].getHeight();
                int x        = barLoc.x + (int) ((barW - iconW) * (perc / 100.0));
                int y        = barLoc.y + (barH - iconH) / 2;

                icone[idx].setBounds(x, y, iconW, iconH);
                icone[idx].setVisible(true);
                icone[idx].getParent().repaint();
            });
        }

        /** Nasconde tutte le icone (reset/ferma). */
        public void resetIcone() {
            SwingUtilities.invokeLater(() -> {
                for (JLabel ic : icone) ic.setVisible(false);
            });
        }
    }
}