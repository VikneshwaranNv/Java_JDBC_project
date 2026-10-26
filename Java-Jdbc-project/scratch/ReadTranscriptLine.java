import java.io.*;

public class ReadTranscriptLine {
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("C:/Users/vigne/.gemini/antigravity/brain/7b55cb41-87ce-4906-a4ff-3baa0e7b656b/.system_generated/logs/transcript_full.jsonl"));
        String line;
        int count = 0;
        while ((line = br.readLine()) != null) {
            count++;
            if (count == 805) {
                int idx = 0;
                while ((idx = line.indexOf("test_stud_arun", idx)) != -1) {
                    int start = Math.max(0, idx - 100);
                    int end = Math.min(line.length(), idx + 200);
                    System.out.println("STUD AT " + idx + ": " + line.substring(start, end));
                    idx += 14;
                }
                break;
            }
        }
    }
}

