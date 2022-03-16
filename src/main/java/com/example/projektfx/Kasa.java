package com.example.projektfx;

import javafx.animation.PathTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.concurrent.locks.Lock;

public class Kasa extends Thread {

    private static final double kasa_Y = 100;
    private static final double kasa_Width = 200;
    private static final int font_size = 30;
    private int nr;
    private int poz_buf;
    private double kasa_X;
    private double kasa_center_X;
    private double kasa_center_Y;
    private Lock dostep;
    private int czas_podejscia_do_kasy;
    private int base_czas_podejscia_do_kasy;
    private double obsluga_rate;
    private int ilosc_kas;
    private double czas_obslugi;
    private int czas_przerwy;
    private double rate;
    private Boolean czy_zmiany;
    private Boolean czy_dziala;
    private Boolean czy_wstzymana;
    public Kolejka kolejka;
    private Circle obecny_klient;
    private Rectangle kasa;
    @FXML
    public Label nazwa_kasy_label;

    public Kasa(int nr, double rectX, Lock lock, int czas_podejscia_do_kasy, int czas_przerwy, int ilosc_kas)
    {
        super("Kasa nr. " + nr);
        this.kasa_X = rectX;
        this.dostep = lock;
        czy_dziala = true;
        czy_wstzymana = false;
        czy_zmiany = false;
        rate = 1;
        kasa_center_X = kasa_X + kasa_Width /2;
        kasa_center_Y = kasa_Y + font_size + kasa_Width /2;
        this.czas_podejscia_do_kasy = czas_podejscia_do_kasy;
        this.base_czas_podejscia_do_kasy = czas_podejscia_do_kasy;
        this.czas_przerwy = czas_przerwy;
        this.ilosc_kas = ilosc_kas;
        this.obsluga_rate = ilosc_kas;
        poz_buf = 0;
        kolejka = new Kolejka(nr, rectX, lock, czas_podejscia_do_kasy, czas_przerwy, ilosc_kas);
        czas_obslugi = 2*czas_podejscia_do_kasy;
        kolejka.start();
    }


    private StackPane stworzKase()
    {
        kasa = new Rectangle(kasa_Width, kasa_Width);
        kasa.setX(kasa_X);
        kasa.setY(kasa_Y);
        kasa.setFill(Color.TRANSPARENT);
        kasa.setStroke(Color.BLACK);
        kasa.setStrokeWidth(2);

        nazwa_kasy_label = new Label(getName());
        nazwa_kasy_label.setFont(new Font("Arial", font_size));

        VBox vbox = new VBox(5); // 5 is the spacing between elements in the VBox
        vbox.getChildren().addAll(nazwa_kasy_label, kasa);
        vbox.setAlignment(Pos.CENTER);

        StackPane pane = new StackPane();
        pane.getChildren().add(vbox);
        pane.setAlignment(vbox, Pos.CENTER);
        pane.setTranslateX(kasa_X);
        pane.setTranslateY(kasa_Y);

        return pane;
    }

    private ScaleTransition scaleKlient(Circle klient)
    {
        ScaleTransition scaleTransition = new ScaleTransition();
        // Setting the duration for the transition
        scaleTransition.setDuration(Duration.millis(czas_obslugi));
        // Setting the node for the transition
        scaleTransition.setNode(klient);
        // Setting the dimensions for scaling
        scaleTransition.setToY(0.2);
        scaleTransition.setToX(0.2);
        // Setting auto reverse value to true
        scaleTransition.setAutoReverse(false);
        scaleTransition.setRate(obsluga_rate);

        return scaleTransition;
    }

    private Path getPath(Circle klient)
    {
        Path path = new Path();
        MoveTo moveTo = new MoveTo();
        moveTo.setX(klient.getCenterX());
        moveTo.setY(klient.getCenterY());
        LineTo lineTo = new LineTo();
        lineTo.setX(kasa_center_X);
        lineTo.setY(kasa_center_Y);
        path.getElements().addAll(moveTo, lineTo);

        return path;
    }
    @Override
    public void run() {
        System.out.println("Kasa start playing: " + super.getName());


        Platform.runLater(() -> {
            Main.root.getChildren().add(stworzKase());
        });

        while(czy_dziala){
            if(czy_zmiany)
            {
                System.out.println(getName() + " Zmiana ustawien");
                setObslugaRate();
                continue;
            }
            if(kolejka.czyKolejkaWstrzymana())
            {
                wstrzymajKase();
                while (kolejka.czyKolejkaWstrzymana()) {
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                wznowKase();
            }


            Circle klient;
            try {
                kolejka.zajete.acquire();
                klient = kolejka.klienci_bufor[poz_buf];
                try {
                    sleep(czas_podejscia_do_kasy);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                kolejka.wolne.release();
                poz_buf = (poz_buf+1)%kolejka.rozmiar_bufora;
                obecny_klient = klient;
                System.out.println(getName() + " obsługa klienta");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            Path path = getPath(obecny_klient);
            PathTransition pathTransition = new PathTransition(Duration.millis(czas_podejscia_do_kasy), path, obecny_klient);

            Platform.runLater(() -> {
                Main.root.getChildren().add(path);
            });

            // Rejestracja reakcji na zakończenie animacji przemieszczania.
            //
            pathTransition.setOnFinished(e -> {
                synchronized (this) {
                    notify();
                }
            });

            dostep.lock();
            try {
                // Uruchomienie animacji przemieszczania.
                //
                pathTransition.play();
            }
            finally {
                // Synchronizacja wątku z animacją przemieszczania.
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

            //Usunięcie ściezki
            Platform.runLater(() -> {
                Main.root.getChildren().remove(path);
            });

            ScaleTransition scaleTransition = scaleKlient(obecny_klient);

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

            System.out.println(getName()+ " usuwanie klienta");
            Platform.runLater(() -> {
                Main.root.getChildren().remove(obecny_klient);
            });
        }

        System.out.println("Koniec wątku");
    }

    public void setObslugaRate(double rate)
    {
        this.rate = rate;
        czy_zmiany = true;
        kolejka.setObslugaRate(rate);
    }

    private void setObslugaRate()
    {
        obsluga_rate = rate*ilosc_kas;
        czas_obslugi = 2*czas_podejscia_do_kasy;
        if(rate < 1)
            czas_obslugi = (int) ((1/rate)*2*czas_podejscia_do_kasy);
        czy_zmiany = false;
    }

    public void zamknijKase()
    {
        System.out.println(getName() + " kończy działanie ");
        kolejka.zamknijKolejke();
        czy_dziala = false;
    }

    private void wstrzymajKase()
    {
        czy_wstzymana = true;
        Platform.runLater(() -> {
            kasa.setFill(Color.GRAY);
        });
    }
    private void wznowKase()
    {
        czy_wstzymana = false;
        Platform.runLater(() -> {
            kasa.setStroke(Color.BLACK);
            kasa.setFill(Color.TRANSPARENT);
        });
    }



}
