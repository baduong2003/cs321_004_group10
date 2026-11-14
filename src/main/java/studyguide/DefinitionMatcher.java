package studyguide;

import java.util.*;
import java.util.regex.*;

public class DefinitionMatcher  {

    public Map<String, String> matchDefinitions(List<String> concepts, String text)  {

        Map<String, String> vocabulary = new LinkedHashMap<>();

        if(concepts == null || text == null || text.isBlank())  {
            return vocabulary;
        }

        String[] lines = text.split("\\R");

        for(String concept : concepts)  {
            String definition = findDefinition(concept, lines);
            vocabulary.put(concept, definition);
        }

        return vocabulary;
    }

    private String findDefinition(String concept, String[] lines)   {
        String escapedConcept = Pattern.quote(concept);

        Pattern directDefinition = Pattern.compile("^\\s*" + escapedConcept + "\\s*[:\\---]\\s*(.+)$", Pattern.CASE_INSENSITIVE);

        Pattern sentenceDefinition = Pattern.compile("^\\s*" + escapedConcept + "\\s+(?:is\\s+defined\\s+as|is|are|refers\\s+to|means|describes)\\s+(.+)$", Pattern.CASE_INSENSITIVE);

        for(String line : lines) {
            Matcher directMatcher = directDefinition.matcher(line);

            if(directMatcher.find())    {
                return directMatcher.group(1).trim();
            }

            Matcher sentenceMatcher = sentenceDefinition.matcher(line);

            if(sentenceMatcher.find())  {
                return sentenceMatcher.group(1).trim();
            }
        }

        return "Definition not found in source material.";
    }
}