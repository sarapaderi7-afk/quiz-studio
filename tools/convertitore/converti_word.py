import json
import re
import sys
import zipfile
import xml.etree.ElementTree as ET
from pathlib import Path


NS = {
    "w": "http://schemas.openxmlformats.org/wordprocessingml/2006/main"
}


def get_text(run):
    parts = []

    for node in run.findall(".//w:t", NS):
        parts.append(node.text or "")

    for node in run.findall(".//w:tab", NS):
        parts.append("\t")

    return "".join(parts)


def run_is_bold(run):
    rpr = run.find("w:rPr", NS)

    if rpr is None:
        return False

    return (
        rpr.find("w:b", NS) is not None
        or rpr.find("w:bCs", NS) is not None
    )


def run_is_underlined(run):
    rpr = run.find("w:rPr", NS)

    if rpr is None:
        return False

    underline = rpr.find("w:u", NS)

    if underline is None:
        return False

    value = underline.get(
        "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}val"
    )

    return value not in ("none", "0")


def read_docx_paragraphs(docx_path):
    with zipfile.ZipFile(docx_path, "r") as z:
        xml_data = z.read("word/document.xml")

    root = ET.fromstring(xml_data)

    paragraphs = []

    for paragraph in root.findall(".//w:p", NS):
        runs = []

        for run in paragraph.findall("./w:r", NS):
            text = get_text(run)

            if not text:
                continue

            runs.append({
                "text": text,
                "bold": run_is_bold(run),
                "underline": run_is_underlined(run)
            })

        if runs:
            text = "".join(run["text"] for run in runs).strip()

            if text:
                paragraphs.append({
                    "text": text,
                    "runs": runs
                })

    return paragraphs


def paragraph_is_correct(paragraph):
    if not paragraph["runs"]:
        return False

    return all(
        run["bold"] and run["underline"]
        for run in paragraph["runs"]
        if run["text"].strip()
    )


def clean_text(text):
    text = re.sub(r"\s+", " ", text)
    return text.strip()


def parse_questions(paragraphs):
    questions = []

    current = None
    current_option = None
    pending_option = None

    question_pattern = re.compile(r"^\s*(\d+)[\.\)]\s*(.*)$")
    option_pattern = re.compile(r"^\s*([ABCD])\s*$", re.IGNORECASE)

    for paragraph in paragraphs:
        text = clean_text(paragraph["text"])

        if not text:
            continue

        # Paragrafo di riferimento
        if text.lower().startswith("paragrafo di riferimento"):
            if current is not None:
                current["reference"] = text
            continue

        # Nuova domanda
        match_question = question_pattern.match(text)

        if match_question:
            if current is not None:
                questions.append(current)

            current = {
                "domanda": clean_text(match_question.group(2)),
                "A": "",
                "B": "",
                "C": "",
                "D": "",
                "corretta": "",
                "_correct_count": 0
            }

            current_option = None
            pending_option = None
            continue

        if current is None:
            continue

        # Una lettera A/B/C/D da sola.
        # Nel documento Word la lettera e il testo della risposta
        # sono in due paragrafi separati.
        match_option = option_pattern.match(text)

        if match_option:
            letter = match_option.group(1).upper()

            pending_option = {
                "letter": letter,
                "correct": paragraph_is_correct(paragraph)
            }

            current_option = letter
            continue

        # Testo della risposta dopo la lettera A/B/C/D
        if pending_option is not None:
            letter = pending_option["letter"]

            current[letter] = clean_text(text)

            # La risposta è corretta se la lettera e il testo
            # sono entrambi grassetto + sottolineato.
            if (
                pending_option["correct"]
                and paragraph_is_correct(paragraph)
            ):
                current["corretta"] = letter
                current["_correct_count"] += 1

            pending_option = None
            continue

        # Continuazione del testo della risposta
        if current_option is not None:
            current[current_option] = clean_text(
                current[current_option] + " " + text
            )

            if paragraph_is_correct(paragraph):
                if current["corretta"] == "":
                    current["corretta"] = current_option
                    current["_correct_count"] += 1

        # Altrimenti è continuazione della domanda
        else:
            current["domanda"] = clean_text(
                current["domanda"] + " " + text
            )

    if current is not None:
        questions.append(current)

    risultato = []

    for numero, question in enumerate(questions, start=1):
        for key in ("domanda", "A", "B", "C", "D"):
            question[key] = clean_text(question[key])

        correct_count = question.pop("_correct_count", 0)

        question["numero"] = numero

        if correct_count != 1:
            question["_errore"] = (
                "Risposta corretta non identificata in modo univoco"
            )

        risultato.append(question)

    return risultato


def convert_file(input_file, output_file):
    print(f"Lettura: {input_file}")

    paragraphs = read_docx_paragraphs(input_file)

    print(f"Paragrafi trovati: {len(paragraphs)}")

    questions = parse_questions(paragraphs)

    print(f"Domande trovate: {len(questions)}")

    with open(output_file, "w", encoding="utf-8") as f:
        json.dump(
            questions,
            f,
            ensure_ascii=False,
            indent=2
        )

    errors = [
        q for q in questions
        if "_errore" in q
    ]

    if errors:
        print()
        print(f"ATTENZIONE: {len(errors)} domande da controllare.")

        for q in errors:
            print(f"  Domanda {q['numero']}: {q['domanda']}")
    else:
        print("Tutte le risposte corrette sono state riconosciute.")

    print()
    print(f"Creato: {output_file}")


def main():
    if len(sys.argv) != 3:
        print("Uso:")
        print("  python3 converti_word.py file.docx file.json")
        sys.exit(1)

    input_file = Path(sys.argv[1])
    output_file = Path(sys.argv[2])

    if not input_file.exists():
        print(f"File non trovato: {input_file}")
        sys.exit(1)

    convert_file(input_file, output_file)


if __name__ == "__main__":
    main()
