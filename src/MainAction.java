import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;

/**
 * Created by goreyjp on 15/9/29.
 */
public class MainAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        Editor editor = event.getData(PlatformDataKeys.EDITOR);
        final Document doc = event.getRequiredData(CommonDataKeys.EDITOR).getDocument();

        // 全局选区 offset
        int selectionStart = editor.getSelectionModel().getSelectionStart();
        int selectinoEnd = editor.getSelectionModel().getSelectionEnd();

        // 选区的 开始行/ 结束行
        final int lineStart = doc.getLineNumber(selectionStart);
        final int lineEnd = doc.getLineNumber(selectinoEnd);


        Runnable r = new Runnable() {
            @Override
            public void run() {
                int lines = doc.getLineCount();

                int maxOffset = 0;

                // 遍历每一行, 找到等号所在的最大的行偏移位置
                for (int i = lineStart; i <= lineEnd; i++) {
                    int p_start = doc.getLineStartOffset(i);
                    int p_end = doc.getLineEndOffset(i);

                    String lineText = doc.getText(TextRange.create(p_start, p_end));

                    int _currOffset = lineText.indexOf("=");

                    if (_currOffset > maxOffset) {
                        maxOffset = _currOffset;
                    }
                }

                // 遍历每一行, 调整每一行等号的位置
                for (int i = lineStart; i <= lineEnd; i++) {

                    int p_start = doc.getLineStartOffset(i);
                    int p_end = doc.getLineEndOffset(i);
                    String lineText = doc.getText(TextRange.create(p_start, p_end));
                    int _currOffset = lineText.indexOf("=");

                    // 此行有等号的话
                    if (_currOffset > -1) {
                        String _blankStr = createBlankStringWithLen(maxOffset - _currOffset);
                        // 插入补白
                        doc.insertString(p_start + _currOffset, _blankStr);
                    }
                }

            }
        };

        WriteCommandAction.runWriteCommandAction(project, r);
    }

    /**
     * 返回一个 空格字符串, 长度由参数 length 控制
     *
     * @param length
     * @return
     */
    private String createBlankStringWithLen(int length) {

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            sb.append(" ");
        }
        return sb.toString();

    }
}
