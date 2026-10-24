import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Official PDF Marksheet Generator for KEC ACADEMIX (R2024 Regulations).
 * Generates an institutional semester grade sheet using Apache PDFBox.
 */
public class PdfMarksheetGenerator {

    public static class CourseMarkEntry {
        public final String courseCode;
        public final String courseName;
        public final String courseType;
        public final int credits;
        public final double attendancePct;
        public final double internalMarks;
        public final double maxInternal;
        public final String eseMarks;
        public final double maxEse;
        public final String totalMarks;
        public final String grade;
        public final int gradePoint;
        public final String result;

        public CourseMarkEntry(String courseCode, String courseName, String courseType,
                               int credits, double attendancePct, double internalMarks, double maxInternal,
                               String eseMarks, double maxEse, String totalMarks,
                               String grade, int gradePoint, String result) {
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.courseType = courseType;
            this.credits = credits;
            this.attendancePct = attendancePct;
            this.internalMarks = internalMarks;
            this.maxInternal = maxInternal;
            this.eseMarks = eseMarks;
            this.maxEse = maxEse;
            this.totalMarks = totalMarks;
            this.grade = grade;
            this.gradePoint = gradePoint;
            this.result = result;
        }
    }

    public static void generateMarksheetPdf(
            File outputFile,
            String studentRoll,
            String studentName,
            String department,
            int semester,
            String academicYear,
            List<CourseMarkEntry> courses,
            int totalCreditsRegistered,
            int totalCreditsEarned,
            double sgpa,
            double equivalentPercentage,
            String overallResult
    ) throws IOException {

        if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float margin = 40.0f;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                // Background & Outer Border
                cs.setStrokingColor(new Color(23, 54, 93)); // KEC Navy
                cs.setLineWidth(1.5f);
                cs.addRect(margin, margin, pageWidth - (2 * margin), pageHeight - (2 * margin));
                cs.stroke();

                // Inner Accent Border
                cs.setStrokingColor(new Color(220, 228, 233));
                cs.setLineWidth(0.8f);
                cs.addRect(margin + 3, margin + 3, pageWidth - (2 * margin) - 6, pageHeight - (2 * margin) - 6);
                cs.stroke();

                float y = pageHeight - 65;

                // College Header
                cs.setNonStrokingColor(new Color(23, 54, 93));
                drawCenteredText(cs, "KONGU ENGINEERING COLLEGE", PDType1Font.HELVETICA_BOLD, 17, pageWidth, y);
                y -= 14;

                cs.setNonStrokingColor(new Color(100, 115, 135));
                drawCenteredText(cs, "(Autonomous Institution • Affiliated to Anna University, Chennai)", PDType1Font.HELVETICA, 10, pageWidth, y);
                y -= 12;

                drawCenteredText(cs, "Perundurai, Erode - 638060, Tamil Nadu, India", PDType1Font.HELVETICA, 9, pageWidth, y);
                y -= 16;

                // Dual Accent Brand Stripe
                cs.setNonStrokingColor(new Color(139, 195, 74)); // KEC Green
                cs.addRect(margin + 10, y, (pageWidth - 2 * margin - 20) / 2, 3);
                cs.fill();
                cs.setNonStrokingColor(new Color(0, 165, 216)); // KEC Cyan
                cs.addRect(margin + 10 + (pageWidth - 2 * margin - 20) / 2, y, (pageWidth - 2 * margin - 20) / 2, 3);
                cs.fill();
                y -= 20;

                // Title Banner
                cs.setNonStrokingColor(new Color(23, 54, 93));
                drawCenteredText(cs, "OFFICIAL GRADE STATEMENT — REGULATIONS 2024", PDType1Font.HELVETICA_BOLD, 12, pageWidth, y);
                y -= 25;

                // Student Metadata Box
                float metaBoxX = margin + 12;
                float metaBoxW = pageWidth - 2 * margin - 24;
                float metaBoxH = 58;

                cs.setNonStrokingColor(new Color(246, 248, 251));
                cs.addRect(metaBoxX, y - metaBoxH + 12, metaBoxW, metaBoxH);
                cs.fill();
                cs.setStrokingColor(new Color(220, 228, 233));
                cs.setLineWidth(1.0f);
                cs.addRect(metaBoxX, y - metaBoxH + 12, metaBoxW, metaBoxH);
                cs.stroke();

                cs.setNonStrokingColor(new Color(23, 54, 93));
                float textY = y - 2;

                // Row 1
                cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
                drawText(cs, "Student Name : ", metaBoxX + 12, textY);
                cs.setFont(PDType1Font.HELVETICA, 10);
                drawText(cs, studentName != null ? studentName : "—", metaBoxX + 95, textY);

                cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
                drawText(cs, "Roll Number : ", metaBoxX + 310, textY);
                cs.setFont(PDType1Font.HELVETICA, 10);
                drawText(cs, studentRoll != null ? studentRoll : "—", metaBoxX + 385, textY);

                textY -= 16;
                // Row 2
                cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
                drawText(cs, "Department    : ", metaBoxX + 12, textY);
                cs.setFont(PDType1Font.HELVETICA, 10);
                drawText(cs, department != null ? department : "—", metaBoxX + 95, textY);

                cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
                drawText(cs, "Semester     : ", metaBoxX + 310, textY);
                cs.setFont(PDType1Font.HELVETICA, 10);
                drawText(cs, String.valueOf(semester), metaBoxX + 385, textY);

                textY -= 16;
                // Row 3
                cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
                drawText(cs, "Academic Yr   : ", metaBoxX + 12, textY);
                cs.setFont(PDType1Font.HELVETICA, 10);
                drawText(cs, academicYear != null ? academicYear : "—", metaBoxX + 95, textY);

                cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
                drawText(cs, "Regulations : ", metaBoxX + 310, textY);
                cs.setFont(PDType1Font.HELVETICA, 10);
                drawText(cs, "R2024 (Autonomous)", metaBoxX + 385, textY);

                y -= (metaBoxH + 18);

                // Table Header
                float tableX = margin + 12;
                float tableW = metaBoxW;
                float rowH = 22;

                cs.setNonStrokingColor(new Color(23, 54, 93));
                cs.addRect(tableX, y, tableW, rowH);
                cs.fill();

                cs.setNonStrokingColor(Color.WHITE);
                cs.setFont(PDType1Font.HELVETICA_BOLD, 9);

                float colCode = tableX + 8;
                float colTitle = tableX + 75;
                float colType = tableX + 225;
                float colCred = tableX + 280;
                float colAtt = tableX + 315;
                float colCia = tableX + 355;
                float colEse = tableX + 395;
                float colTot = tableX + 435;
                float colGrd = tableX + 472;
                float colRes = tableX + 495;

                drawText(cs, "Code", colCode, y + 6);
                drawText(cs, "Course Title", colTitle, y + 6);
                drawText(cs, "Type", colType, y + 6);
                drawText(cs, "Cred", colCred, y + 6);
                drawText(cs, "Att %", colAtt, y + 6);
                drawText(cs, "CIA", colCia, y + 6);
                drawText(cs, "ESE", colEse, y + 6);
                drawText(cs, "Total", colTot, y + 6);
                drawText(cs, "Grd", colGrd, y + 6);
                drawText(cs, "Result", colRes, y + 6);

                y -= rowH;

                // Table Data Rows
                boolean alt = false;
                cs.setFont(PDType1Font.HELVETICA, 8.5f);

                for (CourseMarkEntry c : courses) {
                    if (alt) {
                        cs.setNonStrokingColor(new Color(250, 252, 255));
                        cs.addRect(tableX, y, tableW, rowH);
                        cs.fill();
                    }
                    cs.setStrokingColor(new Color(230, 235, 240));
                    cs.setLineWidth(0.5f);
                    cs.addLine(tableX, y, tableX + tableW, y);
                    cs.stroke();

                    cs.setNonStrokingColor(new Color(30, 40, 50));
                    drawText(cs, c.courseCode, colCode, y + 6);

                    String title = c.courseName;
                    if (title != null && title.length() > 26) {
                        title = title.substring(0, 24) + "..";
                    }
                    drawText(cs, title != null ? title : "—", colTitle, y + 6);
                    drawText(cs, c.courseType != null ? c.courseType : "Theory", colType, y + 6);
                    drawText(cs, String.valueOf(c.credits), colCred + 4, y + 6);
                    drawText(cs, String.format("%.0f%%", c.attendancePct), colAtt, y + 6);
                    drawText(cs, String.format("%.1f", c.internalMarks), colCia, y + 6);
                    drawText(cs, c.eseMarks, colEse, y + 6);
                    drawText(cs, c.totalMarks, colTot, y + 6);

                    // Grade highlight
                    if ("O".equals(c.grade) || "A+".equals(c.grade) || "A".equals(c.grade)) {
                        cs.setNonStrokingColor(new Color(39, 132, 91)); // Green
                    } else if ("RA".equals(c.grade) || "AB".equals(c.grade) || "SA".equals(c.grade)) {
                        cs.setNonStrokingColor(new Color(196, 69, 69)); // Red
                    } else {
                        cs.setNonStrokingColor(new Color(30, 40, 50));
                    }
                    drawText(cs, c.grade, colGrd + 2, y + 6);

                    if ("PASS".equalsIgnoreCase(c.result)) {
                        cs.setNonStrokingColor(new Color(39, 132, 91));
                    } else {
                        cs.setNonStrokingColor(new Color(196, 69, 69));
                    }
                    drawText(cs, c.result, colRes, y + 6);

                    y -= rowH;
                    alt = !alt;
                }

                // Bottom border of table
                cs.setStrokingColor(new Color(23, 54, 93));
                cs.setLineWidth(1.0f);
                cs.addLine(tableX, y + rowH, tableX + tableW, y + rowH);
                cs.stroke();

                y -= 18;

                // Summary Evaluation Box
                float sumBoxW = tableW;
                float sumBoxH = 68;

                cs.setNonStrokingColor(new Color(246, 248, 251));
                cs.addRect(tableX, y - sumBoxH + 12, sumBoxW, sumBoxH);
                cs.fill();
                cs.setStrokingColor(new Color(23, 54, 93));
                cs.setLineWidth(1.2f);
                cs.addRect(tableX, y - sumBoxH + 12, sumBoxW, sumBoxH);
                cs.stroke();

                float sy = y - 4;
                cs.setNonStrokingColor(new Color(23, 54, 93));
                cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
                drawText(cs, "SEMESTER PERFORMANCE SUMMARY", tableX + 12, sy);
                sy -= 18;

                cs.setFont(PDType1Font.HELVETICA_BOLD, 9.5f);
                drawText(cs, "Credits Registered : ", tableX + 12, sy);
                cs.setFont(PDType1Font.HELVETICA, 9.5f);
                drawText(cs, String.valueOf(totalCreditsRegistered), tableX + 115, sy);

                cs.setFont(PDType1Font.HELVETICA_BOLD, 9.5f);
                drawText(cs, "Semester SGPA    : ", tableX + 170, sy);
                cs.setFont(PDType1Font.HELVETICA_BOLD, 10.5f);
                cs.setNonStrokingColor(new Color(0, 120, 180));
                drawText(cs, String.format("%.2f / 10.00", sgpa), tableX + 270, sy);

                cs.setNonStrokingColor(new Color(23, 54, 93));
                cs.setFont(PDType1Font.HELVETICA_BOLD, 9.5f);
                drawText(cs, "Overall Result : ", tableX + 375, sy);
                if ("PASS".equalsIgnoreCase(overallResult)) {
                    cs.setNonStrokingColor(new Color(39, 132, 91));
                } else {
                    cs.setNonStrokingColor(new Color(196, 69, 69));
                }
                cs.setFont(PDType1Font.HELVETICA_BOLD, 10.5f);
                drawText(cs, overallResult != null ? overallResult : "—", tableX + 455, sy);

                sy -= 16;
                cs.setNonStrokingColor(new Color(23, 54, 93));
                cs.setFont(PDType1Font.HELVETICA_BOLD, 9.5f);
                drawText(cs, "Credits Earned       : ", tableX + 12, sy);
                cs.setFont(PDType1Font.HELVETICA, 9.5f);
                drawText(cs, String.valueOf(totalCreditsEarned), tableX + 115, sy);

                cs.setFont(PDType1Font.HELVETICA_BOLD, 9.5f);
                drawText(cs, "Percentage Equiv : ", tableX + 170, sy);
                cs.setFont(PDType1Font.HELVETICA, 9.5f);
                drawText(cs, String.format("%.2f%% (SGPA x 10)", equivalentPercentage), tableX + 270, sy);

                // Footer & Signatures
                float footerY = margin + 40;
                cs.setStrokingColor(new Color(210, 220, 230));
                cs.setLineWidth(0.8f);
                cs.addLine(margin + 12, footerY + 28, pageWidth - margin - 12, footerY + 28);
                cs.stroke();

                cs.setNonStrokingColor(new Color(100, 115, 135));
                cs.setFont(PDType1Font.HELVETICA, 8);
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
                drawText(cs, "Generated by KEC ACADEMIX on: " + sdf.format(new Date()), margin + 12, footerY + 12);
                drawText(cs, "Official Autonomous Academic Evaluation Statement • Authenticated via MySQL Database", margin + 12, footerY);

                cs.setNonStrokingColor(new Color(23, 54, 93));
                cs.setFont(PDType1Font.HELVETICA_BOLD, 9);
                drawText(cs, "CONTROLLER OF EXAMINATIONS", pageWidth - margin - 180, footerY);
            }

            doc.save(outputFile);
        }
    }

    private static void drawText(PDPageContentStream cs, String text, float x, float y) throws IOException {
        if (text == null) return;
        cs.beginText();
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    private static void drawCenteredText(PDPageContentStream cs, String text, PDType1Font font, float fontSize, float pageWidth, float y) throws IOException {
        if (text == null) return;
        float textWidth = font.getStringWidth(text) / 1000 * fontSize;
        float x = (pageWidth - textWidth) / 2;
        cs.setFont(font, fontSize);
        drawText(cs, text, x, y);
    }
}

