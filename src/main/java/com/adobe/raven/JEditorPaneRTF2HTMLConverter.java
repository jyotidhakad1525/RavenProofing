package com.adobe.raven;

import com.auxilii.msgparser.rtf.RTF2HTMLConverter;

import javax.swing.*;
import javax.swing.text.EditorKit;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JEditorPaneRTF2HTMLConverter implements RTF2HTMLConverter {

    public String rtf2html(String rtf) throws Exception {

        String rtf1 = handleSpecialCharacters(rtf);
//        String rtf1 = "{\\*\\htmltag64 <p id=\"reg_foot\">}\\htmlrtf \\par\n" +
//                "\\htmlrtf0 \\htmlrtf {\\htmlrtf0 \\htmlrtf{\\f4\\fs24\\htmlrtf0 Nadawc\\'b9 tej wiadomo\\'9cci marketingowej jest Adobe Systems Software Ireland Limited\\htmlrtf\\f0}\\htmlrtf0 , 4-6 Riverwalk, Citywest Business Park, Dublin 24, Irlandia.\n" +
//                "{\\*\\htmltag116 <br>}\\htmlrtf \\line\n" +
//                "\\htmlrtf0 ";
        String plain = null;
        //rtf = rtf.replaceAll("\\\\\'f3w","ą");
        rtf = rtf.replaceAll("\\\\\'b9","ą");
        rtf = rtf.replaceAll("\\\\\'9c","ś");
        if (rtf != null) {
            plain = this.fetchHtmlSection(rtf);
            plain = this.replaceHexSequences(plain);
            plain = this.replaceSpecialSequences(plain);
            plain = this.replaceRemainingControlSequences(plain);
            plain = this.replaceLineBreaks(plain);
        }

        return plain;
//        JEditorPane p = new JEditorPane();
//        p.setContentType("text/rtf");
//        EditorKit kitRtf = p.getEditorKitForContentType("text/rtf");
//        try {
//            StringReader rtfReader = new StringReader(rtf);
//            kitRtf.read(rtfReader, p.getDocument(), 0);
//            kitRtf = null;
//            EditorKit kitHtml = p.getEditorKitForContentType("text/html");
//            Writer writer = new StringWriter();
//            kitHtml.write(writer, p.getDocument(), 0, p.getDocument().getLength());
//            String value = handleSpecialCharacters(writer.toString());
//            return value;
//        } catch (Exception e) {
//            throw new Exception("Could not convert RTF to HTML.", e);
//        }
    }

    public String handleSpecialCharacters(String str) {
        byte[] utf8bytes = new byte[0];
        try {
            utf8bytes = str.getBytes("UTF8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Charset utf8charset = Charset.forName("UTF-8");
        Charset iso88591charset = Charset.forName("ISO-8859-1");

        String string = new String ( utf8bytes, utf8charset ); //Conversion to UTF-8

        System.out.println("UTF8"+string);

        byte[] iso88591bytes = string.getBytes(iso88591charset);

        String string2 = new String ( iso88591bytes, iso88591charset ); //Conversion to ISO-88591

        System.out.println("ISO"+string2);

        return string2;
    }

    private String replaceLineBreaks(String text) {
        text = text.replaceAll("( <br/> ( <br/> )+)", " <br/> ");
        text = text.replaceAll("[\\n\\r]+", "");
        return text;
    }

    private String replaceHexSequences(String text) {
        Pattern p = Pattern.compile("\\\\'(..)");
        Matcher m = p.matcher(text);

        while(m.find()) {
            for(int g = 1; g <= m.groupCount(); ++g) {
                String hex = m.group(g);
                String hexToString = hexToString(hex, "CP1252");
                if (hexToString != null) {
                    text = text.replaceAll("\\\\'" + hex, hexToString);
                }
            }
        }

        return text;
    }

    private String fetchHtmlSection(String text) {
        String html = null;
        int htmlStart = -1;
        int htmlEnd = -1;
        String[] htmlStartTags = new String[]{"<html ", "<Html ", "<HTML "};
        String[] htmlEndTags = new String[]{"</html>", "</Html>", "</HTML>"};

        int i;
        for(i = 0; i < htmlStartTags.length && htmlStart < 0; ++i) {
            htmlStart = text.indexOf(htmlStartTags[i]);
        }

        for(i = 0; i < htmlEndTags.length && htmlEnd < 0; ++i) {
            htmlEnd = text.indexOf(htmlEndTags[i]);
            if (htmlEnd > 0) {
                htmlEnd += htmlEndTags[i].length();
            }
        }

        if (htmlStart > -1 && htmlEnd > -1) {
            html = text.substring(htmlStart, htmlEnd + 1);
        } else {
            html = "<html><body style=\"font-family:'Courier',monospace;font-size:10pt;\">" + text + "</body></html>";
            html = html.replaceAll("[\\n\\r]+", " <br/> ");
            html = html.replaceAll("(http://\\S+)", "<a href=\"$1\">$1</a>");
            html = html.replaceAll("mailto:(\\S+@\\S+)", "<a href=\"mailto:$1\">$1</a>");
        }

        return html;
    }

    private String replaceSpecialSequences(String text) {

        text = text.replaceAll("\\{\\\\S+ [^\\s\\\\}]*\\}", "");
        text = text.replaceAll("\\{HYPERLINK[^\\}]*\\}", "");
        text = text.replaceAll("\\{\\\\pntext[^\\}]*\\}", "");
      //  text = text.replaceAll("\\{\\\\f\\d+[^\\}]*\\}", "");
        text = text.replaceAll("\\{\\\\\\*\\\\htmltag\\d+[^\\}<]+(<.+>)\\}", "$1");
        text = text.replaceAll("\\{\\\\\\*\\\\htmltag\\d+[^\\}<]+\\}", "");
        text = text.replaceAll("([^\\\\])\\}+", "$1");
        text = text.replaceAll("([^\\\\])\\{+", "$1");
        text = text.replaceAll("\\\\\\}", "}");
        text = text.replaceAll("\\\\\\{", "{");
        return text;
    }

    private static String hexToString(String hex, String charset) {
        boolean var2 = false;

        int i;
        try {
            i = Integer.parseInt(hex, 16);
        } catch (NumberFormatException var6) {
       //     logger.warning("Could not interpret " + hex + " as a number.");
            return null;
        }

        byte[] b = new byte[]{(byte)i};

        try {
            return new String(b, charset);
        } catch (UnsupportedEncodingException var5) {
     //       logger.log(Level.FINEST, "Unsupported encoding: " + charset);
            return null;
        }
    }

    private String replaceRemainingControlSequences(String text) {
        text = text.replaceAll("\\\\pard*", "\n");
        text = text.replaceAll("\\\\tab", "\t");
        text = text.replaceAll("\\\\\\*\\\\\\S+", "");
        text = text.replaceAll("\\\\\\S+", "");
        return text;
    }

}
