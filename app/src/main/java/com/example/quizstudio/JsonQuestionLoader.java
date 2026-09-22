package com.example.quizstudio;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JsonQuestionLoader {

    public static List<Question> loadEconomia(Context context) {

        List<Question> questions = new ArrayList<>();

        try {
            InputStream inputStream =
                    context.getAssets()
                            .open("domande/economia.json");

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    inputStream,
                                    StandardCharsets.UTF_8
                            )
                    );

            StringBuilder jsonText = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                jsonText.append(line);
            }

            reader.close();
            inputStream.close();

            JSONArray array =
                    new JSONArray(jsonText.toString());

            for (int i = 0; i < array.length(); i++) {

                JSONObject item =
                        array.getJSONObject(i);

                String numero =
                        String.valueOf(
                                item.optInt("numero", i + 1)
                        );

                String domanda =
                        item.optString(
                                "domanda",
                                ""
                        );

                String optionA =
                        item.optString("A", "");

                String optionB =
                        item.optString("B", "");

                String optionC =
                        item.optString("C", "");

                String optionD =
                        item.optString("D", "");

                String corretta =
                        item.optString(
                                "corretta",
                                ""
                        );

                String reference =
                        item.optString(
                                "reference",
                                ""
                        );

                Question question =
                        new Question(
                                "economia-" + numero,
                                "Economia e gestione imprese",
                                reference,
                                domanda,
                                optionA,
                                optionB,
                                optionC,
                                optionD,
                                corretta
                        );

                questions.add(question);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return questions;
    }
}
