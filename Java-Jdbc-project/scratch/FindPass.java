import java.io.*;
import java.util.regex.*;

public class FindPass {
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("C:/Users/vigne/.gemini/antigravity/brain/7b55cb41-87ce-4906-a4ff-3baa0e7b656b/.system_generated/logs/transcript.jsonl"));
        String line;
        Pattern p = Pattern.compile("testPass\\s*=\\s*\"[^\"]+\"");
        while ((line = br.readLine()) != null) {
            Matcher m = p.matcher(line);
            while (m.find()) {
                System.out.println(m.group());
            }
        }
    }
}

