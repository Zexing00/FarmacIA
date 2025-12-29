package com.example.farmacia.network;

import java.util.List;

public class GeminiResponse {
    public List<Candidate> candidates;

    public String getFirstTextSafe() {
        if (candidates == null || candidates.isEmpty()) return null;
        Candidate c = candidates.get(0);
        if (c == null || c.content == null || c.content.parts == null || c.content.parts.isEmpty()) return null;
        Part p = c.content.parts.get(0);
        return p != null ? p.text : null;
    }

    public static class Candidate {
        public Content content;
    }

    public static class Content {
        public List<Part> parts;
    }

    public static class Part {
        public String text;
    }
}
