package blockchain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Text {
    private final String file;
    private final Queue<String> texts;

    public Text() {
        this.file = "./text.txt";
        this.texts = new LinkedList<>();
    }

    public void initTexts() throws IOException {
        this.texts.add("no message");
        String[] tokenizedText = Files.readString(Paths.get(this.file)).split("\n\n");
        for (String i : tokenizedText) {
            if (!Objects.equals(i, "")) {
                this.texts.add(i);
            }
        }
    }

    public String next() {
        return this.texts.poll();
    }
}