package asaitov.arduino.plugin.customfold;

import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;
import org.fife.ui.rsyntaxtextarea.folding.Fold;
import org.fife.ui.rsyntaxtextarea.folding.FoldManager;
import org.fife.ui.rsyntaxtextarea.folding.FoldParser;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.app.Editor;
import processing.app.EditorTab;
import processing.app.helpers.Keys;
import processing.app.helpers.SimpleAction;
import processing.app.syntax.SketchTextArea;
import processing.app.tools.Tool;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static asaitov.arduino.plugin.customfold.CustomFoldUtil.CUSTOM_FOLD_TYPE;
import static org.fife.ui.rsyntaxtextarea.SyntaxConstants.SYNTAX_STYLE_C;
import static org.fife.ui.rsyntaxtextarea.SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS;


public class CustomFold implements Tool {
    private static final Logger logger = LoggerFactory.getLogger(CustomFold.class);
    private static final KeyStroke HOTKEY = Keys.ctrlAlt(KeyEvent.VK_F);

    private Editor editor;

    public void init(Editor editor) {
        this.editor = editor;
        wrapFoldParser(SYNTAX_STYLE_C);
        wrapFoldParser(SYNTAX_STYLE_CPLUSPLUS);
        Keys.bind(editor.getRootPane(), new SimpleAction("Custom folding", this::processCommand), HOTKEY);
    }

    public String getMenuTitle() {
        return "Custom folding";
    }

    public void run() {
        JOptionPane.showMessageDialog(editor, "Custom folding enabled for C/C++\nHotkey: Ctrl+Alt+F");
    }

    protected void processCommand() {
        try {
            EditorTab tab = editor.getCurrentTab();
            SketchTextArea textArea = tab.getTextArea();

            int selectionStart = textArea.getSelectionStart();
            int selectionEnd = textArea.getSelectionEnd();
            if (selectionStart < selectionEnd && selectionEnd == textArea.getLineStartOffset(textArea.getLineOfOffset(selectionEnd)))
                selectionEnd--;

            int selectionLineStart = textArea.getLineOfOffset(selectionStart);
            int selectionLineEnd = textArea.getLineOfOffset(selectionEnd);

            if (selectionLineStart == selectionLineEnd) {
                removeFoldingBLock(textArea, selectionLineStart);
            } else {
                addFoldingBlock(textArea, selectionLineStart, selectionLineEnd);
            }
            RSyntaxUtilities.possiblyRepaintGutter(textArea);
            textArea.repaint();
        } catch (Exception e) {
            logger.error("Failed to process hotkey", e);
        }
    }

    private void addFoldingBlock(SketchTextArea textArea, int startLine, int endLine) throws BadLocationException {
        Fold newFold = CustomFoldUtil.newFold(textArea, CUSTOM_FOLD_TYPE, textArea.getLineStartOffset(startLine), textArea.getLineEndOffset(endLine) - 1);
        List<Fold> folds = CustomFoldUtil.merge(textArea, getFolds(textArea), Collections.singletonList(newFold));
        textArea.getFoldManager().setFolds(folds);
    }

    private void removeFoldingBLock(SketchTextArea textArea, int line) throws BadLocationException {
        List<Fold> folds = CustomFoldUtil.removeFold(textArea, getFolds(textArea), textArea.getLineStartOffset(line));
        textArea.getFoldManager().setFolds(folds);
    }

    private List<Fold> getFolds(SketchTextArea textArea) {
        FoldManager fm = textArea.getFoldManager();
        List<Fold> folds = new ArrayList<>(fm.getFoldCount());
        for (int i = 0; i < fm.getFoldCount(); i++) {
            folds.add(fm.getFold(i));
        }
        return folds;
    }

    private static void wrapFoldParser(String syntaxStyle) {
        FoldParser parser = FoldParserManager.get().getFoldParser(syntaxStyle);
        if (parser != null) {
            FoldParserManager.get().addFoldParserMapping(syntaxStyle, new CustomFoldParser(parser));
        }
    }
}
