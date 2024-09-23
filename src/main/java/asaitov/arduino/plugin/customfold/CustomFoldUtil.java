package asaitov.arduino.plugin.customfold;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.folding.Fold;

import javax.swing.text.BadLocationException;
import java.util.ArrayList;
import java.util.List;

public class CustomFoldUtil {
    public static int CUSTOM_FOLD_TYPE = 2000;

    public static List<Fold> merge(RSyntaxTextArea textArea, List<Fold> base, List<Fold> custom) throws BadLocationException {
        List<Fold> result = new ArrayList<>();
        for (int i = 0, j = 0; i < base.size() || j < custom.size();) {
            if (i < base.size() && (j == custom.size() || base.get(i).getStartOffset() <= custom.get(j).getStartOffset())) {
                if (!result.isEmpty() && inside(result.get(result.size() - 1), base.get(i))) {
                    result.set(result.size() - 1, insert(textArea, result.get(result.size() - 1), base.get(i)));
                } else if (result.isEmpty() || result.get(result.size() - 1).getEndOffset() < base.get(i).getStartOffset()) {
                    result.add(base.get(i));
                }
                i++;
            } else {
                if (j < custom.size() && !result.isEmpty() && inside(result.get(result.size() - 1), custom.get(j))) {
                    result.set(result.size() - 1, insert(textArea, result.get(result.size() - 1), custom.get(j)));
                } else if (result.isEmpty() || result.get(result.size() - 1).getEndOffset() < custom.get(j).getStartOffset()) {
                    result.add(custom.get(j));
                }
                j++;
            }
        }
        return result;
    }

    private static Fold insert(RSyntaxTextArea textArea, Fold base, Fold custom) throws BadLocationException {
        List<Fold> children = new ArrayList<>();
        int i = 0;
        for (; i < base.getChildCount() && base.getChild(i).getEndOffset() < custom.getStartOffset(); i++) {
            children.add(base.getChild(i));
        }

        if (i < base.getChildCount() && inside(base.getChild(i), custom)) {
            children.add(insert(textArea, base.getChild(i++), custom));
        } else if (i == base.getChildCount() || custom.getStartOffset() < base.getChild(i).getStartOffset()) {
            children.add(custom);
        }

        for (; i < base.getChildCount(); i++) {
            if (children.size() > 0 && inside(children.get(children.size() - 1), base.getChild(i))) {
                children.set(children.size() - 1, insert(textArea, children.get(children.size() - 1), base.getChild(i)));
            } else if (children.size() == 0 || children.get(children.size() - 1).getEndOffset() < base.getChild(i).getStartOffset()) {
                children.add(base.getChild(i));
            }
        }
        return createFold(textArea, base, children);
    }

    public static List<Fold> removeFold(RSyntaxTextArea textArea, List<Fold> base, int offset) throws BadLocationException {
        List<Fold> result = new ArrayList<>();
        for (Fold fold: base)
            removeFold(textArea, fold, offset, result);
        return result;
    }

    private static boolean removeFold(RSyntaxTextArea textArea, Fold base, int offset, List<Fold> result) throws BadLocationException {
        boolean removed = false;
        if (base.getStartOffset() <= offset && base.getEndOffset() >= offset) {
            List<Fold> childrenResult = new ArrayList<>();
            for (int i = 0; i < base.getChildCount(); i++) {
                removed = removeFold(textArea, base.getChild(i), offset, childrenResult) || removed;
            }
            if (removed) {
                result.add(createFold(textArea, base, childrenResult));
            } else if (base.getFoldType() == CUSTOM_FOLD_TYPE) {
                removed = true;
                result.addAll(childrenResult);
            } else {
                result.add(base);
            }
        } else {
            result.add(base);
        }
        return removed;
    }

    public static void flattenCustomFolds(RSyntaxTextArea textArea, Fold fold, List<Fold> result) throws BadLocationException {
        if (fold.getFoldType() == CUSTOM_FOLD_TYPE && !fold.isOnSingleLine()) {
            result.add(copyFold(textArea, fold));
        }
        for (int i = 0; i < fold.getChildCount(); i++) {
            flattenCustomFolds(textArea, fold.getChild(i), result);
        }
    }

    public static Fold newFold(RSyntaxTextArea textArea, int type, int startOffset, int endOffset) throws BadLocationException {
        Fold result = new Fold(type, textArea, startOffset);
        result.setEndOffset(endOffset);
        return result;
    }

    public static Fold copyFold(RSyntaxTextArea textArea, Fold fold) throws BadLocationException {
        Fold result = new Fold(fold.getFoldType(), textArea, fold.getStartOffset());
        result.setEndOffset(fold.getEndOffset());
        result.setCollapsed(fold.isCollapsed());
        return result;
    }

    private static Fold createFold(RSyntaxTextArea textArea, Fold fold, List<Fold> children) throws BadLocationException {
        Fold result = copyFold(textArea, fold);
        for (Fold child: children) {
            copyChild(result, child);
        }
        return result;
    }

    private static void adoptChildren(Fold newParent, Fold oldParent) throws BadLocationException {
        for (int i = 0; i < oldParent.getChildCount(); i++) {
            Fold child = oldParent.getChild(i);
            copyChild(newParent, child);
        }
    }

    private static void copyChild(Fold parent, Fold child) throws BadLocationException {
        Fold result = parent.createChild(child.getFoldType(), child.getStartOffset());
        result.setEndOffset(child.getEndOffset());
        result.setCollapsed(child.isCollapsed());
        adoptChildren(result, child);
    }

    private static boolean inside(Fold big, Fold small) {
        return big.getStartOffset() < small.getStartOffset() && big.getEndOffset() >= small.getEndOffset();
    }
}
