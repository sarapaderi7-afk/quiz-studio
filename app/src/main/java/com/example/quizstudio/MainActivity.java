package com.example.quizstudio;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final int PICK_DOCX = 1001;

    private Storage storage;
    private TextView stato;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storage = new Storage(this);

        creaSchermata();
    }

    private void creaSchermata() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 60, 40, 40);

        TextView titolo = new TextView(this);
        titolo.setText("Quiz Studio");
        titolo.setTextSize(30);
        titolo.setTextColor(Color.BLACK);
        titolo.setGravity(Gravity.CENTER);
        titolo.setPadding(0, 0, 0, 30);

        TextView descrizione = new TextView(this);
        descrizione.setText(
                "Importa le tue domande Word e costruisci " +
                "il tuo archivio quiz."
        );
        descrizione.setTextSize(18);
        descrizione.setTextColor(Color.DKGRAY);
        descrizione.setPadding(0, 0, 0, 30);

        Button importa = new Button(this);
        importa.setText("IMPORTA DOMANDE DA WORD");

        importa.setOnClickListener(v ->
                chiediMateriaEApriFile()
        );

        Button quiz = new Button(this);
        quiz.setText("INIZIA QUIZ");

        quiz.setOnClickListener(v ->
                mostraStatoQuiz()
        );

        stato = new TextView(this);
        stato.setTextSize(17);
        stato.setTextColor(Color.DKGRAY);
        stato.setPadding(0, 30, 0, 0);

        aggiornaStato();

        layout.addView(titolo);
        layout.addView(descrizione);
        layout.addView(importa);
        layout.addView(quiz);
        layout.addView(stato);

        setContentView(layout);
    }

    private void chiediMateriaEApriFile() {

        final EditText input = new EditText(this);

        input.setHint("Es. Economia Aziendale");

        LinearLayout contenitore =
                new LinearLayout(this);

        contenitore.setOrientation(
                LinearLayout.VERTICAL
        );

        contenitore.setPadding(
                50, 10, 50, 10
        );

        contenitore.addView(input);

        new android.app.AlertDialog.Builder(this)
                .setTitle("Materia")
                .setMessage(
                        "Inserisci la materia a cui appartengono " +
                        "le domande che stai importando."
                )
                .setView(contenitore)
                .setNegativeButton(
                        "ANNULLA",
                        null
                )
                .setPositiveButton(
                        "SCEGLI WORD",
                        (dialog, which) -> {

                            String materia =
                                    input.getText()
                                            .toString()
                                            .trim();

                            if (materia.isEmpty()) {
                                Toast.makeText(
                                        this,
                                        "Inserisci una materia.",
                                        Toast.LENGTH_SHORT
                                ).show();
                                return;
                            }

                            apriSelettoreDocx(materia);
                        }
                )
                .show();
    }

    private void apriSelettoreDocx(
            String materia) {

        Intent intent =
                new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        intent.setType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        );

        intent.putExtra(
                "materia",
                materia
        );

        startActivityForResult(
                intent,
                PICK_DOCX
        );
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode != PICK_DOCX
                || resultCode != RESULT_OK
                || data == null) {

            return;
        }

        Uri uri = data.getData();

        if (uri == null) {
            return;
        }

        String materia =
                data.getStringExtra("materia");

        if (materia == null
                || materia.trim().isEmpty()) {

            materia = "Senza materia";
        }

        importaFile(uri, materia);
    }

    private void importaFile(
            Uri uri,
            String materia) {

        DocxParser.ParseResult result =
                DocxParser.parse(
                        this,
                        uri,
                        materia
                );

        List<Question> vecchie =
                storage.loadQuestions();

        List<Question> nuove =
                new ArrayList<>();

        int duplicati = 0;

        for (Question nuova : result.questions) {

            boolean giaPresente = false;

            for (Question vecchia : vecchie) {

                if (vecchia.getSubject()
                        .equalsIgnoreCase(
                                nuova.getSubject()
                        )
                        && vecchia.getText()
                        .trim()
                        .equalsIgnoreCase(
                                nuova.getText()
                                        .trim()
                        )) {

                    giaPresente = true;
                    break;
                }
            }

            if (giaPresente) {
                duplicati++;
            } else {
                nuove.add(nuova);
            }
        }

        vecchie.addAll(nuove);

        storage.saveQuestions(vecchie);

        String messaggio =
                "Importate: " + nuove.size()
                        + "\nDuplicati ignorati: "
                        + duplicati;

        if (!result.warnings.isEmpty()) {

            messaggio +=
                    "\n\nDa controllare: "
                    + result.warnings.size();
        }

        Toast.makeText(
                this,
                messaggio,
                Toast.LENGTH_LONG
        ).show();

        aggiornaStato();
    }

    private void aggiornaStato() {

        if (stato == null) {
            return;
        }

        List<Question> questions =
                storage.loadQuestions();

        stato.setText(
                "Domande archiviate: "
                        + questions.size()
        );
    }

    private void mostraStatoQuiz() {

        List<Question> questions =
                JsonQuestionLoader.loadEconomia(this);

        if (questions.isEmpty()) {

            Toast.makeText(
                    this,
                    "Nessuna domanda caricata dal JSON.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        Toast.makeText(
                this,
                "Caricate " + questions.size() + " domande.",
                Toast.LENGTH_SHORT
        ).show();
    }
}
