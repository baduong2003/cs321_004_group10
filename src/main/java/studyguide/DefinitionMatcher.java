package studyguide;

import java.util.*;
import java.util.regex.*;

public class DefinitionMatcher  {

    private static final String NOT_FOUND = "Definition not found in source material.";

    public Map<String, String> matchDefinitions(List<String> concepts, String text)  {

        Map<String, String> vocabulary = new LinkedHashMap<>();

        if(concepts == null || text == null || text.isBlank())  {
            return vocabulary;
        }

        String[] lines = text.split("\\R");

        for(String concept : concepts)  {
            if(concept == null || concept.isBlank())    {
                continue;
            }

            String definition = findDefinition(concept, lines, text);
            vocabulary.put(concept, definition);
        }

        return vocabulary;
    }

    private String findDefinition(String concept, String[] lines, String text)   {
        String escapedConcept = Pattern.quote(concept.trim());

        //Format 1: "Concept: definition"
        //Format 2: "Concept - definition"
        Pattern directDefinition = Pattern.compile("^\\s*" + escapedConcept + "\\s*(?:[:—–-])\\s*(.+)$", Pattern.CASE_INSENSITIVE);

        //Format 3: Concept is/means/refers to definition
        Pattern sentenceDefinition = Pattern.compile("^\\s*" + escapedConcept + "\\s+(?:is\\s+defined\\s+as|is|are|" + "refers\\s+to|means|describes)\\s+(.+)$", Pattern.CASE_INSENSITIVE);

        //Format 4: Definition is known as "Concept"
        Pattern reverseDefinition = Pattern.compile("^\\s*(.+?)\\s+" + "(?:is\\s+known\\s+as|is\\s+called|are\\s+called)\\s+" + escapedConcept +"\\b.*$", Pattern.CASE_INSENSITIVE);

        for(String line : lines) {
            Matcher directMatcher = directDefinition.matcher(line);

            if(directMatcher.find())    {
                return directMatcher.group(1).trim();
            }

            Matcher sentenceMatcher = sentenceDefinition.matcher(line);

            if(sentenceMatcher.find())  {
                return sentenceMatcher.group(1).trim();
            }

            Matcher reverseMatcher = reverseDefinition.matcher(line);

            if(reverseMatcher.find())   {
                return  reverseMatcher.group(1).trim();
            }
        }

        String[] sentences = text.split("(?<=[.!?])\\s+");

        Pattern conceptPattern = Pattern.compile("\\b" + escapedConcept + "\\b", Pattern.CASE_INSENSITIVE);

        for(String sentence : sentences)    {
            if(conceptPattern.matcher(sentence).find())  {
                return sentence.trim();
            }
        }

        return NOT_FOUND;
    }
}