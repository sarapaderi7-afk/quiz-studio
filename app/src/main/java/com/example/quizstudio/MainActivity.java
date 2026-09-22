package com.example.quizstudio;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

    private List<Question> questions;
    private int indiceDomanda = 0;

    private TextView numeroDomanda;
    private TextView testoDomanda;
    private TextView risultato;
    private TextView rispostaCorretta;

    private Button pulsanteA;
    private Button pulsanteB;
    private Button pulsanteC;
    private Button pulsanteD;
    private Button prossima;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        questions = JsonQuestionLoader.loadEconomia(this);

        if (questions.isEmpty()) {
            mostraErrore();
            return;
        }

        // Mischia le domande ogni volta che parte un nuovo quiz.
        Collections.shuffle(questions);

        creaSchermataQuiz();
        mostraDomanda();
    }

    private void creaSchermataQuiz() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(35, 50, 35, 35);

        numeroDomanda = new TextView(this);
        numeroDomanda.setTextSize(18);
        numeroDomanda.setTextColor(Color.DKGRAY);
        numeroDomanda.setPadding(0, 0, 0, 20);

        testoDomanda = new TextView(this);
        testoDomanda.setTextSize(22);
        testoDomanda.setTextColor(Color.BLACK);
        testoDomanda.setPadding(0, 0, 0, 35);

        pulsanteA = creaPulsante();
        pulsanteB = creaPulsante();
        pulsanteC = creaPulsante();
        pulsanteD = creaPulsante();

        pulsanteA.setOnClickListener(v -> controllaRisposta("A"));
        pulsanteB.setOnClickListener(v -> controllaRisposta("B"));
        pulsanteC.setOnClickListener(v -> controllaRisposta("C"));
        pulsanteD.setOnClickListener(v -> controllaRisposta("D"));

        risultato = new TextView(this);
        risultato.setTextSize(20);
        risultato.setGravity(Gravity.CENTER);
        risultato.setPadding(0, 30, 0, 10);

        rispostaCorretta = new TextView(this);
        rispostaCorretta.setTextSize(18);
        rispostaCorretta.setTextColor(Color.DKGRAY);
        rispostaCorretta.setGravity(Gravity.CENTER);
        rispostaCorretta.setPadding(0, 0, 0, 20);

        prossima = new Button(this);
        prossima.setText("PROSSIMA DOMANDA");
        prossima.setVisibility(View.GONE);

        prossima.setOnClickListener(v -> {
            indiceDomanda++;

            if (indiceDomanda >= questions.size()) {
                indiceDomanda = 0;
                Collections.shuffle(questions);
            }

            mostraDomanda();
        });

        layout.addView(numeroDomanda);
        layout.addView(testoDomanda);
        layout.addView(pulsanteA);
        layout.addView(pulsanteB);
        layout.addView(pulsanteC);
        layout.addView(pulsanteD);
        layout.addView(risultato);
        layout.addView(rispostaCorretta);
        layout.addView(prossima);

        setContentView(layout);
    }

    private Button creaPulsante() {

        Button button = new Button(this);

        button.setTextSize(17);
        button.setTextColor(Color.BLACK);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(0, 0, 0, 12);

        button.setLayoutParams(params);

        return button;
    }

    private void mostraDomanda() {

        if (questions == null || questions.isEmpty()) {
            return;
        }

        Question question =
                questions.get(indiceDomanda);

        numeroDomanda.setText(
                "Domanda " + (indiceDomanda + 1)
                        + " di " + questions.size()
        );

        testoDomanda.setText(
                question.getText()
        );

        pulsanteA.setText(
                "A. " + question.getOptionA()
        );

        pulsanteB.setText(
                "B. " + question.getOptionB()
        );

        pulsanteC.setText(
                "C. " + question.getOptionC()
        );

        pulsanteD.setText(
                "D. " + question.getOptionD()
        );

        pulsanteA.setEnabled(true);
        pulsanteB.setEnabled(true);
        pulsanteC.setEnabled(true);
        pulsanteD.setEnabled(true);

        risultato.setText("");
        rispostaCorretta.setText("");

        prossima.setVisibility(View.GONE);
    }

    private void controllaRisposta(String rispostaScelta) {

        Question question =
                questions.get(indiceDomanda);

        String corretta =
                question.getCorrectAnswer();

        String testoCorretta;

        switch (corretta) {
            case "A":
                testoCorretta = question.getOptionA();
                break;

            case "B":
                testoCorretta = question.getOptionB();
                break;

            case "C":
                testoCorretta = question.getOptionC();
                break;

            case "D":
                testoCorretta = question.getOptionD();
                break;

            default:
                testoCorretta = "Risposta non disponibile";
                break;
        }

        if (rispostaScelta.equalsIgnoreCase(corretta)) {

            risultato.setText("✓ CORRETTA");
            risultato.setTextColor(Color.rgb(0, 130, 0));

        } else {

            risultato.setText("✗ SBAGLIATA");
            risultato.setTextColor(Color.RED);
        }

        rispostaCorretta.setText(
                "Risposta corretta: "
                        + corretta
                        + ". "
                        + testoCorretta
        );

        pulsanteA.setEnabled(false);
        pulsanteB.setEnabled(false);
        pulsanteC.setEnabled(false);
        pulsanteD.setEnabled(false);

        prossima.setVisibility(View.VISIBLE);
    }

    private void mostraErrore() {

        TextView errore = new TextView(this);

        errore.setText(
                "Nessuna domanda caricata dal JSON."
        );

        errore.setTextSize(20);
        errore.setGravity(Gravity.CENTER);
        errore.setPadding(40, 80, 40, 40);

        setContentView(errore);
    }
}

