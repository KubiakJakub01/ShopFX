package com.example.projektfx;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.text.Font;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.sleep;

public class Sklep {

    private final static int space = 300;
    private static final int font_size = 50;
    private double X;
    private Lock lock = new ReentrantLock();
    private int ilosc_kas;
    private Kasa[] kasy;
    public int max_ilosc_klientow_w_sklepie;
    public AtomicInteger ilosc_klientow_w_sklepie;
    private int czas_podejscia_do_kasy;
    private int czas_wejscia_do_sklepu;
    private int base_czas_wejscia_do_sklepu;
    private int czas_podejscia_do_kolejki;
    private int base_czas_podejscia_do_kolejki;
    private int czas_do_przerwy;
    private int czas_przerwy;
    private int scale_rate;
    private boolean czy_sklep_dzala;
    Thread wpuszczajDoSklepu;
    Thread przechodzDoKasy;
    Thread zarzadzajPrzerwamiKolejek;
    @FXML
    private Label ilosc_klientow_w_sklepie_label;

    public Sklep(int ilosc_kas, int max_ilosc_klientow_w_sklepie, int czas_podejscia_do_kasy, int czas_wejscia_do_sklepu, int czas_podejscia_do_kolejki, int czas_do_przerwy, int czas_przerwy, double X)
    {
        this.ilosc_kas = ilosc_kas;
        this.X = X;
        this.max_ilosc_klientow_w_sklepie = max_ilosc_klientow_w_sklepie;
        this.czas_podejscia_do_kasy = czas_podejscia_do_kasy;
        this.czas_wejscia_do_sklepu = czas_wejscia_do_sklepu;
        this.czas_podejscia_do_kolejki = czas_podejscia_do_kolejki;
        this.czas_do_przerwy = czas_do_przerwy;
        this.czas_przerwy = czas_przerwy;
        base_czas_wejscia_do_sklepu = czas_wejscia_do_sklepu;
        base_czas_podejscia_do_kolejki = czas_podejscia_do_kolejki;
        czy_sklep_dzala = true;
        ilosc_klientow_w_sklepie = new AtomicInteger();
        ilosc_klientow_w_sklepie.set(0);
        ilosc_klientow_w_sklepie_label = new Label(String.valueOf(ilosc_klientow_w_sklepie.get()));
        ilosc_klientow_w_sklepie_label.setFont(new Font("Arial", font_size));
        ilosc_klientow_w_sklepie_label.setLayoutX(210);
        Platform.runLater(() -> {
            Main.root.getChildren().add(ilosc_klientow_w_sklepie_label);
        });
    }

    public void zacznijPraceSklepu()
    {
        zacznij_prace_kas();
         wpuszczajDoSklepu = new Thread(() -> {
            wpuszczajDoSklepu();
        });
         przechodzDoKasy = new Thread(() -> {
            przechodzDoKasy();
        });
         zarzadzajPrzerwamiKolejek = new Thread(() -> {
            zarzadzajPrzerwamiKolejek();
        });
        wpuszczajDoSklepu.start();
        przechodzDoKasy.start();
        zarzadzajPrzerwamiKolejek.start();
    }


    private void zacznij_prace_kas()
    {
        kasy = new Kasa[ilosc_kas];
        double rectX = 150 + (X - space*ilosc_kas)/2;
        for(int i = 0; i< ilosc_kas; i++)
        {
            int nr = i+1;
            kasy[i] = new Kasa(nr, rectX, lock, czas_podejscia_do_kasy, czas_przerwy, ilosc_kas);

            rectX += space;
        }
        for(Kasa thread: kasy)
        {
            thread.start();
        }
    }

    private void zarzadzajPrzerwamiKolejek()
    {
        int i = 0;

        while(czy_sklep_dzala)
        {
            int ilosc_wolnych_kolejek = 0;
            try {
                sleep(czas_do_przerwy);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for(Kasa k: kasy)
            {
                if(!k.kolejka.czyChcePrzerwa()) ilosc_wolnych_kolejek++;
            }
            if(ilosc_wolnych_kolejek <= 1) {
                try {
                    sleep(czas_do_przerwy/3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            kasy[i].kolejka.zarzadzPrzerwe();
            i++;
            i = i % ilosc_kas;
        }
    }
    private void wpuszczajDoSklepu(){
        while (czy_sklep_dzala)
        {
            try {
                sleep(czas_wejscia_do_sklepu);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(ilosc_klientow_w_sklepie.get() < max_ilosc_klientow_w_sklepie)
            {
                System.out.println("Ilosc klientow w sklepie: " + ilosc_klientow_w_sklepie.get() + " max klientow: " + max_ilosc_klientow_w_sklepie);
                dodajKlienta();
            }
        }
    }

    private void przechodzDoKasy() {
        int i;
        int min_i;
        int min_val;
        int val;
        while (czy_sklep_dzala)
        {
            while (ilosc_klientow_w_sklepie.get() > 0) {
                i = 0;
                min_i = 0;
                val = 0;
                min_val = max_ilosc_klientow_w_sklepie;
                try {
                    sleep(czas_podejscia_do_kolejki);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (Kasa k : kasy) {
                    if(!k.kolejka.czyChcePrzerwa()) {
                        val = k.kolejka.getIlosc_klientow_w_kolejce();
                        if (val < min_val) {
                            min_val = val;
                            min_i = i;
                        }
                    }
                    i++;
                }
                odejmijKlienta();
                kasy[min_i].kolejka.dodajKlienta();
            }
        }
    }

    public void setObslugaRate(double rate)
    {
        for(Kasa k: kasy)
        {
            k.setObslugaRate(rate);
        }
    }

    public void setSklepRate(double rate)
    {
        czas_wejscia_do_sklepu = (int)((1/rate)*base_czas_wejscia_do_sklepu);
    }
    public void setKolejkaRate(double rate)
    {
        czas_podejscia_do_kolejki = (int)((1/rate)*base_czas_podejscia_do_kolejki);
    }

    private void dodajKlienta()
    {
        setIloscKlientowWSklepielabel(ilosc_klientow_w_sklepie.incrementAndGet());
    }

    private void odejmijKlienta()
    {
        setIloscKlientowWSklepielabel(ilosc_klientow_w_sklepie.decrementAndGet());
    }

    public void wstrzymajSklep()
    {
        System.out.println("Zatrzymuje dzialanie sklepu");

        wpuszczajDoSklepu.suspend();
        przechodzDoKasy.suspend();
        zarzadzajPrzerwamiKolejek.suspend();

        for(Kasa kasa: kasy)
        {
            kasa.kolejka.suspend();
            kasa.suspend();
        }


    }

    public void wznowSklep()
    {
        System.out.println("Wznawiam dzialanie sklepu");

        for(Kasa kasa: kasy)
        {
            kasa.kolejka.resume();
            kasa.resume();
        }
        wpuszczajDoSklepu.resume();
        przechodzDoKasy.resume();
        zarzadzajPrzerwamiKolejek.resume();

    }

    public void zamknijSklep() throws InterruptedException {
        System.out.println("Kończe dzialanie sklepu");
        czy_sklep_dzala = false;

        for(Kasa kasa: kasy)
        {
            kasa.zamknijKase();
        }
    }

    @FXML
    public void setIloscKlientowWSklepielabel(int value){
        Platform.runLater(() -> {
            ilosc_klientow_w_sklepie_label.setText(String.valueOf(value));
        });
    }
}
