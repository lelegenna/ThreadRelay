package threadrelay;

/**
 * ConcreteSubject + Runnable.
 * Avanza da 0 a 100, notifica gli Observer ad ogni step
 * e al 90% passa il testimone al runner successivo.
 */
public class Corridori extends GestoreNotifiche.Subject implements Runnable {

    public static int contatoreThread = 0;

    private final int       indiceRunner;
    private final Staffetta staffetta;

    private volatile boolean sospeso            = false;
    private volatile boolean fermato            = false;
    private          boolean nuovoThreadAvviato = false;

    public Corridori(int indiceRunner, Staffetta staffetta) {
        this.indiceRunner = indiceRunner;
        this.staffetta    = staffetta;
    }

    @Override
    public void run() {
        for (int i = 0; i <= 100; i++) {
            if (fermato) return;

            notifyObservers(indiceRunner, i);

            // Passa il testimone al 90%
            if (i == 90 && !nuovoThreadAvviato && contatoreThread < 3) {
                nuovoThreadAvviato = true;
                staffetta.avviaRunner(++contatoreThread);
            }

            synchronized (this) {
                while (sospeso) {
                    try { wait(); } catch (InterruptedException e) { return; }
                }
            }

            try { Thread.sleep(staffetta.getVelocita()); }
            catch (InterruptedException e) { return; }
        }
    }

    public synchronized void setSospeso(boolean sospeso) {
        this.sospeso = sospeso;
        if (!sospeso) notifyAll();
    }

    public void setFermato(boolean fermato) {
        this.fermato = fermato;
    }
}
