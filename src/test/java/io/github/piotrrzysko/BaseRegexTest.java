package io.github.piotrrzysko;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.RFC4180Parser;
import com.opencsv.RFC4180ParserBuilder;
import io.airlift.slice.Slice;
import io.airlift.slice.Slices;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

public abstract class BaseRegexTest
{
    @Test
    public void testComplianceWithW3c()
    {
        SoftAssertions softAssertions = new SoftAssertions();
        for (String[] testCase : loadTestCases()) {

            String pattern = testCase[0];
            String flags = testCase[1];
            byte[] inputBytes = Base64.getDecoder().decode(testCase[2]);
            Slice input = Slices.wrappedBuffer(inputBytes);
            boolean matches = Boolean.parseBoolean(testCase[3]);
            boolean error = Boolean.parseBoolean(testCase[4]);

            try {
                boolean actualMatchesResult = matches(pattern, flags, input);
                if (error) {
                    softAssertions.fail("Regex: '%s', flags: '%s', input: '%s' should fail", pattern, flags, input.toStringUtf8());
                }
                else if (matches) {
                    softAssertions.assertThat(actualMatchesResult)
                            .withFailMessage("Regex: '%s', flags: '%s', input: '%s'", pattern, flags, input.toStringUtf8())
                            .isTrue();
                }
                else {
                    softAssertions.assertThat(actualMatchesResult)
                            .withFailMessage("Regex: '%s', flags: '%s', input: '%s'", pattern, flags, input.toStringUtf8())
                            .isFalse();
                }
            }
            catch (Exception e) {
                if (!error) {
                    softAssertions.fail("Regex: '%s', flags: '%s', input: '%s' should not fail", pattern, flags, input.toStringUtf8());
                }
            }
        }
        softAssertions.assertAll();
    }

    protected abstract boolean matches(String pattern, String flags, Slice input);

    private static List<String[]> loadTestCases()
    {
        Path testCasesPath = Path.of(BaseRegexTest.class.getClassLoader().getResource("qt3tests.csv").getPath());
        RFC4180Parser csvParser = new RFC4180ParserBuilder().build();

        try (Reader reader = Files.newBufferedReader(testCasesPath);
                CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(csvParser).build()) {
            return csvReader.readAll();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
