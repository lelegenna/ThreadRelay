package threadrelay;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

/**
 * Contiene tutto il necessario per il Pattern Observer della staffetta:
 *  - Observer     : interfaccia per chi riceve le notifiche
 *  - Subject      : classe base che gestisce la lista Observer e le notifiche
 *  - AscoltatoreCorsore : Observer concreto che aggiorna la GUI
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

        // Notifica su copia per evitare ConcurrentModificationException
        protected synchronized void notifyObservers(int indiceRunner, int percentuale) {
            new ArrayList<>(observers).forEach(o -> o.update(indiceRunner, percentuale));
        }
    }

    /** Observer concreto: riceve la notifica e aggiorna JProgressBar e JLabel sull'EDT. */
    public static class AscoltatoreCorsore implements Observer {

        private final JProgressBar[] bars;
        private final JLabel[]       lblPercentuale;

        public AscoltatoreCorsore(JProgressBar[] bars, JLabel[] lblPercentuale) {
            this.bars           = bars;
            this.lblPercentuale = lblPercentuale;
        }

        @Override
        public void update(int indiceRunner, int percentuale) {
            SwingUtilities.invokeLater(() -> {
                bars[indiceRunner].setValue(percentuale);
                lblPercentuale[indiceRunner].setText(percentuale == 100 ? "Fine" : percentuale + "%");
            });
        }
    }
}
