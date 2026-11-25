package tr.edu.sakarya.bil002.summary;

import java.util.List;

@FunctionalInterface
public interface SummaryHistoryReader {
    List<SummaryEntry> history(String accountNumber);
}
