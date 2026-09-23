package com.example.quizstudio;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends Activity {

    private List<Question> questions;
    private int indiceDomanda = 0;

    private String materiaCorrente;

    private boolean modalitaTest30 = false;
    private int punteggioTest30 = 0;

    private final Map<String, List<Question>> domandeDisponibili =
            new HashMap<>();

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

        mostraMenuMaterie();
    }

    private void mostraMenuMaterie() {

        LinearLayout layout = new LinearLayout(this);

        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(40, 50, 40, 40);

        TextView titolo = new TextView(this);

        titolo.setText("QUIZ STUDIO");
        titolo.setTextSize(30);
        titolo.setTextColor(Color.BLACK);
        titolo.setGravity(Gravity.CENTER);
        titolo.setPadding(0, 0, 0, 50);

        TextView sottotitolo = new TextView(this);

        sottotitolo.setText("Scegli la materia");
        sottotitolo.setTextSize(21);
        sottotitolo.setTextColor(Color.DKGRAY);
        sottotitolo.setGravity(Gravity.CENTER);
        sottotitolo.setPadding(0, 0, 0, 30);

        Button economia = creaBottoneMenu(
                "Economia e gestione imprese"
        );

        Button psicologia = creaBottoneMenu(
                "Psicologia del Lavoro e delle Organizzazioni"
        );

        Button dirittoLavoro = creaBottoneMenu(
                "Diritto del lavoro"
        );

        Button dirittoPenalePa = creaBottoneMenu(
                "Diritto penale PA"
        );

        Button fondamentiSpagnolo = creaBottoneMenu(
                "Fondamenti di Spagnolo"
        );

        Button psicologiaSociale = creaBottoneMenu(
                "Psicologia sociale"
        );

        Button sociologia = creaBottoneMenu(
                "Sociologia dei processi culturali e comunicativi"
        );

        economia.setOnClickListener(v ->
                scegliModalita(
                        "economia.json",
                        "Economia e gestione imprese"
                )
        );

        psicologia.setOnClickListener(v ->
                scegliModalita(
                        "psicologia.json",
                        "Psicologia del Lavoro e delle Organizzazioni"
                )
        );

        dirittoLavoro.setOnClickListener(v ->
                scegliModalita(
                        "diritto_lavoro.json",
                        "Diritto del lavoro"
                )
        );

        dirittoPenalePa.setOnClickListener(v ->
                scegliModalita(
                        "diritto_penale_pa.json",
                        "Diritto penale PA"
                )
        );

        fondamentiSpagnolo.setOnClickListener(v ->
                scegliModalita(
                        "fondamenti_spagnolo.json",
                        "Fondamenti di Spagnolo"
                )
        );

        psicologiaSociale.setOnClickListener(v ->
                scegliModalita(
                        "psicologia_sociale.json",
                        "Psicologia sociale"
                )
        );

        sociologia.setOnClickListener(v ->
                scegliModalita(
                        "sociologia_processi_culturali_comunicativi.json",
                        "Sociologia dei processi culturali e comunicativi"
                )
        );

        layout.addView(titolo);
        layout.addView(sottotitolo);
        layout.addView(economia);
        layout.addView(psicologia);
        layout.addView(dirittoLavoro);
        layout.addView(dirittoPenalePa);
        layout.addView(fondamentiSpagnolo);
        layout.addView(psicologiaSociale);
        layout.addView(sociologia);

        setContentView(layout);
    }

    private Button creaBottoneMenu(String testo) {

        Button button = new Button(this);

        button.setText(testo);
        button.setTextSize(17);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(0, 0, 0, 20);

        button.setLayoutParams(params);

        return button;
    }

    private void scegliModalita(
            String fileName,
            String subjectName) {

        LinearLayout layout = new LinearLayout(this);

        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(40, 50, 40, 40);

        TextView titolo = new TextView(this);

        titolo.setText(subjectName);
        titolo.setTextSize(24);
        titolo.setGravity(Gravity.CENTER);
        titolo.setPadding(0, 0, 0, 40);

        Button quizInfinito = new Button(this);

        quizInfinito.setText("QUIZ INFINITO");
        quizInfinito.setTextSize(18);

        Button test30 = new Button(this);

        test30.setText("TEST DA 30");
        test30.setTextSize(18);

        quizInfinito.setOnClickListener(v ->
                avviaQuiz(fileName, subjectName)
        );

        test30.setOnClickListener(v ->
                avviaTest30(fileName, subjectName)
        );

        layout.addView(titolo);
        layout.addView(quizInfinito);
        layout.addView(test30);

        setContentView(layout);
    }

    private void preparaDomandeMateria(
            String fileName,
            String subjectName) {

        if (domandeDisponibili.containsKey(subjectName)
                && !domandeDisponibili.get(subjectName).isEmpty()) {

            materiaCorrente = subjectName;
            return;
        }

        List<Question> tutte =
                JsonQuestionLoader.loadMateria(
                        this,
                        fileName,
                        subjectName
                );

        if (tutte.isEmpty()) {
            return;
        }

        SharedPreferences prefs =
                getSharedPreferences(
                        "quiz_studio_progressi",
                        MODE_PRIVATE
                );

        Set<String> usate =
                new HashSet<>(
                        prefs.getStringSet(
                                "usate_" + subjectName,
                                new HashSet<>()
                        )
                );

        List<Question> nuovoMazzo =
                new ArrayList<>();

        for (Question question : tutte) {

            String id = question.getText();

            if (!usate.contains(id)) {
                nuovoMazzo.add(question);
            }
        }

        /*
         * Se tutte le domande della materia sono state
         * utilizzate, iniziamo un nuovo giro.
         */
        if (nuovoMazzo.isEmpty()) {

            usate.clear();

            prefs.edit()
                    .putStringSet(
                            "usate_" + subjectName,
                            usate
                    )
                    .apply();

            nuovoMazzo =
                    new ArrayList<>(tutte);
        }

        Collections.shuffle(nuovoMazzo);

        domandeDisponibili.put(
                subjectName,
                nuovoMazzo
        );

        materiaCorrente = subjectName;
    }

    private Question prossimaDomandaCasuale(
            String fileName,
            String subjectName) {

        preparaDomandeMateria(
                fileName,
                subjectName
        );

        List<Question> mazzo =
                domandeDisponibili.get(subjectName);

        if (mazzo == null || mazzo.isEmpty()) {
            return null;
        }

        Question question =
                mazzo.remove(0);

        SharedPreferences prefs =
                getSharedPreferences(
                        "quiz_studio_progressi",
                        MODE_PRIVATE
                );

        Set<String> usate =
                new HashSet<>(
                        prefs.getStringSet(
                                "usate_" + subjectName,
                                new HashSet<>()
                        )
                );

        usate.add(question.getText());

        prefs.edit()
                .putStringSet(
                        "usate_" + subjectName,
                        usate
                )
                .apply();

        return question;
    }

    private void avviaQuiz(
            String fileName,
            String subjectName) {

        preparaDomandeMateria(
                fileName,
                subjectName
        );

        questions = new ArrayList<>();

        Question primaDomanda =
                prossimaDomandaCasuale(
                        fileName,
                        subjectName
                );

        if (primaDomanda == null) {

            mostraErrore(
                    "Nessuna domanda trovata per "
                            + subjectName
            );

            return;
        }

        questions.add(primaDomanda);

        modalitaTest30 = false;
        punteggioTest30 = 0;
        indiceDomanda = 0;

        creaSchermataQuiz();

        mostraDomanda();
    }

    private void avviaTest30(
            String fileName,
            String subjectName) {

        List<Question> tutte =
                JsonQuestionLoader.loadMateria(
                        this,
                        fileName,
                        subjectName
                );

        if (tutte.size() < 30) {

            mostraErrore(
                    "Questa materia contiene meno di 30 domande."
            );

            return;
        }

        questions = new ArrayList<>();

        modalitaTest30 = true;
        punteggioTest30 = 0;

        for (int i = 0; i < 30; i++) {

            Question question =
                    prossimaDomandaCasuale(
                            fileName,
                            subjectName
                    );

            if (question != null) {
                questions.add(question);
            }
        }

        if (questions.size() < 30) {

            mostraErrore(
                    "Non è stato possibile preparare il test."
            );

            return;
        }

        indiceDomanda = 0;

        creaSchermataQuiz();

        mostraDomanda();
    }

    private void mostraRisultatoTest30() {

        LinearLayout layout = new LinearLayout(this);

        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(40, 60, 40, 40);

        TextView titolo = new TextView(this);

        titolo.setText("TEST COMPLETATO");
        titolo.setTextSize(28);
        titolo.setGravity(Gravity.CENTER);
        titolo.setPadding(0, 0, 0, 35);

        TextView punteggio = new TextView(this);

        punteggio.setText(
                "Punteggio: "
                        + punteggioTest30
                        + "/30"
        );

        punteggio.setTextSize(30);
        punteggio.setGravity(Gravity.CENTER);
        punteggio.setPadding(0, 0, 0, 40);

        TextView dettaglio = new TextView(this);

        dettaglio.setText(
                "Risposte corrette: "
                        + punteggioTest30
                        + "\nRisposte sbagliate: "
                        + (30 - punteggioTest30)
        );

        dettaglio.setTextSize(20);
        dettaglio.setGravity(Gravity.CENTER);
        dettaglio.setPadding(0, 0, 0, 40);

        Button nuovoTest = new Button(this);

        nuovoTest.setText("NUOVO TEST DA 30");
        nuovoTest.setTextSize(18);

        Button menu = new Button(this);

        menu.setText("TORNA ALLE MATERIE");
        menu.setTextSize(18);

        nuovoTest.setOnClickListener(v ->
                avviaTest30(
                        getFileNameMateria(materiaCorrente),
                        materiaCorrente
                )
        );

        menu.setOnClickListener(v ->
                mostraMenuMaterie()
        );

        layout.addView(titolo);
        layout.addView(punteggio);
        layout.addView(dettaglio);
        layout.addView(nuovoTest);
        layout.addView(menu);

        setContentView(layout);
    }

    private String getFileNameMateria(String subjectName) {

        switch (subjectName) {

            case "Economia e gestione imprese":
                return "economia.json";

            case "Psicologia del Lavoro e delle Organizzazioni":
                return "psicologia.json";

            case "Diritto del lavoro":
                return "diritto_lavoro.json";

            case "Diritto penale PA":
                return "diritto_penale_pa.json";

            case "Fondamenti di Spagnolo":
                return "fondamenti_spagnolo.json";

            case "Psicologia sociale":
                return "psicologia_sociale.json";

            case "Sociologia dei processi culturali e comunicativi":
                return "sociologia_processi_culturali_comunicativi.json";

            default:
                return "";
        }
    }

    private void creaSchermataQuiz() {

        ScrollView scrollView = new ScrollView(this);

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

        pulsanteA.setOnClickListener(
                v -> controllaRisposta("A")
        );

        pulsanteB.setOnClickListener(
                v -> controllaRisposta("B")
        );

        pulsanteC.setOnClickListener(
                v -> controllaRisposta("C")
        );

        pulsanteD.setOnClickListener(
                v -> controllaRisposta("D")
        );

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

            if (modalitaTest30
                    && indiceDomanda >= questions.size()) {

                mostraRisultatoTest30();
                return;
            }

            if (!modalitaTest30
                    && indiceDomanda >= questions.size()) {

                Question nuovaDomanda =
                        prossimaDomandaCasuale(
                                getFileNameMateria(materiaCorrente),
                                materiaCorrente
                        );

                if (nuovaDomanda != null) {

                    questions.add(nuovaDomanda);
                }
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

        scrollView.addView(layout);

        setContentView(scrollView);
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
                "Domanda "
                        + (indiceDomanda + 1)
                        + " di "
                        + questions.size()
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

    private void controllaRisposta(
            String rispostaScelta) {

        Question question =
                questions.get(indiceDomanda);

        String corretta =
                question.getCorrectAnswer();

        String testoCorretta;

        switch (corretta) {

            case "A":
                testoCorretta =
                        question.getOptionA();
                break;

            case "B":
                testoCorretta =
                        question.getOptionB();
                break;

            case "C":
                testoCorretta =
                        question.getOptionC();
                break;

            case "D":
                testoCorretta =
                        question.getOptionD();
                break;

            default:
                testoCorretta =
                        "Risposta non disponibile";
                break;
        }

        if (rispostaScelta.equalsIgnoreCase(corretta)) {

            if (modalitaTest30) {
                punteggioTest30++;
            }

            risultato.setText("✓ CORRETTA");
            risultato.setTextColor(
                    Color.rgb(0, 130, 0)
            );

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

        if (modalitaTest30
                && indiceDomanda == questions.size() - 1) {

            prossima.setText("VEDI RISULTATO");
        } else {

            prossima.setText("PROSSIMA DOMANDA");
        }

        prossima.setVisibility(View.VISIBLE);
    }

    private void mostraErrore(String messaggio) {

        TextView errore = new TextView(this);

        errore.setText(messaggio);

        errore.setTextSize(20);
        errore.setGravity(Gravity.CENTER);
        errore.setPadding(40, 80, 40, 40);

        setContentView(errore);
    }
}
