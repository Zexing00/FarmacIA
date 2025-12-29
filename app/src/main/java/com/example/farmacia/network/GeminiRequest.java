package com.example.farmacia.network;

import java.util.ArrayList;
import java.util.List;

public class GeminiRequest {
    public List<Content> contents;

    public GeminiRequest(String userText) {
        contents = new ArrayList<>();
        Content c = new Content();
        c.parts = new ArrayList<>();
        Part p = new Part();
        p.text = userText;
        c.parts.add(p);
        contents.add(c);
    }

    public static class Content {
        public List<Part> parts;
    }

    public static class Part {
        public String text;
    }
}
