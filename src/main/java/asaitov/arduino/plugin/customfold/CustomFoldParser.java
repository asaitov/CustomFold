package asaitov.arduino.plugin.customfold;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.folding.Fold;
import org.fife.ui.rsyntaxtextarea.folding.FoldManager;
import org.fife.ui.rsyntaxtextarea.folding.FoldParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CustomFoldParser implements FoldParser {
    private final FoldParser delegate;
    private static final Logger logger = LoggerFactory.getLogger(CustomFoldParser.class);

    public CustomFoldParser(FoldParser delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<Fold> getFolds(RSyntaxTextArea textArea) {
        List<Fold> parsedFolds = delegate.getFolds(textArea);
        try {
            List<Fold> custom = new ArrayList<>();
            FoldManager foldManager = textArea.getFoldManager();
            for (int i = 0; i < foldManager.getFoldCount(); i++) {
                CustomFoldUtil.flattenCustomFolds(textArea, foldManager.getFold(i), custom);
            }
            return CustomFoldUtil.merge(textArea, parsedFolds, custom);
        } catch (Exception e) {
            logger.error("Failed to merge folds", e);
            return parsedFolds;
        }
    }
}
