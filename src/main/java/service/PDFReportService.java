package service;

import model.Person;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PDFReportService {

    private static final PDType1Font FONT_REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    public void generateReport(File file, List<Person> data) {

        try (PDDocument document = new PDDocument()) {

            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream content = new PDPageContentStream(document, page);

            // Title
            content.beginText();

            content.setFont(FONT_BOLD, 18);
            content.newLineAtOffset(200, 750);
            content.showText("Student Report by Major");
            content.endText();

            // Date
            content.beginText();
            content.setFont(FONT_REGULAR, 10);
            content.newLineAtOffset(200, 730);
            content.showText("Generated on: " + LocalDate.now());
            content.endText();

            // Count students per major
            Map<String, Integer> majorCount = new HashMap<>();
            for (Person p : data) {
                String major = p.getMajor().toString();
                majorCount.put(major, majorCount.getOrDefault(major, 0) + 1);
            }

            // Start writing results
            int yPosition = 700;

            content.setFont(FONT_REGULAR, 12);

            for (Map.Entry<String, Integer> entry : majorCount.entrySet()) {

                content.beginText();
                content.newLineAtOffset(100, yPosition);
                content.showText("Major: " + entry.getKey()
                        + " | Students: " + entry.getValue());
                content.endText();

                yPosition -= 20;
            }

            content.close();

            document.save(file);

            MyLogger.makeLog("PDF report generated successfully.");

        } catch (IOException e) {
            e.printStackTrace();
            MyLogger.makeLog("PDF report generation failed.");
        }
    }
}