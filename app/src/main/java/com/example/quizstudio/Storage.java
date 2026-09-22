package com.example.quizstudio;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Storage {

    private static final String FILE_NAME = "questions.json";

    private final Context context;

    public Storage(Context context) {
        this.context = context.getApplicationContext();
    }

    public List<Question> loadQuestions() {
        List<Question> questions = new ArrayList<>();

        try {
            File file = new File(context.getFilesDir(), FILE_NAME);

            if (!file.exists()) {
                return questions;
            }

            FileInputStream input = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            int read = input.read(data);
            input.close();

            if (read <= 0) {
                return questions;
            }

            String jsonText = new String(data, StandardCharsets.UTF_8);
            JSONArray array = new JSONArray(jsonText);

            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);

                Question question = new Question();

                question.setId(object.optString("id", ""));
                question.setSubject(object.optString("subject", ""));
                question.setReference(object.optString("reference", ""));
                question.setText(object.optString("text", ""));
                question.setOptionA(object.optString("optionA", ""));
                question.setOptionB(object.optString("optionB", ""));
                question.setOptionC(object.optString("optionC", ""));
                question.setOptionD(object.optString("optionD", ""));
                question.setCorrectAnswer(
                        object.optString("correctAnswer", "")
                );

                questions.add(question);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return questions;
    }

    public void saveQuestions(List<Question> questions) {

        try {
            JSONArray array = new JSONArray();

            for (Question question : questions) {

                JSONObject object = new JSONObject();

                object.put("id", question.getId());
                object.put("subject", question.getSubject());
                object.put("reference", question.getReference());
                object.put("text", question.getText());
                object.put("optionA", question.getOptionA());
                object.put("optionB", question.getOptionB());
                object.put("optionC", question.getOptionC());
                object.put("optionD", question.getOptionD());
                object.put(
                        "correctAnswer",
                        question.getCorrectAnswer()
                );

                array.put(object);
            }

            File file = new File(context.getFilesDir(), FILE_NAME);

            FileOutputStream output = new FileOutputStream(file);
            output.write(
                    array.toString(2).getBytes(StandardCharsets.UTF_8)
            );
            output.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
