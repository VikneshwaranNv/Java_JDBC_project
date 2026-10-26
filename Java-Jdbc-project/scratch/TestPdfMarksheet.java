import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TestPdfMarksheet {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Testing PDF Marksheet Generation ===");

        List<PdfMarksheetGenerator.CourseMarkEntry> list = new ArrayList<>();
        list.add(new PdfMarksheetGenerator.CourseMarkEntry(
                "CS2401", "Java Programming", "Theory", 4, 88.0, 36.0, 40.0, "52.0", 60.0, "88.0", "A+", 9, "PASS"
        ));
        list.add(new PdfMarksheetGenerator.CourseMarkEntry(
                "CS2402", "Database Management Systems", "Theory", 3, 92.0, 35.0, 40.0, "45.0", 60.0, "80.0", "A", 8, "PASS"
        ));
        list.add(new PdfMarksheetGenerator.CourseMarkEntry(
                "CS2403", "Data Structures Laboratory", "Practical", 2, 95.0, 56.0, 60.0, "36.0", 40.0, "92.0", "O", 10, "PASS"
        ));

        File out = new File("scratch/test_marksheet.pdf");
        PdfMarksheetGenerator.generateMarksheetPdf(
                out,
                "25ITR173",
                "VIKNESHWARAN N",
                "Information Technology",
                4,
                "2026-2027",
                list,
                9,
                9,
                8.89,
                88.9,
                "PASS"
        );

        if (out.exists() && out.length() > 1000) {
            System.out.println("✓ Sample PDF Marksheet generated successfully: " + out.getAbsolutePath() + " (" + out.length() + " bytes)");
        } else {
            System.err.println("✗ PDF generation failed or file too small");
            System.exit(1);
        }
    }
}

