package com.example.projektfx;

import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

public class Kolejka extends Thread{

    private static final double kolejka_Y = 500;
    private static final double kolejka_Width = 200;
    private static final int klient_rozmiar_poczatkowy = 20;
    public static final int rozmiar_bufora = 2;
    private int font_size = 30;
    private int nr;
    public Semaphore wolne;
    public Semaphore zajete;
    private AtomicInteger ilosc_klientow_w_kolejce;
    private double kolejka_X;
    private int czas_przerwy_kolejki;
    private int czas_tworzenia_klienta;
    private int czas_pokazania_kolejnego_klienta;
    private int ilosc_kolejek;
    private int czas_podejscia_do_kasy;
    private double rate;
    public Circle[] klienci_bufor;
    public int poz_buf;
    private int r;
    private int g;
    private int b;
    private double kolejka_center_X;
    private double kolejka_center_Y;
    private double obsluga_rate;
    private Lock dostep;
    private Circle obecny_klient;
    private Boolean czy_zmiany;
    private Boolean czy_dziala;
    private Boolean czy_wstzymana;
    private AtomicBoolean czy_chce_przerwe;
    private Rectangle kolejka;
    @FXML
    public Label ilosc_klientow_label;
    @FXML
    public Label nazwa_kolejki_label;

    public Kolejka( int nr, double rectX, Lock lock, int czas_podejscia_do_kasy, int czas_przerwy_kolejki, int ilosc_kolejek)
    {
        super("Kolejka nr. " + nr);
        this.nr = nr;
        this.ilosc_kolejek = ilosc_kolejek;
        this.obsluga_rate = ilosc_kolejek;
        this.czas_przerwy_kolejki = czas_przerwy_kolejki;
        this.czas_podejscia_do_kasy = czas_podejscia_do_kasy;
        czas_pokazania_kolejnego_klienta = (ilosc_kolejek+1)*czas_podejscia_do_kasy;
        czas_tworzenia_klienta = 2*czas_podejscia_do_kasy;
        rate = 1;
        czy_zmiany = false;

        klienci_bufor = new Circle[rozmiar_bufora];
        poz_buf = 0;

        kolejka_X = rectX;
        kolejka_center_X = kolejka_X + kolejka_Width /2;
        kolejka_center_Y = kolejka_Y + font_size + 5 + kolejka_Width /2;

        randKolor();
        ilosc_klientow_w_kolejce = new AtomicInteger();
        ilosc_klientow_w_kolejce.set(2);

        dostep = lock;
        czy_dziala = true;
        czy_wstzymana = false;
        czy_chce_przerwe = new AtomicBoolean();
        czy_chce_przerwe.set(false);
        wolne = new Semaphore(rozmiar_bufora);
        zajete = new Semaphore(0);
    }


    private StackPane stworzKolejke()
    {
        kolejka = new Rectangle(kolejka_Width, kolejka_Width);
        kolejka.setFill(Color.TRANSPARENT);
        kolejka.setStroke(Color.BLACK);
        kolejka.setStrokeWidth(2);

        nazwa_kolejki_label = new Label(getName());
        nazwa_kolejki_label.setFont(new Font("Arial", font_size));
        ilosc_klientow_label = new Label(String.valueOf(getIlosc_klientow_w_kolejce()));
        ilosc_klientow_label.setFont(new Font("Arial", font_size));

        VBox vbox = new VBox(5); // 5 is the spacing between elements in the VBox
        vbox.getChildren().addAll(nazwa_kolejki_label, kolejka, ilosc_klientow_label);
        vbox.setAlignment(Pos.CENTER);

        StackPane pane = new StackPane();
        pane.getChildren().add(vbox);
        pane.setAlignment(vbox, Pos.CENTER);
        pane.setTranslateX(kolejka_X);
        pane.setTranslateY(kolejka_Y);

        return pane;
    }


    private ScaleTransition scaleKlient(Circle klient)
    {
        ScaleTransition scaleTransition = new ScaleTransition();
        // Setting the duration for the transition
        scaleTransition.setDuration(Duration.millis(czas_tworzenia_klienta));
        // Setting the node for the transition
        scaleTransition.setNode(klient);
        // Setting the dimensions for scaling
        scaleTransition.setByY(1.5);
        scaleTransition.setByX(1.5);
        // Setting auto reverse value to true
        scaleTransition.setAutoReverse(false);
        scaleTransition.setRate(obsluga_rate);

        return scaleTransition;
    }


    @Override
    public void run()
    {
        System.out.println("Kolejka start playing: " + super.getName());


        Platform.runLater(() -> {
            Main.root.getChildren().add(stworzKolejke());
        });

        while(czy_dziala)
        {
            // Utworzenie i wyświetlenie klienta.
            //
            if(czy_zmiany)
            {
                System.out.println(getName() + " Zmiana ustawien");
                setObslugaRate();
                continue;
            }
            if(czy_chce_przerwe.get() && ilosc_klientow_w_kolejce.get() <= 0)
            {
                wstrzymajKolejke();
                try {
                    sleep(czas_przerwy_kolejki);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                koniecPrzerwy();
            }
            else if (ilosc_klientow_w_kolejce.get() == 0) {
                wstrzymajKolejke();
                while (ilosc_klientow_w_kolejce.get() == 0) {
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                wznowKolejke();
            }

            Circle klient = new Circle(kolejka_center_X, kolejka_center_Y, klient_rozmiar_poczatkowy);
            //System.out.println("Tworze klienta kolejka nr. " + nr + " r: " + r + " g: " + g + " b: " + b);
            klient.setFill(Color.rgb(r, g, b));
            klient.setStroke(Color.BLACK);
            klient.setStrokeWidth(2);

            Platform.runLater(() -> {
                Main.root.getChildren().add(klient);
            });

            ScaleTransition scaleTransition = scaleKlient(klient);

            scaleTransition.setOnFinished(e -> {
                synchronized (this) {
                    notify();
                }
            });

            dostep.lock();
            try
            {
                // Uruchomienie animacji przeskalowania.
                //
                scaleTransition.play();
            }
            finally {
                // Synchronizacja wątku z animacją skalowania.
                // Wstrzymanie wątku do czasu zakończenia animacji.
                //
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                dostep.unlock();
            }

            try {
                System.out.println(getName() + " produkcja klienta");
                wolne.acquire();
                klienci_bufor[poz_buf] = klient;
                zajete.release();
                poz_buf = (poz_buf+1)%rozmiar_bufora;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            odejmijKlienta();
            try {
                sleep(czas_pokazania_kolejnego_klienta);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public Circle getObecny_klient() {
        return obecny_klient;
    }

    public int getIlosc_klientow_w_kolejce() {
        return ilosc_klientow_w_kolejce.get();
    }

    public void dodajKlienta()
    {
        setIloscKlientowWKolejceLabel(ilosc_klientow_w_kolejce.incrementAndGet());
    }
    public void odejmijKlienta()
    {
        setIloscKlientowWKolejceLabel(ilosc_klientow_w_kolejce.decrementAndGet());
    }

    public void setObslugaRate(double rate)
    {
        this.rate = rate;
        czy_zmiany = true;
    }
    private void setObslugaRate()
    {
        obsluga_rate = rate*ilosc_kolejek;
        czas_pokazania_kolejnego_klienta = (ilosc_kolejek+1)*czas_podejscia_do_kasy;
        if(rate < 1)
            czas_pokazania_kolejnego_klienta = (int) ((1/rate)*(ilosc_kolejek+1)*czas_podejscia_do_kasy);
        czy_zmiany = false;
    }

    private void wstrzymajKolejke()
    {
        czy_wstzymana = true;
        Platform.runLater(() -> {
            kolejka.setFill(Color.GRAY);
        });
    }
    private void wznowKolejke()
    {
        czy_wstzymana = false;
        Platform.runLater(() -> {
            kolejka.setFill(Color.TRANSPARENT);
            kolejka.setStroke(Color.BLACK);
        });
    }

    public boolean czyKolejkaWstrzymana()
    {
        return czy_wstzymana;
    }

    public void zarzadzPrzerwe()
    {
        czy_chce_przerwe.set(true);
        Platform.runLater(() -> {
            kolejka.setStroke(Color.RED);
        });
    }

    public void koniecPrzerwy()
    {
        czy_chce_przerwe.set(false);
        wznowKolejke();
    }

    public boolean czyChcePrzerwa()
    {
        return czy_chce_przerwe.get();
    }

    private void randKolor()
    {
        Random random = new Random();

        r = (random.nextInt(255)*nr)%255;

        g = (random.nextInt(255)*nr)%255;

        b = (random.nextInt(255)*nr)%255;
    }

    public void zamknijKolejke()
    {
        System.out.println(getName() + " kończy działanie ");
        czy_dziala = false;
    }

    @FXML
    public void setIloscKlientowWKolejceLabel(int value){
        if(value >= 0) {
            Platform.runLater(() -> {
                ilosc_klientow_label.setText(String.valueOf(value));
            });
        }
    }
}
