package com.example.quizstudio;

import android.content.Context;
import android.net.Uri;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class DocxParser {

    public static class ParseResult {
        public final List<Question> questions;
        public final List<String> warnings;

        public ParseResult(
                List<Question> questions,
                List<String> warnings) {

            this.questions = questions;
            this.warnings = warnings;
        }
    }

    private static class Paragraph {
        String text;
        boolean bold;

        Paragraph(String text, boolean bold) {
            this.text = text;
            this.bold = bold;
        }
    }

    public static ParseResult parse(
            Context context,
            Uri uri,
            String subject) {

        List<Question> questions = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        try {
            Document document = readDocumentXml(context, uri);

            if (document == null) {
                warnings.add("Impossibile leggere il file Word.");
                return new ParseResult(questions, warnings);
            }

            List<Paragraph> paragraphs = extractParagraphs(document);

            Question current = null;
            String currentReference = "";

            for (Paragraph paragraph : paragraphs) {

                String text = cleanText(paragraph.text);

                if (text.isEmpty()) {
                    continue;
                }

                if (isReference(text)) {
                    currentReference = extractReference(text);
                    continue;
                }

                if (isQuestionStart(text)) {

                    if (current != null) {
                        finishQuestion(current, questions, warnings);
                    }

                    current = new Question();

                    current.setId(
                            subject + "_" + questions.size()
                    );

                    current.setSubject(subject);
                    current.setReference(currentReference);
                    current.setText(
                            removeQuestionNumber(text)
                    );

                    continue;
                }

                if (current == null) {
                    continue;
                }

                String optionLetter = getOptionLetter(text);

                if (optionLetter != null) {

                    String optionText =
                            removeOptionLetter(text);

                    setOption(
                            current,
                            optionLetter,
                            optionText
                    );

                    if (paragraph.bold) {
                        current.setCorrectAnswer(
                                optionLetter
                        );
                    }

                    continue;
                }

                appendToQuestion(current, text);
            }

            if (current != null) {
                finishQuestion(current, questions, warnings);
            }

        } catch (Exception e) {
            e.printStackTrace();
            warnings.add(
                    "Errore durante la lettura del file: "
                            + e.getMessage()
            );
        }

        return new ParseResult(questions, warnings);
    }

    private static Document readDocumentXml(
            Context context,
            Uri uri) throws Exception {

        InputStream input =
                context.getContentResolver()
                        .openInputStream(uri);

        if (input == null) {
            return null;
        }

        ZipInputStream zip =
                new ZipInputStream(input);

        ZipEntry entry;

        while ((entry = zip.getNextEntry()) != null) {

            if ("word/document.xml".equals(entry.getName())) {

                DocumentBuilderFactory factory =
                        DocumentBuilderFactory.newInstance();

                factory.setNamespaceAware(true);

                DocumentBuilder builder =
                        factory.newDocumentBuilder();

                Document document =
                        builder.parse(zip);

                zip.close();
                return document;
            }
        }

        zip.close();
        return null;
    }

    private static List<Paragraph> extractParagraphs(
            Document document) {

        List<Paragraph> result =
                new ArrayList<>();

        NodeList nodes =
                document.getElementsByTagNameNS(
                        "http://schemas.openxmlformats.org/wordprocessingml/2006/main",
                        "p"
                );

        for (int i = 0; i < nodes.getLength(); i++) {

            Node paragraphNode = nodes.item(i);

            StringBuilder text =
                    new StringBuilder();

            boolean paragraphBold = false;

            NodeList children =
                    paragraphNode.getChildNodes();

            for (int j = 0; j < children.getLength(); j++) {

                Node run = children.item(j);

                if (!"r".equals(run.getLocalName())) {
                    continue;
                }

                boolean runBold =
                        hasBold(run);

                if (runBold) {
                    paragraphBold = true;
                }

                NodeList runChildren =
                        run.getChildNodes();

                for (int k = 0;
                     k < runChildren.getLength();
                     k++) {

                    Node child =
                            runChildren.item(k);

                    String name =
                            child.getLocalName();

                    if ("t".equals(name)
                            || "tab".equals(name)) {

                        text.append(
                                child.getTextContent()
                        );
                    }
                }
            }

            result.add(
                    new Paragraph(
                            text.toString(),
                            paragraphBold
                    )
            );
        }

        return result;
    }

    private static boolean hasBold(Node run) {

        NodeList children =
                run.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {

            Node child = children.item(i);

            if ("rPr".equals(child.getLocalName())) {

                NodeList properties =
                        child.getChildNodes();

                for (int j = 0;
                     j < properties.getLength();
                     j++) {

                    Node property =
                            properties.item(j);

                    String name =
                            property.getLocalName();

                    if ("b".equals(name)
                            || "bCs".equals(name)) {

                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static boolean isReference(
            String text) {

        return text
                .toLowerCase()
                .startsWith(
                        "paragrafo di riferimento"
                );
    }

    private static String extractReference(
            String text) {

        int dash =
                text.indexOf("-");

        if (dash >= 0
                && dash + 1 < text.length()) {

            return text.substring(dash + 1)
                    .trim();
        }

        return text;
    }

    private static boolean isQuestionStart(
            String text) {

        return text.matches(
                "^\\d+[.)].+"
        );
    }

    private static String removeQuestionNumber(
            String text) {

        return text.replaceFirst(
                "^\\d+[.)]\\s*",
                ""
        ).trim();
    }

    private static String getOptionLetter(
            String text) {

        if (text.matches("^[A-Da-d][.)]\\s*.+")) {
            return text.substring(0, 1)
                    .toUpperCase();
        }

        return null;
    }

    private static String removeOptionLetter(
            String text) {

        return text.replaceFirst(
                "^[A-Da-d][.)]\\s*",
                ""
        ).trim();
    }

    private static void setOption(
            Question question,
            String letter,
            String text) {

        switch (letter) {

            case "A":
                question.setOptionA(text);
                break;

            case "B":
                question.setOptionB(text);
                break;

            case "C":
                question.setOptionC(text);
                break;

            case "D":
                question.setOptionD(text);
                break;
        }
    }

    private static void appendToQuestion(
            Question question,
            String text) {

        String oldText = question.getText();

        if (oldText == null || oldText.isEmpty()) {
            question.setText(text);
        } else {
            question.setText(
                    oldText + " " + text
            );
        }
    }

    private static void finishQuestion(
            Question question,
            List<Question> questions,
            List<String> warnings) {

        boolean complete =
                !isEmpty(question.getText())
                        && !isEmpty(question.getOptionA())
                        && !isEmpty(question.getOptionB())
                        && !isEmpty(question.getOptionC())
                        && !isEmpty(question.getOptionD());

        if (!complete) {
            warnings.add(
                    "Domanda incompleta: "
                            + question.getText()
            );
            return;
        }

        if (isEmpty(question.getCorrectAnswer())) {
            warnings.add(
                    "Risposta corretta non riconosciuta: "
                            + question.getText()
            );
        }

        questions.add(question);
    }

    private static boolean isEmpty(String value) {
        return value == null
                || value.trim().isEmpty();
    }

    private static String cleanText(
            String text) {

        return text
                .replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }
}
