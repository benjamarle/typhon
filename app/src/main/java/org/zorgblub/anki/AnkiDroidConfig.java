package org.zorgblub.anki;

public final class
AnkiDroidConfig {
    // Name of model which will be created in AnkiDroid
    public static final String MODEL_NAME = "org.org.zorgblub.Typhon";
    // Optional space separated list of tags to add to every note
    public static final String TAGS = "Typhon";
    // List of field names that will be used in AnkiDroid model
    public static final String[] FIELDS = {"Expression","Reading","Meaning","Sentence", "Reason", "Deinflected"};
    // List of card names that will be used in AnkiDroid (one for each direction of learning)
    public static final String[] CARD_NAMES = {"Japanese>English"};
    // CSS to share between all the cards (optional). User will need to install the NotoSans font by themselves
    public static final String CSS = ".card {\n" +
            " font-family: NotoSansJP;\n" +
            " font-size: 24px;\n" +
            " text-align: center;\n" +
            " color: black;\n" +
            " background-color: white;\n" +
            " word-wrap: break-word;\n" +
            "}\n" +
            "@font-face { font-family: \"NotoSansJP\"; src: url('_NotoSansJP-Regular.otf'); }\n" +
            "@font-face { font-family: \"NotoSansJP\"; src: url('_NotoSansJP-Bold.otf'); font-weight: bold; }\n" +
            "\n" +
            ".big { font-size: 64px; }\n" +
            ".emph { font-weight: \"bold\"; }\n" +
            ".small { font-size: 18px;}\n";
    // Template for the question of each card
    static final String QFMT1 = "<div class=big>{{Expression}}</div><br>{{Sentence}}";
    public static final String[] QFMT = {QFMT1};
    // Template for the answer (use identical for both sides)
    static final String AFMT1 = "{{Meaning}}<br><br>\n" +
            "<div class=small>\n" +
            "{{Expression}} ({{Reading}})<br><br>\n" +
            "{{#Reason}}Deinflection: {{Deinflected}}{{Reason}}{{/Reason}}\n" +
            "</div>";
    public static final String[] AFMT = {AFMT1};
    // Define two keys which will be used when using legacy ACTION_SEND intent
    public static final String FRONT_SIDE_KEY = FIELDS[0];
    public static final String BACK_SIDE_KEY = FIELDS[2];
}