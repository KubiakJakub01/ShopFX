package com.example.projektfx;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

     //Kasy i klienci
    @FXML
    public AnchorPane kasyKlienciPane;
    @FXML
    public TextField liczbaKasText;
    @FXML
    public TextField maxKlientowText;
    @FXML
    public Button saveKasyKlienciBtn;
    @FXML
    public Label warningKasaKlientLb;
    
    //Czasy
    @FXML
    public AnchorPane czasPane;
    @FXML
    public TextField czasWejsciaDoSklepuText;
    @FXML
    public TextField czasPodejsciaDoKolejkiText;
    @FXML
    public TextField czasPodejsciaDoKasyTesxt;
    @FXML
    public Button saveTimeBtn;
    @FXML
    public TextField czasDoPrzerwyText;
    @FXML
    public TextField czasPrzerwyText;
    @FXML
    public Label warningCzasLb;

    //Start/stop i ustawienia
    @FXML
    public Button stopPlayBtn;
    
    //Slidery
    @FXML
    public Slider obslugaSlider;
    private double predkoscObsluga;
    @FXML
    public Slider sklepSlider;
    private double predkoscSklep;
    @FXML
    public Slider kolejkaSlider;
    private double predkoscKolejka;

    //Stage stage;
    private final static int X = 1800;
    private final static int Y = 800;

    //Sklep
    public Sklep sklep;
    private int ilosc_kas;
    private int max_ilosc_klientow;
    private boolean czy_sklep_wstrzymany;
    private boolean czy_sklep_dziala;
    private int czas_wejscia_do_sklepu;
    private int czas_podejscia_do_kolejki;
    private int czas_podejscia_do_kasy;
    private int czas_do_przerwy;
    private int czas_przerwy;


    public Controller()
    {
        System.out.println("Witam w controllerze");
        czy_sklep_wstrzymany = false;
        czy_sklep_dziala = false;
    }

    public void onSaveKasyKlienciBtn(ActionEvent actionEvent) {
        try {
            ilosc_kas = Integer.parseInt(liczbaKasText.getText());
            max_ilosc_klientow = Integer.parseInt(maxKlientowText.getText());
            if(ilosc_kas <= 0 || ilosc_kas >= 6)
            {
                warningKasaKlientLb.setText("Niepoporawna ilość kas");
                warningKasaKlientLb.setTextFill(Color.BLACK);
            }
            else {
                warningKasaKlientLb.setText("Dane wprowadzone poprawnie");
                warningKasaKlientLb.setTextFill(Color.GREEN);
                liczbaKasText.setDisable(true);
                maxKlientowText.setDisable(true);
                saveKasyKlienciBtn.setDisable(true);

                odblokujSegmentZEdycja();
            }
        }catch (NumberFormatException e) {
            warningKasaKlientLb.setText("Niepoporawne dane");
            warningKasaKlientLb.setTextFill(Color.RED);
        }
    }

    public void onSaveTimeBtn(ActionEvent event) {
        try {
            czas_wejscia_do_sklepu = Integer.parseInt(czasWejsciaDoSklepuText.getText());
            czas_podejscia_do_kolejki = Integer.parseInt(czasPodejsciaDoKolejkiText.getText());
            czas_podejscia_do_kasy = Integer.parseInt(czasPodejsciaDoKasyTesxt.getText());
            czas_do_przerwy = Integer.parseInt(czasDoPrzerwyText.getText());
            czas_przerwy = Integer.parseInt(czasPrzerwyText.getText());

            warningCzasLb.setText("Dane wprowadzone poprawnie");
            warningCzasLb.setTextFill(Color.GREEN);

            if(!czy_sklep_dziala)
            {
                czy_sklep_dziala = true;
                zacznijPraceSklepu();
            }
            else
            {
                onStopPlayBtnAction(new ActionEvent());
            }
            stopPlayBtn.setDisable(false);

            zablokujSegmentZEdycja();

        }catch (NumberFormatException e){
            warningCzasLb.setText("Niepoporawne dane");
            warningCzasLb.setTextFill(Color.RED);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    public void onStopPlayBtnAction(ActionEvent actionEvent) throws InterruptedException {
        if(!czy_sklep_wstrzymany) {
            czy_sklep_wstrzymany = true;
            sklep.wstrzymajSklep();
            stopPlayBtn.setText("START");
            stopPlayBtn.setTextFill(Color.GREEN);
        }
        else
        {
            czy_sklep_wstrzymany = false;
            sklep.wznowSklep();
            stopPlayBtn.setText("STOP");
            stopPlayBtn.setTextFill(Color.RED);
        }

    }



    private void zablokujSegmentZEdycja()
    {
        czasWejsciaDoSklepuText.setDisable(true);
        czasPodejsciaDoKolejkiText.setDisable(true);
        czasPodejsciaDoKasyTesxt.setDisable(true);
        czasDoPrzerwyText.setDisable(true);
        czasPrzerwyText.setDisable(true);
        saveTimeBtn.setDisable(true);
    }

    private void odblokujSegmentZEdycja()
    {
        czasWejsciaDoSklepuText.setDisable(false);
        czasPodejsciaDoKolejkiText.setDisable(false);
        czasPodejsciaDoKasyTesxt.setDisable(false);
        czasDoPrzerwyText.setDisable(false);
        czasPrzerwyText.setDisable(false);
        saveTimeBtn.setDisable(false);
    }

    private void zacznijPraceSklepu()
    {
        sklep = new Sklep(ilosc_kas, max_ilosc_klientow,czas_podejscia_do_kasy,czas_wejscia_do_sklepu,czas_podejscia_do_kolejki,czas_do_przerwy,czas_przerwy, X);
        sklep.zacznijPraceSklepu();
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        obslugaSlider.valueChangingProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                predkoscObsluga = obslugaSlider.getValue();
                if(predkoscObsluga == 0) predkoscObsluga = 0.1;
                sklep.setObslugaRate(predkoscObsluga);
            }
        });

        sklepSlider.valueChangingProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                predkoscSklep = sklepSlider.getValue();
                if(predkoscSklep == 0) predkoscSklep = 0.1;
                sklep.setSklepRate(predkoscSklep);
            }
        });

        kolejkaSlider.valueChangingProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                predkoscKolejka = kolejkaSlider.getValue();
                if(predkoscKolejka == 0) predkoscKolejka = 0.1;
                sklep.setKolejkaRate(predkoscKolejka);
            }
        });
    }
}