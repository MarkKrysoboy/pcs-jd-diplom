import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BooleanSearchEngine implements SearchEngine {
    Map<String, List<PageEntry>> wordInfMap;


    public BooleanSearchEngine(File pdfsDir) throws IOException {
        wordInfMap = new HashMap<>();
        if (pdfsDir.isDirectory()) {
            for (File file : pdfsDir.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".pdf")) {
                    try (var doc = new PdfDocument(new PdfReader(file))) {
                        for (int i = 1; i <= doc.getPageNumber(doc.getLastPage()); i++) {
                            var text = PdfTextExtractor.getTextFromPage(doc.getPage(i));
                            var words = text.split("\\P{IsAlphabetic}+");
                            Set<String> wordSet = Arrays.stream(words)
                                    .map(String::toLowerCase)
                                    .collect(Collectors.toSet());
                            List<PageEntry> pageEntryList;
                            for (String word : wordSet) {
                                int count = Collections.frequency(Arrays.stream(words).collect(Collectors.toList()), word);
                                PageEntry pageEntry = new PageEntry(file.getName(), i, count);
                                if (!wordInfMap.containsKey(word)) {
                                    pageEntryList = new ArrayList<>();
                                    pageEntryList.add(pageEntry);
                                    wordInfMap.put(word, pageEntryList);
                                } else {
                                    wordInfMap.get(word).add(pageEntry);
                                    Collections.sort(wordInfMap.get(word), Collections.reverseOrder());
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    @Override
    public List<PageEntry> search(String word) {
        return wordInfMap.get(word.toLowerCase());
    }
}
